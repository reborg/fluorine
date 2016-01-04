(ns ^:skip-aot net.reborg.fluorine.system
  (:gen-class)
  (:require [net.reborg.fluorine]
            [net.reborg.fluorine.bootstrap]
            [manifold.stream :as s]
            [manifold.bus :as b]
            [clojure.tools.nrepl.server :as nrepl]
            [net.reborg.fluorine.config :as c]
            [aleph.http :as http]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defn- init-change-stream [] (s/stream))
(defn- close-change-stream [s] (when s (s/close! s)))

(defn- init-bus [] (b/event-bus))

(defn- stop-server [server]
  (when server (.close server)))

(defn- start-server [handler port]
  (let [server (http/start-server handler {:port port})] server))

(defn- stop-server [server]
  (when server
    (try
      (.close server)
      (catch Exception e
        (log/warn "unable to close server. ignoring.")))))

(defn- init-watchers []
  (atom {}))

(defn- teardown-watchers [watchers]
  (when watchers
    (doall
      (map (fn [[k v]] (v)) @watchers))))

(defn- start-nrepl-server [port]
  (let [server (nrepl/start-server :port port :bind "0.0.0.0")] server))

(defn- stop-nrepl-server [server]
  (when server (nrepl/stop-server server)))

(defn- serialize [x]
  (with-out-str (clojure.pprint/write x)))

(defn- dispatch-fn [bus]
  (fn [event]
    (b/publish! bus
                (:channel event)
                (serialize (:msg event)))))

(defn- bootstrap-bus!
  "The bus needs to be bootstrapped after the change
  stream and the bus have been built."
  [bus changes]
  (s/consume (dispatch-fn bus) changes))

(defrecord FluorineServer []
  component/Lifecycle
  (start [this]
    (let [init (-> this
                   (assoc :changes (init-change-stream))
                   (assoc :bus (init-bus))
                   (assoc :watchers (init-watchers))
                   (assoc :server (start-server #'net.reborg.fluorine/handler (c/fluorine-port)))
                   (assoc :nrepl-server (start-nrepl-server (c/nrepl-port)))
                   )]
      (log/info (format "started with %s" (c/debug)))
      (bootstrap-bus! (:bus init) (:changes init))
      init))
  (stop [this]
    (stop-server (:server this))
    (teardown-watchers (:watchers this))
    (close-change-stream (:changes this))
    (stop-nrepl-server (:nrepl-server this))
    (-> this
        (dissoc :server)
        (dissoc :watchers)
        (dissoc :bus)
        (dissoc :changes)
        (dissoc :nrepl-server))))

(defn create-system []
  (FluorineServer.))

(defn -main [& args]
  (alter-var-root
    #'net.reborg.fluorine.bootstrap/system
    (fn [_] (.start (create-system)))))
