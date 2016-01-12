(ns lunchselector.fetch
  (:require [clj-http.client :as client]))

(def user-key "5ef7ba0cfc27725aefcd7fb20014485e")

(def api-uri "https://developers.zomato.com/api/v2.1/")

(defn get-categories []
  (client/get (str api-uri "categories")
              {:headers {"user_key" user-key}}))
