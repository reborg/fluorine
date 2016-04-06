(ns net.reborg.fluorine
  (:require
    [net.reborg.fluorine.bootstrap :refer [system]]
    [net.reborg.fluorine.fs :as fs]
    [clojure.tools.logging :as log]
    [compojure.core :as compojure :refer [GET]]
    [ring.middleware.params :as params]
    [compojure.route :as route]
    [aleph.http :as http]
    [net.reborg.fluorine.config :as c]
    [net.reborg.fluorine.bus :as bus]
    [net.reborg.fluorine.watcher :as watcher]
    [manifold.stream :as s]
    [manifold.deferred :as d]
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

(defn- register-client [conn ip path]
  (bus/subscribe! conn ip path)
  (s/consume #(log/info (format "%s %s" % ip)) conn)
  (s/on-closed conn #(log/warn (format "closed connection from %s" ip)))
  (watcher/register! ip path)
  (push-config! conn path)
  {:connected true})

(defn connection-handler
  [path {:keys [remote-addr] :as req}]
  (d/let-flow [conn (d/catch (http/websocket-connection req) (constantly nil))]
    (if conn
      (register-client conn remote-addr path)
      non-websocket-request)))

(def handler
  (params/wrap-params
    (compojure/routes
      (GET "*" {{path :*} :params} (partial connection-handler path))
      (route/not-found "No such page."))))

(defn- debug
  "Just here for a one off send of a sample config down connected clients."
  []
  @(s/put! (:changes system) {:channel "apps/clj-fe" :msg {:a "hello"}}))
