(ns net.reborg.fluorine.fs-test
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [net.reborg.fluorine.config :as c]
            [net.reborg.fluorine.fs :as fs]))

(defn with-file [fname fbody f]
  (do
     (io/make-parents fname)
     (spit fname fbody)
     (let [res (f)]
       (do (io/delete-file fname)
           res))))

(facts "conversion of files to clojure maps"
       (fact "the key is the name of the file"
             (with-file (fs/abs "/test/testdata/test") "{:a 1 :b 2}"
               #(do (fs/read "/testdata/test") => {:test {:a 1 :b 2}}
                    (provided (c/fluorine-root) => (fs/abs "/test")))))
       (fact "multiple files in folder means multiple keys"
             (with-file (fs/abs "/test/testdata/test1") "{:a 1 :b 2}"
               (fn []
                 (with-file (fs/abs "/test/testdata/test2") "{:c 3 :d 4}"
                   (fn []
                     (fs/read "/testdata") => {:testdata {:test1 {:a 1 :b 2}
                                                          :test2 {:c 3 :d 4}}}
                     (provided (c/fluorine-root) => (fs/abs "/test")))))))
       (fact "nested files"
             (with-file (fs/abs "/test/testdata/test1") "{:a 1 :b 2}"
               (fn []
                 (with-file (fs/abs "/test/testdata/fold1/test2") "{:c 3 :d 4}"
                   (fn []
                     (fs/read "/testdata") => {:testdata {:test1 {:a 1 :b 2}
                                                          :fold1 {:test2 {:c 3 :d 4}}}}
                     (provided (c/fluorine-root) => (fs/abs "/test"))))))))
