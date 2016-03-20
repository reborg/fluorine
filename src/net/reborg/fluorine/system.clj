(ns ^:skip-aot net.reborg.fluorine.system
  (:gen-class)
  (:require [net.reborg.fluorine]
            [net.reborg.fluorine.bootstrap]
            [clojure.tools.nrepl.server :as nrepl]
            [net.reborg.fluorine.config :as c]
            [net.reborg.fluorine.bus :as bus]
            [net.reborg.fluorine.watcher :as watcher]
            [aleph.http :as http]
            [manifold.stream :as s]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defn- init-change-stream [] (s/stream))
(defn- close-change-stream [s] (when s (s/close! s)))

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

(defn- start-nrepl-server [port]
  (let [server (nrepl/start-server :port port :bind "0.0.0.0")] server))

(defn- stop-nrepl-server [server]
  (when server (nrepl/stop-server server)))

(defrecord FluorineServer []
  component/Lifecycle
  (start [this]
    (let [init (-> this
                   (assoc :changes (init-change-stream))
                   (assoc :bus (bus/start))
                   (assoc :watchers (watcher/init))
                   (assoc :server (start-server #'net.reborg.fluorine/handler (c/fluorine-port)))
                   (assoc :nrepl-server (start-nrepl-server (c/nrepl-port)))
                   )]
      (log/info (format "started with %s" (c/debug)))
      (bus/bootstrap-bus! (:bus init) (:changes init))
      init))
  (stop [this]
    (stop-server (:server this))
    (watcher/teardown (:watchers this))
    (close-change-stream (:changes this))
    (stop-nrepl-server (:nrepl-server this))
    (bus/stop (:bus this))
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
