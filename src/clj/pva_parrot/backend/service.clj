(ns pva-parrot.backend.service
  (:gen-class)
  (:require [ring.middleware.params         :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.util.response             :refer [resource-response content-type]]
            [compojure.core                 :refer :all]
            [compojure.route                :as route]
            [org.httpkit.server             :as httpkit]
            [taoensso.sente                 :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [taoensso.timbre                :as timbre :refer (tracef debugf infof warnf errorf)]
            [clojure.data.csv               :as csv]
            [pva-parrot.calc.pca            :as pca]
            [pva-parrot.io                  :as io]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket!
       sente-web-server-adapter
       {:user-id-fn (fn [ring-req] (:client-id ring-req))})]
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
    (let [uid      (get-in ring-req [:params :client-id])
          csv-data (-> ?data
                       :file-body
                       csv/read-csv
                       io/process-csv)
          csv-matrix      (:matrix csv-data)
          normalized-data (pca/normalize-compos csv-matrix)
          pca-data        (pca/components csv-matrix)
          {:keys [headings body num-samples num-variables]} csv-data
          {:keys [std-dev eigen-values rotation]} pca-data
          reply-msg       [:data-import/file-returned
                           {:parsed-data {:headings headings
                                          :body body}
                            :num-samples num-samples
                            :num-variables num-variables
                            :summary-variables (pca/summarize-variables csv-matrix)
                            :summary-normalized (pca/summarize-variables normalized-data)
                            :std-devs      {:headings nil
                                            :body [std-dev]}
                            :eigen-values  {:headings nil
                                            :body [eigen-values]}
                            :eigen-vectors {:headings nil
                                            :body rotation}}]]
      (chsk-send! uid reply-msg))))

(sente/start-chsk-router! ch-chsk event-msg-handler)

(defroutes api-handlers
  (GET  "/" [] (-> (resource-response "index.html" {:root ""})
                   (content-type "text/html")))
  (route/resources "/js" {:root "js"})
  (route/resources "/css" {:root "css"})
  (route/resources "/img" {:root "img"})
  (GET  "/chsk" request (ring-ajax-get-or-ws-handshake request))
  (POST "/chsk" request (ring-ajax-post request)))

(def api (-> api-handlers
             (keyword-params/wrap-keyword-params)
             (params/wrap-params)))

(defn -main []
  (let [port (get (System/getenv) "PORT" 3333)]
    (httpkit/run-server api {:port port})))
