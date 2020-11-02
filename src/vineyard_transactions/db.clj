(ns vineyard-transactions.db
  (:require
   [clojure.java.jdbc :as sql]
   [java-jdbc.ddl :as ddl]
   [clojure.tools.logging :as log]
   [vineyard-transactions.util :as util]
   [buddy.core.hash :as hash]
   [environ.core :refer [env]]))


;; Create two url/db for testing and for production
(defonce url
  (or (env :db-url)
      "//127.0.0.1:8889/vineyard"))

(defonce db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1:8889/vineyard"
   :user "root"
   :password "root"
   :default-time-zone "+1"
   :server-timezone "UTC"})



(defn create-users-table
  "Create users table" []
  (try
    (let [res (sql/db-do-commands db-spec
                                  (sql/create-table-ddl   :users
                                                          [[:id :serial "PRIMARY KEY"]
                                                           [:user_name "varchar(32)"]
                                                           [:password "varchar(255)"]
                                                           [:email "varchar(32) UNIQUE NOT NULL"]
                                                           [:first_name "varchar(32)"]
                                                           [:last_name "varchar(32)"]
                                                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]]))]
    (if (= 0 (first res))
      (log/info  "Successfully created users table")
      (log/error res)))
    (catch Exception e (log/error (str "Exception: " (.getMessage e))))))

(defn create-products-table
  "Create products table" []
  (try
    (let [res (sql/db-do-commands db-spec
                                  (sql/create-table-ddl   :products
                                                          [[:id :serial "PRIMARY KEY"]
                                                           [:description "varchar(64)"]
                                                           [:name "varchar(255)"]
                                                           [:year_of_made :int]
                                                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]]))]
      (if (= 0 (first res))
        (log/info  "Successfully created products table")
        (log/error res)))
    (catch Exception e (log/error (str "Exception: " (.getMessage e))))))



(defn create-transaction-lines-table
  "Create transaction lines table" []
  (try
    (let [res (sql/db-do-commands db-spec
                                  (sql/create-table-ddl   :transaction_lines
                                                          [[:id :serial "PRIMARY KEY"]
                                                           [:description "varchar(254)"]
                                                           [:product "BIGINT"]
                                                           [:quantity "DECIMAL(12,6)"]
                                                           [:price "DECIMAL(12,6)"]
                                                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]]))]
      (if (= 0 (first res))
        (log/info  "Successfully created tranaction lines table")
        (log/error res)))
    (catch Exception e (log/error (str "Exception: " (.getMessage e))))))


(defn create-transactions-table
  "Create transactions table" []
  (try
    (let [res (sql/db-do-commands db-spec
                                  (sql/create-table-ddl   :transactions
                                                          [[:id :serial "PRIMARY KEY"]
                                                           [:description "varchar(254)"]
                                                           [:type :int]
                                                           [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]]))]
      (if (= 0 (first res))
        (log/info  "Successfully created transactions table")
        (log/error res)))
    (catch Exception e (log/error (str "Exception: " (.getMessage e))))))




(defn create-tables []
  (do
   (create-users-table)
   (create-products-table)
   (create-transactions-table)
   (create-transaction-lines-table)))

(defn drop-table-by-name
  "Drop a table by name"  [name]
  (try
    (sql/db-do-commands db-spec
                        (ddl/drop-table name))
    (catch Exception e (log/error (.getMessage e)))))

(defn insert-user
  "Insert a new user"
  [user-name password email first-name last-name ]
  (try
    (sql/insert! db-spec :users
                 {:id (util/generate-id)
                  :user_name user-name
                  :email email
                  :password (util/create-hash password)
                  :first_name first-name
                  :last_name last-name})
    (catch Exception e (log/error (.getMessage e)))))

(defn insert-product
  "Insert a new product"
  [description name year_of_made ]
  (try
    (sql/insert! db-spec :products
                 {:id (util/generate-id)
                  :description description
                  :name name
                  :year_of_made year_of_made})
    (catch Exception e (log/error (.getMessage e)))))


(defn insert-transaction
  "Insert a new transaction"
  [description type ]
  (try
    (sql/insert! db-spec :transactions
                 {:id (util/generate-id)
                  :description description
                  :type type})
    (catch Exception e (log/error (.getMessage e)))))

(defn set-type [type]
  (case type
    "IN" 1
    "OUT" 2
    "NOT-DEFINED" ))

(defn get-type [type]
  (case  type
    1 "IN"
    2 "OUT"
    "NOT-DEFINED" ))


(defn get-transactions []
  (try
      (into []
            (map (fn [{:keys [id description type created_at]}]
                   :key-word {:id id :description description :type type :created_at created_at})
                 (sql/query db-spec ["SELECT id, description, type, created_at FROM transactions"] {:row-fn util/convert-inst})))
      (catch Exception e (log/error (.getMessage e)))))

(defn get-products []
  (try
    (sql/query db-spec ["SELECT * FROM products"] {:row-fn util/convert-inst})
    (catch Exception e (log/error (.getMessage e)))))

(defn get-transaction [id]
  (try
    (into []
          (map (fn [{:keys [id trans-desc type line-id line-desc product-id quantity price product-desc product year_of_made created_at]}]
                 :key-word {:id id :trans-desc trans-desc :type type :line-id line-id :line-desc line-desc :product-id product-id :quantity quantity :price price :product-desc product-desc :product product :year-of-made year_of_made :created created_at})
           (sql/query db-spec
               ["SELECT t.id as 'id', t.description as 'trans-desc', t.type, l.id as 'line-id', l.description as 'line-desc', l.product as 'product-id', l.quantity, l.price, p.description as 'product-desc', p.name as 'product', p.year_of_made, t.created_at
                 FROM  transaction_lines l
                 INNER  JOIN products p
                 ON l.product = p.id
                 INNER  JOIN transactions t
                 WHERE  transaction_id = ? and t.id = l.transaction_id " id ]
               {:row-fn util/convert-inst})))
    (catch Exception e (log/error (.getMessage e)))))


(defn insert-new-transaction [transaction trans-lines t-con]
  (sql/with-db-transaction [t-con db-spec]

    (let [ arr-a (sql/query db-spec ["SELECT * FROM transactions"] {:row-fn util/convert-inst})
          arr-b (sql/query db-spec ["SELECT * FROM products"] {:row-fn util/convert-inst})]
      (println arr-a)
      (println "-----------------------")
      (println arr-b))))

(defn get-all [table]
  (try
    (sql/query db-spec [(str "SELECT * FROM " table)] {:row-fn util/convert-inst})
    (catch Exception e (log/error (.getMessage e)))))



(defn get-user-by-username [username]
  (filter (fn [x] (= (compare (:user_name x) username) 0)) (get-all "users")))

