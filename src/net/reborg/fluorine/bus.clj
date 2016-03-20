(ns net.reborg.fluorine.bus
  (:require
    [manifold.bus :as b]
    [manifold.stream :as s]
    [net.reborg.fluorine.bootstrap :as boot]
    [clojure.tools.logging :as log]
    ))

(def clients (atom {}))

(defn- serialize [x]
  (with-out-str (clojure.pprint/write x)))

(defn start []
  (reset! clients {})
  (b/event-bus))

(defn stop
  "Close all the streams in the bus, throw away local
  client cache."
  [bus]
  (doseq [s (b/topic->subscribers (:bus bus))] (s/close! s))
  (alter-var-root #'clients (constantly nil)))

(defn- dispatch-fn [bus]
  (fn [event]
    (b/publish! bus
                (:channel event)
                (serialize (:msg event)))))

(defn init!
  "The bus needs to be bootstrapped after the change
  stream and the bus have been built."
  [bus changes]
  (s/consume (dispatch-fn bus) changes))

(defn subscribe!
  "Streams in the bus already associated with this ip-path pair needs to be
  closed because if not messages will be duplicated.
  Returns true if this client was never seen before. False otherwise."
  [conn ip path]
  (log/warn "reaction request" [ip path] ". Known clients:" (keys @clients))
  (let [bus (:bus boot/system)
        k [ip path]
        old-stream (@clients k)
        new-stream (b/subscribe bus path)]
    (when old-stream
      (log/warn k "was already connected, closing old.")
      (s/close! old-stream))
    (s/connect new-stream conn)
    (swap! clients assoc k new-stream)
    (not old-stream)))
