(ns ^:skip-aot fluorine-client.core
  (:gen-class)
  (:require [net.reborg.fluorine-client :as c]))

(defn forever []
  (while true
    (Thread/sleep 3000)
    (println "client-alive: config keys" (keys @c/cfg))))

(defn -main [& args]
  (c/attach "/apps/test-json" "fluorine1,fluorine2,fluorine3,fluorine4" 10101 :test-json)
  (.start (Thread. forever)))
