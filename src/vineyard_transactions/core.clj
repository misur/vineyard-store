(ns vineyard-transactions.core
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.util.response :refer [response]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [ring.middleware.reload :refer [wrap-reload ]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.nested-params :only [wrap-nested-params]]
   [compojure.core :refer [defroutes GET POST DELETE]]
   [compojure.route :refer [not-found]]
   [clojure.tools.logging :as log]
   [vineyard-transactions.db :as db]
   [vineyard-transactions.web_util :as web-util]
   [vineyard-transactions.controller :as ctrl])
  (:gen-class))


(defroutes routes
  (GET "/"  [] (ctrl/index))
  (GET "/api/transactions" [] (ctrl/get-transactions-list))
  (GET "/api/transactions/:id" [id] (ctrl/get-transaction id))
  (not-found (ctrl/not-found)))



(def app  (-> #'routes
              wrap-keyword-params
              wrap-params
              wrap-json-body
              wrap-json-response
              wrap-reload))

(defn -main
  [& port-number]
  (let [port (if (nil? port-number) (System/getenv "PORT") port-number)]
    (do
     (db/create-tables)
     (log/info (str "Start server on port: " port)))
    (jetty/run-jetty app
                     {:port (Integer. port)})))
