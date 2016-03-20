(ns net.reborg.fluorine.watcher
  (:require
    [net.reborg.fluorine.bootstrap :refer [system]]
    [net.reborg.fluorine.fs :as fs]
    [clojure.tools.logging :as log]
    [clojure-watch.core :refer [start-watch]]
    [net.reborg.fluorine.config :as c]
    [manifold.stream :as s]
    ))

(def watchers (atom {}))

(defn init []
  (reset! watchers {}))

(defn teardown [watchers]
  (when watchers
    (doall
      (map (fn [[k v]] (v)) @watchers))))

(defn- log-watcher [from path]
  (log/warn (format "%s started watching %s" from path)))

(defn- send-event [from path event fname]
  (log/warn (format "%s changed. Sending cfg to %s" fname [from path]))
  @(s/put! (:changes system) {:channel path :msg (fs/read path)}))

(defn register-watcher! [path from]
  (let [watcher (start-watch
                  [{:path (str (c/fluorine-root) path)
                    :event-types [:create :modify :delete]
                    :bootstrap (partial log-watcher from)
                    :callback (partial send-event from path)
                    :options {:recursive true}}])]
    (swap! watchers assoc path watcher)))

