(ns net.reborg.fluorine.fs
  (:refer-clojure :exclude [read])
  (:require
    [net.reborg.fluorine.config :refer [fluorine-root]]
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    ))

(defn abs
  "Append path to the current user.dir creating
  an absolute path."
  [path]
  (str (System/getProperty "user.dir") path))

(defn file? [^java.io.File path]
  (.isFile path))

(defn keywordize [^java.io.File path]
    (keyword (.getName path)))

(defn read
  "Reads the configuration tree recursively starting from
  the configured :fluorine-root and appending 'path'."
  [path]
  (let [f (io/file (str (fluorine-root) path))
        k (keywordize f)]
    (log/debug (.getPath f) "file?" (file? f) "content" (map str (.list f)))
    (if (file? f)
      {k (edn/read-string (slurp f))}
      (->> (.list f)
           (map (fn [sub] {k (read (str path "/" sub))}))
           (remove #(empty? (k %)))
           (apply (partial merge-with into))))))
