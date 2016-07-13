(ns net.reborg.fluorine.fs
  (:refer-clojure :exclude [read])
  (:require
    [net.reborg.fluorine.config :refer [fluorine-root]]
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [net.reborg.fluorine.data :as data]
    [net.reborg.fluorine.metadata :as metadata]
    ))

(defn with-file
  "Creates a temporary file with the given content
  and removes it right after the block. Used for testing."
  [fname fbody f]
  (do
    (io/make-parents fname)
    (spit fname fbody)
    (try
      (f)
      (catch Exception e nil)
      (finally (io/delete-file fname)))))

(defn abs
  "Append path to the current user.dir creating
  an absolute path."
  [path]
  (str (System/getProperty "user.dir") path))

(defn file? [^java.io.File path]
  (.isFile path))

(defn keywordize [^java.io.File file]
    (keyword (first (clojure.string/split (.getName file) #"\."))))

(defn read
  "Reads the configuration tree recursively starting from
  the configured :fluorine-root and appending 'path'."
  [path]
  (let [f (io/file (str (fluorine-root) path))
        k (keywordize f)]
    (if (file? f)
      (metadata/new-map k (data/unmarshall f))
      (->> (.list f)
           (map (fn [sub] (metadata/new-map k (read (str path "/" sub)))))
           (remove #(empty? (k %)))
           metadata/merge-with-meta))))
