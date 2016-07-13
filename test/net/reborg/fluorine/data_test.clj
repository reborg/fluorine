(ns net.reborg.fluorine.data-test
  (:require [midje.sweet :refer :all]
            [net.reborg.fluorine.fs :as fs]
            [clojure.java.io :as io]
            [net.reborg.fluorine.data :as data]))

(defn- as-file [fpath]
  (io/file (fs/abs fpath)))

(facts "decomposing file names"
       (data/ext (as-file "/some.txt")) => :txt
       (data/fname (as-file "/some.txt")) => #"some")

(facts "unmarshall"
       (fact "distpatch on file extensions"
             (let [fbz (as-file "/test/testdata/unmarshall/file.baz")]
               (fs/with-file fbz "default" #(data/unmarshall fbz))) => {:content "default"}
             (let [ftxt (as-file "/test/testdata/unmarshall/file.txt")]
               (fs/with-file ftxt "plain" #(data/unmarshall ftxt))) => {:content "plain"}
             (let [fjson (as-file "/test/testdata/unmarshall/file.json")]
               (fs/with-file fjson "[1, 2, 3]" #(data/unmarshall fjson))) => [1 2 3]
             (let [fedn (as-file "/test/testdata/unmarshall/file.edn")]
               (fs/with-file fedn "{:a 1 :b 2}" #(data/unmarshall fedn))) => {:a 1 :b 2})
       (fact "dispatch on string special tag format"
             (data/unmarshall "RAW=json\n[1, 2]") => [1 2]
             (data/unmarshall "RAW=edn\n{1 2}") => {1 2}
             (data/unmarshall "RAW=txt\nanything") => {:content "anything"}
             (data/unmarshall "RAW=bogus\nanything") => {:content "anything"}
             (data/unmarshall "BOGUS=bogus\nanything") => {:content "anything"})
       (fact "retaining original format"
             (let [fbz (as-file "/test/testdata/unmarshall/file.baz")]
               (:format (meta (fs/with-file fbz "default" #(data/unmarshall fbz))))) => :default
             (let [ftxt (as-file "/test/testdata/unmarshall/file.txt")]
               (:format (meta (fs/with-file ftxt "plain" #(data/unmarshall ftxt))))) => :txt
             (let [fjson (as-file "/test/testdata/unmarshall/file.json")]
               (:format (meta (fs/with-file fjson "[]" #(data/unmarshall fjson))))) => :json
             (let [fedn (as-file "/test/testdata/unmarshall/file.edn")]
               (:format (meta (fs/with-file fedn "{}" #(data/unmarshall fedn))))) => :edn))

(facts "marshall"
       (fact "different formats"
             (data/marshall (vary-meta {:a "blah"} assoc :format :json)) => "RAW=json\n{\"a\":\"blah\"}"
             (data/marshall (vary-meta {:a "blah"} assoc :format :edn)) => "RAW=edn\n{:a \"blah\"}"
             (data/marshall (vary-meta {:content "whateva"} assoc :format :txt)) => "RAW=txt\nwhateva"
             (data/marshall (vary-meta {:content "whateva2"} assoc :format :default)) => "RAW=default\nwhateva2"))
