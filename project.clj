(defproject net.reborg/fluorine "0.0.9"
  :description "Distributed configuration for Clojure"
  :url "https://github.com/reborg/fluorine"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.8.0"]

                 ;; logging
                 [ch.qos.logback/logback-classic "1.1.3" :exclusions [org.slf4j/slf4j-api]]
                 [ch.qos.logback/logback-access "1.1.3"]
                 [ch.qos.logback/logback-core "1.1.3"]
                 [org.slf4j/slf4j-api "1.7.12"]
                 [org.clojure/tools.logging "0.3.1"]

                 ;; components
                 [com.stuartsierra/component "0.3.0"]
                 [org.clojure/tools.nrepl "0.2.11"]
                 [org.clojure/tools.namespace "0.2.11"]

                 ;; aleph
                 [aleph "0.4.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [manifold "0.1.1"]
                 [gloss "0.2.5"]
                 [compojure "1.4.0"]

                 ;; other
                 [clojure-watch "0.1.11"]
                 [cheshire "5.2.0"]
                 ]
  :uberjar-name "fluorine.jar"
  :repl-options {:init-ns user
                 :init (do (require 'midje.repl) (midje.repl/autotest))}
  :profiles {:uberjar {:main net.reborg.fluorine.system
                       :aot :all}
             :dev {:plugins [[lein-midje "3.1.3"]]
                   :dependencies [[midje "1.6.3"]]
                   :source-paths ["dev"]}}
  :jvm-opts ~(vec (map (fn [[p v]] (str "-D" (name p) "=" v))
                       {:java.awt.headless "true"
                        :log.dir "logs"})))
