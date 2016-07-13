(ns net.reborg.fluorine.data
  (:require
    [net.reborg.fluorine.config :refer [fluorine-root]]
    [clojure.tools.logging :as log]
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    [cheshire.core :as json]
    ))

(defn- fname+ext [file]
  (when file
    (let [fname (.getPath file)
          dot (.lastIndexOf fname ".")]
      [(.substring fname 0 dot)
       (.substring fname (inc dot))])))

(defn ext [file]
  (keyword (last (fname+ext file))))

(defn fname [file]
  (first (fname+ext file)))

(defprotocol ReadContent (read-content [this]))
(extend-protocol ReadContent
  java.lang.String
  (read-content [this]
    (when this (.substring this (inc (.indexOf this "\n")))))
  java.io.File
  (read-content [this] (slurp this)))

(defprotocol UnmarshallDispatch (unmarshall-dispatch [this]))
(extend-protocol UnmarshallDispatch
  java.lang.String
  (unmarshall-dispatch [this] (keyword (last (re-find #"^RAW=(.*)" this))))
  java.io.File
  (unmarshall-dispatch [this] (ext this)))

(defmulti unmarshall
  unmarshall-dispatch)

(defmethod unmarshall :json
  [raw]
  (vary-meta
    (json/decode (read-content raw) true)
    assoc :format :json))

(defmethod unmarshall :edn
  [raw]
  (vary-meta
    (edn/read-string (read-content raw))
    assoc :format :edn))

(defmethod unmarshall :txt
  [raw]
  (vary-meta {:content (read-content raw)} assoc :format :txt))

(defmethod unmarshall :default
  [raw]
  (vary-meta {:content (read-content raw)} assoc :format :default))

(defmulti marshall
  (fn [data] (:format (meta data))))

(defmethod marshall :json [data]
  (str "RAW=json\n" (json/generate-string data)))

(defmethod marshall :edn [data]
  (str "RAW=edn\n" (with-out-str (clojure.pprint/write data))))

(defmethod marshall :txt [data]
  (str "RAW=txt\n" (:content data)))

(defmethod marshall :default [data]
  (str "RAW=default\n" (:content data)))
