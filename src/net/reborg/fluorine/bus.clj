(ns net.reborg.fluorine.bus
  (:require
    [manifold.bus :as b]
    [manifold.stream :as s]
    [net.reborg.fluorine.bootstrap :as boot]
    [clojure.tools.logging :as log]
    ))

(def ^:private clients (atom {}))

(defn start [] (b/event-bus))

(defn stop
  "Close all the streams in the bus, throw away local
  client cache."
  [bus]
  (when bus
    (doseq [s (first (vals (b/topic->subscribers bus)))]
      (s/close! s))
    (reset! clients {})))

(defn- dispatch-fn [bus]
  (fn [event]
    (b/publish! bus
                (:channel event)
                (:msg event))))

(defn init!
  "The bus needs to be bootstrapped after the change
  stream and the bus have been built."
  [bus changes]
  (s/consume (dispatch-fn bus) changes))

(defn subscribe!
  "Streams already associated with this ip-path pair needs to be
  closed to avoid message dupes. The bus automatically removes the stream
  when the stream is closed."
  [conn ip path]
  (log/warn "reaction request" [ip path] "known clients:" (keys @clients))
  (let [bus (:bus boot/system)
        k [ip path]
        old-stream (@clients k)
        new-stream (b/subscribe bus path)]
    (when old-stream
      (log/warn k "was already connected, closing old first.")
      (s/close! old-stream))
    (s/connect new-stream conn)
    (swap! clients assoc k new-stream)))
