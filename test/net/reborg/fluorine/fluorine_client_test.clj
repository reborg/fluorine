(ns net.reborg.fluorine.fluorine-client-test
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [net.reborg.fluorine.config :as c]
            [net.reborg.fluorine-client :as fc]))

(facts "parsing hosts and ports"
       (fact "fallback to pure defaults"
             (fc/parse-hosts) => [{:host "127.0.0.1" :port 10101}]
             (fc/parse-hosts "somehost") => [{:host "somehost" :port 10101}]
             (fc/parse-hosts "somehost" "aport") => [{:host "somehost" :port "aport"}])
       (fact "multiple hosts, single port"
             (fc/parse-hosts "host1,host2") => [{:host "host1" :port 10101} {:host "host2" :port 10101}]
             (fc/parse-hosts "host1,host2" 20202) => [{:host "host1" :port 20202} {:host "host2" :port 20202}]))

(facts "first connection"
       (fact "should call only the first host in the list"
             (let [acc (atom {})
                   res (fc/round-robin
                         (fn [host port]
                           (swap! acc assoc host port)) "a,b" 888)]
               @acc => {"a" 888}))
       (fact "when returning nil, it goes through all the list"
             (let [acc (atom {})
                   res (fc/round-robin
                         (fn [host port]
                           (do (swap! acc assoc host port) nil)) "a,b,c" 888)]
               @acc => {"a" 888 "b" 888 "c" 888})))
