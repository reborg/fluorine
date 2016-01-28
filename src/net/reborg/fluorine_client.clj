(ns net.reborg.fluorine-client
  (:require
    [clojure.tools.logging :as log]
    [aleph.http :as http]
    [manifold.stream :as s]
    [clojure.edn :as edn]
    ))

(defn attach
  ([path callback]
   (attach path callback "localhost" 10101))
  ([path callback host port]
   (let [conn @(http/websocket-client
                 (format "ws://%s:%s%s"
                         host
                         port
                         path))]
     (s/consume #(callback (edn/read-string %)) conn))))

(defn detach
  "Closes an open channel, presumably
  resulting from attaching to it previously."
  [conn]
  (s/close! conn))

(defn- debug []
  (attach "/apps/clj-fe" (fn [cfg] (println "received new config" cfg))))
