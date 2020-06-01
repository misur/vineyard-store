(ns vineyard-transactions.web_util
  (:require
   [clojure.data.json :as json]))


(defn web-response
  "Retur web response "
  [code message]
  {:status code
   :body (json/write-str message )
   :headers {"content-type" "application/json"
             "Access-Control-Allow-Origin" "*"
             "Access-Control-Allow-Headers" "Content-Type"}})


