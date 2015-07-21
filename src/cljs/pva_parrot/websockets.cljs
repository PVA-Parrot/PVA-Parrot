
(ns pva-parrot.websockets
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [taoensso.encore :as enc    :refer (tracef debugf infof warnf errorf)]
            [taoensso.sente :as sente :refer (cb-success?)]))

(defn chsk-url-fn [path {:as window-location :keys [protocol host pathname]} websocket?]
  (str (if-not websocket? protocol (if (= protocol "https:") "wss:" "ws:"))
    "//localhost:3333"  (or path pathname)))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk"
        {:type :auto :chsk-url-fn chsk-url-fn})]

  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(defmulti handle-event (fn [[event-id event-message]] event-id))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event]}]
  (debugf "Unhandled event: %s" event))

(defmethod event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (if (= ?data {:first-open? true})
    (debugf "Channel socket successfully established!")
    (debugf "Channel socket state change: %s" ?data)))

(defmethod event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (debugf "Push event from server: %s" ?data)
  (handle-event ?data))

(defmethod event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (debugf "Handshake: %s" ?data)))

(sente/start-chsk-router! ch-chsk event-msg-handler)
