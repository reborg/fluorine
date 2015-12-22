(ns ^:skip-aot net.reborg.fluorine.system
  (:gen-class)
  (:require [org.httpkit.server :refer [run-server]]
            [net.reborg.fluorine]
            [net.reborg.fluorine.bootstrap]
            [clojure.tools.nrepl.server :as nrepl]
            [net.reborg.fluorine.config :as c]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defn- start-server [handler port] (let [server (run-server handler {:port port})] server))
(defn- stop-server [server] (when server (server)))

(defn- start-nrepl-server [port] (let [server (nrepl/start-server :port port :bind "0.0.0.0")] server))
(defn- stop-nrepl-server [server] (when server (nrepl/stop-server server)))

(defrecord FluorineServer []
  component/Lifecycle
  (start [this]
    (let [init (-> this
                   (assoc :server (start-server #'net.reborg.fluorine/app (c/fluorine-port)))
                   (assoc :nrepl-server (start-nrepl-server (c/nrepl-port)))
                   )]
      (log/info (format "started with %s" (c/debug)))
      init))
  (stop [this]
    (stop-server (:server this))
    (stop-nrepl-server (:nrepl-server this))
    (-> this
        (dissoc :server)
        (dissoc :nrepl-server))))

(defn create-system []
  (FluorineServer.))

(defn -main [& args]
  (alter-var-root
    #'net.reborg.fluorine.bootstrap/system
    (fn [_] (.start (create-system)))))
