(ns vineyard-transactions.util
  (:require
   [buddy.core.hash :as hash]
   [buddy.core.codecs :refer :all]))

(defn generate-id []
  (* (quot (System/currentTimeMillis) 1000) (rand-int 10000)))

(defn convert-inst [row]
  (assoc row :created_at (.toString (:created_at row))))


(defn create-hash [text]
  (-> (hash/sha256 text) (bytes->hex)))

