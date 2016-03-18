(ns net.reborg.fluorine-client
  (:require
    [clojure.tools.logging :as log]
    [aleph.http :as http]
    [manifold.stream :as s]
    [clojure.edn :as edn]
    [clojure.data :refer [diff]]
    [clojure.main :refer [demunge]]
    ))

(def ^{:doc "Main configuration"} cfg
  (atom {}))

(defn parse-hosts
  ([]
   (parse-hosts "127.0.0.1" 10101))
  ([hosts]
   (parse-hosts hosts 10101))
  ([hosts ports]
   (let [hosts (clojure.string/split hosts #",")]
     (map #(-> {:host % :port ports}) hosts))))

(defn- retry [f timeout & [sleeptime]]
  (if-let [res (f)]
    res
    (do
      (log/info (format "No results invoking %s yet. Retrying." (demunge (str f))))
      (Thread/sleep (or sleeptime 500))
      (if (< (System/currentTimeMillis) timeout)
        (recur f timeout [sleeptime])
        (throw (RuntimeException. (format "Timeout waiting for %s to return results." (demunge (str f)))))))))

(defn round-robin [f hosts port & [timeout]]
  (retry
    (fn []
      (->> (parse-hosts hosts port)
           (map #(try
                   (f (:host %) (:port %))
                   (catch Throwable t
                     (log/warn "connection failed" (str (type t) " " (.getMessage t))))))
           (remove nil?)
           seq))
    (or timeout (+ (System/currentTimeMillis) 3000))))

(defn- connect [hosts port path & [timeout]]
  "First attempt to connect roung robin to the given list of host.
  First host to reply creates the connection. If all hosts fail
  will wait and try again up to the requested timeout."
  (round-robin
    (fn [host port]
      (log/info "attempting to connect to" host port)
      @(http/websocket-client (format "ws://%s:%s%s" host port path)))
    hosts
    port
    timeout))

(defn- connect-all! [conns kk]
  (doseq [conn conns]
    (s/consume
      (fn [new-cfg]
        (let [new-cfg (get-in (edn/read-string new-cfg) kk)]
          (log/info "Received new config" (diff @cfg new-cfg))
          (reset! cfg new-cfg)))
      conn)))

(defn attach
  "Attach will connect to the first available server and register
  the callback function that updates the local atom.
  It the waits until the callback is called
  the first time to guarantee the configuration is ready to be used."
  ([path]
   (attach path "localhost" 10101))
  ([path hosts port & kk]
   (let [conns (connect hosts port path)]
     (connect-all! conns kk)
     (retry #(seq (keys @cfg)) (+ (System/currentTimeMillis) 3000) 200)
     conns)))

(defn detach
  "Closes an open channel, presumably
  resulting from attaching to it previously."
  [conns]
  (doseq [conn conns] (s/close! conn)))

(defn- debug []
  (attach "/apps/clj-fe" (fn [cfg] (println "received new config" cfg))))
