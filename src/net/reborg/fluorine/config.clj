(ns net.reborg.fluorine.config
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.edn :as edn])
  (:import [java.net InetAddress URL]))

(defn- load-edn [fname]
  (some->> fname
           (io/resource)
           (slurp)
           (edn/read-string)))

(defn- which-env []
  (or (System/getenv "FLUORINE_ENV")
      (System/getProperty "fluorine.env")
      "local"))

(defn- user-cfg-location [main-cfg]
  (str (System/getProperty "user.dir") "/"
       (or (:user-config main-cfg) "test/fluorine.config")))

(defn- config []
  (let [main-cfg (load-edn (str "config." (which-env)))
        user-cfg (edn/read-string (slurp (user-cfg-location main-cfg)))]
    (merge main-cfg user-cfg)))

(defn nrepl-port [] (:nrepl-port (config)))
(defn fluorine-port [] (:fluorine-port (config)))

(defn pretty [x]
  (with-out-str (pp/write x)))

(defn debug []
  "Returns the details about current ENV configuration."
  (str "'" (which-env) "' environment:\n"
       (pretty (config))))
