(ns vineyard-transactions.controller
  (:require
   [clojure.tools.logging :as log]
   [clojure.data.csv :as csv]
   [semantic-csv.core :as sc]
   [clojure.java.io :as io]
   [ring.util.response :refer [response]]
   [vineyard-transactions.web_util :as web-util]
   [vineyard-transactions.db :as db]))

(defn index []
  (web-util/web-response 200 "Vineyard transactions REST Api"))

(defn not-found []
  (web-util/web-response 404 "Request does not exist"))


(defn test-request []
  (response {:response "Hello world"}))



(defn get-transactions-list []
  (web-util/web-response 200 (db/get-transactions)))


(defn get-transaction [id]
  (web-util/web-response 200 (db/get-transaction id)))



#_(with-open [out-file (io/writer "test.csv")]
  (->> (db/get-transactions)
       #_(sc/cast-with {:this #(-> % float str)})
       sc/vectorize
       (csv/write-csv out-file)))
