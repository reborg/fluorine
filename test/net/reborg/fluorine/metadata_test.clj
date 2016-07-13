(ns net.reborg.fluorine.metadata-test
  (:require [midje.sweet :refer :all]
            [net.reborg.fluorine.metadata :as metadata]))

(facts "propagating the format"
       (fact "destination object is untouched"
             (metadata/propagate-format {:a "a"} {:b "b"}) => {:b "b"})
       (fact "creating new maps"
             (metadata/new-map :k {:b "b"}) => {:k {:b "b"}}
             (meta (metadata/new-map :k ^{:format 1} {:b "b"})) => {:format 1})
       (fact "meta are propagated"
             (:format (meta (metadata/propagate-format (vary-meta {} assoc :format "a") {}))) => "a")
       (fact "into object is untouched"
             (metadata/into-with-meta {:a "a"} {:b "b"}) => {:a "a" :b "b"})
       (fact "into object merges meta"
             (meta (metadata/into-with-meta ^{:m1 1} {:a "a"} ^{:m2 2} {:b "b"})) => {:m1 1 :m2 2})
       (fact "merged object is untouched"
             (metadata/merge-with-meta
               [^{:format :json} {:testdata [{:fold1 {:test2 {:c 3 :d 4}}}]}
                ^{:format :json} {:testdata {:test1 {:a 1 :b 2}}}]) =>
             {:testdata [{:fold1 {:test2 {:c 3, :d 4}}} [:test1 {:a 1, :b 2}]]})
       (fact "merging meta on merge"
             (meta (metadata/merge-with-meta
               [^{:format :json} {:testdata [{:fold1 {:test2 {:c 3 :d 4}}}]}
                ^{:format :json} {:testdata {:test1 {:a 1 :b 2}}}])) => {:format :json})
             (meta (metadata/merge-with-meta
               [^{:a :edn} {:testdata [{:fold1 {:test2 {:c 3 :d 4}}}]}
                ^{:b :json} {:testdata {:test1 {:a 1 :b 2}}}])) => {:a :edn})
