(ns vineyard-transactions.core
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.util.response :refer [response content-type file-response resource-response]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
   [ring.middleware.reload :refer [wrap-reload ]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.nested-params :only [wrap-nested-params]]
   [compojure.core :refer [defroutes GET POST DELETE PUT]]
   [compojure.route :refer [not-found]]
   [clojure.tools.logging :as log]
   [vineyard-transactions.db :as db]
   [vineyard-transactions.controller :as ctrl]
   [environ.core :refer [env]]
   [clojure.java.io :as io]
   [clojure.pprint :refer :all])
  (:gen-class))

(def uri-list '("/" "/api/login"))

(defn in?
  [elm]
  (some #(= elm %) uri-list))

(defroutes routes
  (GET "/"  [] (ctrl/index))
  (POST "/api/login" req (ctrl/login  (get-in req [:body "username"]) (get-in req [:body "password"]))) 
  (POST "/api/logout" req (ctrl/logout (get (:headers req) "vineyard-token")))

  (GET "/api/transactions" [] (ctrl/get-transactions-list))
  (GET "/api/transactions/:id" [id] (ctrl/get-transaction id))
  (POST "/api/transactions" [transaction] (ctrl/insert-new-transaction transaction))
  (PUT "/api/transactions" [transaction] (ctrl/edit-transaction transaction))
  (DELETE "/api/transactions/:id" [id] (ctrl/delete-transaction id))


  (POST "/api/lines" [line] (ctrl/insert-new-line line))
  (DELETE "/api/lines/:id" [id] (ctrl/delete-line id))

  (POST "/api/products" [product] (ctrl/insert-product product))
  (DELETE "/api/products/:id" [id] (ctrl/delete-product id))
  (GET "/api/products" [] (ctrl/get-products))

  (GET "/file" [] (ctrl/test-request))

  (not-found (ctrl/not-found)))


(defn wrap-auth [handler]
  (fn [request]
    (if (in? (:uri request))
      (handler request)
      (let [token (get (:headers request) "vineyard-token")]
        (if (nil? token)
          (ctrl/unauthorized)
          (if (ctrl/check-token token)
            (handler request)
            (ctrl/unauthorized)))))))

(def app  (-> #'routes
              wrap-auth
              wrap-keyword-params
              wrap-params
              wrap-json-body
              wrap-json-response
              wrap-reload))

(defn -main
  [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (do
     (db/create-tables)
     (log/info (str "Start server on port: " port)))
    (jetty/run-jetty app
                     {:port (Integer. port)})))

