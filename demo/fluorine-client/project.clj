(defproject fluorine-client "0.0.1"
  :description "Fluorine demo app"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.reborg/fluorine "0.0.8"]
                 ]
  :uberjar-name "fluorine-client.jar"
  :profiles {:uberjar {:main fluorine-client.core
                       :aot :all}})
