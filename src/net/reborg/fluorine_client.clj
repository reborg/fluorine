(ns net.reborg.fluorine-client
  (:require
    [clojure.tools.logging :as log]
    [aleph.http :as http]
    [manifold.stream :as s]
    [manifold.deferred :as d]
    [clojure.edn :as edn]
    [clojure.data :refer [diff]]
    [clojure.main :refer [demunge]]
    [net.reborg.fluorine.data :as data]
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
    (s/on-closed conn #(log/warn (format "Lost 1 of %s connections." (count conns))))
    (s/consume
      (fn [new-cfg]
        (let [new-cfg (get-in (data/unmarshall new-cfg) kk)]
          (let [d (diff @cfg new-cfg)] (log/info "Received new config. Diff:" (second d)))
          (reset! cfg new-cfg)))
      conn)))

(defn keep-alive
  "Create a separate thread to send a ping message every X secs to prevent
  firewalls to close the connections. The future is the actual thread holder.
  The delay is there to prevent the thread to start right away."
  [conns]
  (delay
    (future
      (d/loop []
        (Thread/sleep 15000)
        (doseq [conn conns] (s/put! conn "ping"))
        (d/recur)))))

(defn- wait-config
  "Will try to fetch the local config atom
  until it's not an empty sequence or a final timeout."
  []
  (retry
    #(seq (keys @cfg))
    (+ (System/currentTimeMillis) 3000) 200))

(defn attach
  "Attach will connect to all the available server and register
  the callback function that updates the local atom.
  It then starts the keep-alive thread and waits until the callback
  is called once, to guarantee the configuration is ready to be used."
  ([path]
   (attach path "localhost" 10101))
  ([path hosts port & kk]
   (let [conns (connect hosts port path)
         keep-alive-thread (keep-alive conns)]
     (connect-all! conns kk)
     @keep-alive-thread
     (wait-config)
     {:conn conns :keep-alive keep-alive-thread})))

(defn detach
  "Closes an open channel, presumably
  resulting from attaching to it previously."
  [{conns :conns keep-alive :keep-alive}]
  (doseq [conn conns]
    (s/close! conn))
  (future-cancel @keep-alive))

(defn- debug []
  (attach "/apps/clj-fe" (fn [cfg] (println "received new config" cfg))))
