(ns net.reborg.fluorine-client
  (:require
    [net.reborg.fluorine.config :refer [fluorine-host fluorine-port]]
    [clojure.tools.logging :as log]
    [aleph.http :as http]
    [manifold.stream :as s]
    [clojure.edn :as edn]
    ))

(defn attach [path callback]
  (let [conn @(http/websocket-client
                (format "ws://%s:%s%s"
                        (fluorine-host)
                        (fluorine-port)
                        path))]
    (s/consume #(callback (edn/read-string %)) conn)))

(defn- debug []
  (attach "/apps/clj-fe"
            (fn [cfg] (println "received new config" cfg))))
