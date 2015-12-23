(ns net.reborg.fluorine-client
  (:require
    [clojure.tools.logging :as log]
    [aleph.http :as http]
    [manifold.stream :as s]
    [clojure.edn :as edn]
    ))

(defn attach [path callback]
  (let [conn @(http/websocket-client (str "ws://localhost:10101" path))]
    (s/consume #(callback (edn/read-string %)) conn)))

(defn- debug []
  (attach "/apps/clj-fe"
            (fn [cfg] (println "received new config" cfg))))
