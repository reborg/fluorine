(ns net.reborg.fluorine.metadata
  )

(defn propagate-format [origin dest]
  (vary-meta dest assoc :format (:format (meta origin))))

(defn new-map [k m]
  (propagate-format m {k m}))

(defn into-with-meta [to from]
  (let [metas (into (or (meta to) {}) (or (meta from) {}))]
    (with-meta (into to from) metas)))

(defn merge-with-meta
  "TODO: not huge problem for now, but this only
  picks up the meta from the first map in the list.
  It is a fair assumption for one app config uses the same format
  for all the config files."
  [coll]
  (apply (partial merge-with into-with-meta) coll))
