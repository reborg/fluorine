(ns net.reborg.fluorine.bootstrap
  (:require [clojure.tools.namespace.repl :refer [disable-reload! refresh]]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

;; this prevents system var to disappear on reset
(disable-reload!)

(def system nil)

(def ^:private initializer nil)

(defn set-init! [init]
  (alter-var-root #'initializer (constantly init)))

(defn- stop-system [s]
  (when s (component/stop s)))

(defn init []
  (if-let [init initializer]
    (do (alter-var-root #'system #(do (stop-system %) (init))) :ok)
    (throw (Error. "No system initializer function found."))))

(defn start []
  (alter-var-root #'system component/start)
  :started)

(defn stop []
  (alter-var-root #'system stop-system)
  :stopped)

(defn go []
  (init)
  (start))

(defn clear []
  (alter-var-root #'system #(do (stop-system %) nil))
  :ok)

(defn reset []
  (clear)
  (refresh :after 'net.reborg.fluorine.bootstrap/go))
