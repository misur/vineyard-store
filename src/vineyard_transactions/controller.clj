(ns vineyard-transactions.controller
  (:require
   [clojure.tools.logging :as log]
   [clojure.data.csv :as csv]
   [semantic-csv.core :as sc]
   [clojure.java.io :as io]
   [ring.util.response :refer [response]]
   [vineyard-transactions.web_util :as web-util]
   [vineyard-transactions.db :as db]
   [vineyard-transactions.util :as util]
   [vineyard-transactions.service :as sr]))

(defn index []
  (web-util/web-response 200 "Vineyard transactions REST Api"))

(defn not-found []
  (web-util/web-response 404 "Request does not exist"))

(defn unauthorized []
  (web-util/web-response 401 "Unauthorized request"))

(defn check-token [token]
  (if (sr/is-logged token)
    true
    false))

(defn login [username password]
  (try
    (do
     (sr/login username password)
     (if (sr/is-logged (util/create-hash password))
       (web-util/web-response 200 (util/create-hash password))
       (web-util/web-response 404 (str "Wrong username " username " or password "  password " " (sr/is-logged password)))))
    (catch Exception e (web-util/web-response 404  (.getMessage e)))))

(defn logout [token]
  (try
    (do
      (sr/logout token)
      (web-util/web-response 200 "Successfully logged out"))
    (catch Exception e (web-util/web-response 404 (.getMessage e)))))

(defn get-transactions-list []
  (web-util/web-response 200 (sr/get-transactions)))


(defn get-transaction [id]
  (web-util/web-response 200 (db/get-transaction id)))



#_TODO

(defn insert-new-transaction [transaction]
  (web-util/web-response 200 "Inserted a new transaction"))

(defn delete-transaction [id]
  (web-util/web-response 200 "Deleted a transaction"))

(defn edit-transaction [transaction id]
  (web-util/web-response 200 "Edited a transaction"))



(defn insert-new-line [line]
  (web-util/web-response 200 "Inserted transaction line"))

(defn delete-line [id]
  (web-util/web-response 200 "Deleted transaction line"))



(defn insert-product [product]
  (web-util/web-response 200 "Ineserted product"))

(defn delete-product [product]
  (web-util/web-response 200 "Deleted a product"))


(defn get-products []
  (web-util/web-response 200 "List of products"))

;; File export
(defn test-request []
  {:status 200
   :headers {"Content-Type" "application/octet-stream"
             "Content-Disposition" "attachment; filename=test.csv"}
   :body (io/input-stream (io/resource "public/test.csv"))})


#_(with-open [out-file (io/writer "test.csv")]
  (->> (db/get-transactions)
       #_(sc/cast-with {:this #(-> % float str)})
       sc/vectorize
       (csv/write-csv out-file)))

