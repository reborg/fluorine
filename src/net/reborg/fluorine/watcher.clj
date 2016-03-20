(ns net.reborg.fluorine.watcher
  (:require
    [net.reborg.fluorine.bootstrap :refer [system]]
    [net.reborg.fluorine.fs :as fs]
    [clojure.tools.logging :as log]
    [clojure-watch.core :refer [start-watch]]
    [net.reborg.fluorine.config :as c]
    [manifold.stream :as s]
    ))

(def ^:private watchers (atom {}))

(defn start []
  (reset! watchers {}))

(defn stop
  "Just invoke the watcher to stop." []
  (when watchers
    (doseq [[k f-watch] @watchers] (f-watch))))

(defn- log-watcher [ip path]
  (log/warn (format "%s started watching %s" ip path)))

(defn- send-event [ip path event fname]
  (log/warn (format "%s changed. Sending cfg to %s" fname [ip path]))
  @(s/put! (:changes system) {:channel path :msg (fs/read path)}))

(defn register!
  "Only create a new watcher if none exists already for [ip path] key."
  [ip path]
  (when-not (@watchers [ip path])
    (swap! watchers assoc [ip path]
           (start-watch
             [{:path (str (c/fluorine-root) path)
               :event-types [:create :modify :delete]
               :bootstrap (partial log-watcher ip)
               :callback (partial send-event ip path)
               :options {:recursive true}}]))))
