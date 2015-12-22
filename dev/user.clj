(ns user
  (:require [clojure.test]
            [net.reborg.fluorine.system]
            [net.reborg.fluorine.bootstrap :as b :refer [system stop start]]))

(b/set-init! #'net.reborg.fluorine.system/create-system)

(defn reset []
  (binding [clojure.test/*load-tests* false] (b/reset)))
