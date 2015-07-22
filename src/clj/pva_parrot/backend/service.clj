(ns pva-parrot.backend.service
  (:require [ring.middleware.params         :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [compojure.core                 :refer :all]
            [taoensso.sente                 :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [taoensso.timbre                :as timbre :refer (tracef debugf infof warnf errorf)]
            [clojure.data.csv               :as csv]
            [pva-parrot.calc.pca            :as pca]
            [pva-parrot.io                  :as io]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn)
  (def connected-uids                connected-uids))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:as event-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (when ?reply-fn
    (?reply-fn {:unmatched-event-as-echoed-from-server event})))

(defmethod event-msg-handler :data-import/csv-sent
  [{:as event-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (when ?data
    (let [uid            (get-in ring-req [:session :uid])
          csv-raw        (csv/read-csv (:file-body ?data))
          csv-headings   (first csv-raw)
          csv-body       (rest csv-raw)
          num-variables  (- (count csv-headings) 1)
          num-samples    (count csv-body)
          csv-matrix     (io/csv-to-matrix (:file-body ?data))
          summary-variables (->> csv-matrix
                                 (pca/transpose)
                                 (pca/summarize (rest csv-headings)))
          normalized-data (pca/normalize-compos csv-matrix)
          summary-normalized (->> normalized-data
                                  (pca/transpose)
                                  (pca/summarize (rest csv-headings)))
          pca-data       (pca/components csv-matrix)
          pca-std-devs   (:std-dev pca-data)
          eigen-values   (:eigen-values pca-data)
          eigen-vectors  (:rotation pca-data)
          reply-msg      [:data-import/file-returned
                          {:parsed-data   {:headings csv-headings :body csv-body}
                           :num-samples   num-samples
                           :num-variables num-variables
                           :summary-variables summary-variables
                           :summary-normalized summary-normalized
                           :std-devs      {:headings nil :body [pca-std-devs]}
                           :eigen-values  {:headings nil :body [eigen-values]}
                           :eigen-vectors {:headings nil :body eigen-vectors}
                           }]]
      (chsk-send! uid reply-msg))))

(sente/start-chsk-router! ch-chsk event-msg-handler)

(defroutes api-handlers
  (GET "/" [] "You found the PVA Parrot backend service!")
  (GET  "/chsk" request (ring-ajax-get-or-ws-handshake request))
  (POST "/chsk" request (ring-ajax-post request)))

(def api (-> api-handlers
             (keyword-params/wrap-keyword-params)
             (params/wrap-params)))
