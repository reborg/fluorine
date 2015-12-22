(ns net.reborg.fluorine
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response file-response content-type]]))

(defroutes app-routes
  (route/resources "/")
  (GET "/*" {{path :*} :params}
       (if-let [resp "resp"]
         (do
           (log/info (str "Serving: " path))
           resp)))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in api-defaults [:params :multipart] true))
      (wrap-json-response)))
