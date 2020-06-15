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


(def data [{:product "brendy"
            :year 1999
            :price 7.30}
           {:product "red vine"
            :year 2000
            :price 20.33}
           {:product "white vine"
            :year 2002
            :price 10.22}])




(with-open [writer (io/writer "out-file.csv")
            headers (into [](map #(name %)(keys (first data))))
            values (into [] (map #(vals %) data))
            con-value (map #(str % ) [("22" "222") ("222" "222")])
            exp (concat headers values)]

  (csv/write-csv writer
                 (concat
                   headers
                   values)))



(map #(map  (fn [x ] str  ) %) [["aa" "bbb"] ["aaa" "ccc"] ["vvv" "eee"]])

(def data1 [{:this 1, :that "2", :more "stuff"}
           {:this 2, :that "3", :more "other yeah"}])

(with-open [out-file (io/writer "test.csv")]
  (->> data1
       (sc/cast-with {:this #(-> % float str)})
       sc/vectorize
       (csv/write-csv out-file)))
