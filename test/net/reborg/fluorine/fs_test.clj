(ns net.reborg.fluorine.fs-test
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [net.reborg.fluorine.config :as c]
            [net.reborg.fluorine.fs :as fs]))

(facts "conversion of files to clojure maps"
       (fact "the key is the name of the file"
             (fs/with-file (fs/abs "/test/testdata/test.edn") "{:a 1 :b 2}"
               #(do (fs/read "/testdata/test.edn") => {:test {:a 1 :b 2}}
                    (provided (c/fluorine-root) => (fs/abs "/test")))))
       (fact "multiple files in folder means multiple keys"
             (fs/with-file (fs/abs "/test/testdata/test1.edn") "{:a 1 :b 2}"
               (fn []
                 (fs/with-file (fs/abs "/test/testdata/test2.edn") "{:c 3 :d 4}"
                   (fn []
                     (fs/read "/testdata") => {:testdata {:test1 {:a 1 :b 2}
                                                          :test2 {:c 3 :d 4}}}
                     (provided (c/fluorine-root) => (fs/abs "/test")))))))
       (fact "nested files"
             (fs/with-file (fs/abs "/test/testdata/test1.edn") "{:a 1 :b 2}"
               (fn []
                 (fs/with-file (fs/abs "/test/testdata/fold1/test2.edn") "{:c 3 :d 4}"
                   (fn []
                     (fs/read "/testdata") => {:testdata {:test1 {:a 1 :b 2} :fold1 {:test2 {:c 3 :d 4}}}}
                     (provided (c/fluorine-root) => (fs/abs "/test")))))))
       (fact "it maintains meta"
             (fs/with-file (fs/abs "/test/testdata/test4.edn") "{:a 1 :b 2}"
               (fn []
                 (fs/with-file (fs/abs "/test/testdata/fold1/test5.edn") "{:c 3 :d 4}"
                   (fn []
                     (meta (fs/read "/testdata")) => {:format :edn}
                     (provided (c/fluorine-root) => (fs/abs "/test"))))))))
