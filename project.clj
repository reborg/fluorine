(defproject fluorine "0.0.1"
  :description "Distributed configuration for Clojure"
  :url "https://github.com/reborg/fluorine"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.8.0-RC4"]

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

                 ;; other stuff

                 ;; web
                 [http-kit "2.1.19"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
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
