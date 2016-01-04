(ns net.reborg.fluorine
  (:require
    [net.reborg.fluorine.bootstrap :refer [system]]
    [net.reborg.fluorine.fs :as fs]
    [clojure.tools.logging :as log]
    [compojure.core :as compojure :refer [GET]]
    [ring.middleware.params :as params]
    [compojure.route :as route]
    [aleph.http :as http]
    [clojure-watch.core :refer [start-watch]]
    [net.reborg.fluorine.config :as c]
    [manifold.stream :as s]
    [manifold.deferred :as d]
    [manifold.bus :as b]
    ))

(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})

(defn- serialize [x]
  (with-out-str (clojure.pprint/write x)))

(defn- push-config!
  "Send current config reading down a connected client."
  [conn path]
  (s/put! conn (serialize (fs/read path))))

(defn- register-watcher [path from]
  (let [watcher (start-watch [{:path (str (c/fluorine-root) path)
                               :event-types [:create :modify :delete]
                               :bootstrap (fn [path]
                                            (log/warn (format "%s started watching %s" from path)))
                               :callback (fn [event filename]
                                           (do
                                             (log/warn (format "file %s changed. firing watcher for %s" filename from))
                                             @(s/put! (:changes system) {:channel path :msg (fs/read path)})))
                               :options {:recursive true}}])]
    (swap! (:watchers system) assoc path watcher)))

(defn connection-handler
  [path req]
  (d/let-flow [conn (d/catch (http/websocket-connection req) (constantly nil))]
    (if-not conn
      non-websocket-request
      (do
        (log/warn "reaction request from" (:remote-addr req) "for path" path)
        (push-config! conn path)
        (s/connect (b/subscribe (:bus system) path) conn)
        (register-watcher path (:remote-addr req))))))

(def handler
  (params/wrap-params
    (compojure/routes
      (GET "*" {{path :*} :params}
           (partial connection-handler path))
      (route/not-found "No such page."))))

(defn- debug
  "Just here for a one off send of a sample config down connected clients."
  []
  @(s/put! (:changes system) {:channel "apps/clj-fe" :msg {:a "hello"}}))
