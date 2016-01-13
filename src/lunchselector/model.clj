(ns lunchselector.model
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]))

;; Zomato specific keys
(def user-key "5ef7ba0cfc27725aefcd7fb20014485e")
(def api-uri "https://developers.zomato.com/api/v2.1/")
(def entity_id 4) ;; Bangalore entity id

;; Nilenso Office lat-long
(def latitude 12.981860)
(def longitude 77.638664)

;; API to fetch data from Zomato
(defn get-categories []
  (client/get (str api-uri "categories")
              {:headers {"user_key" user-key}
               :debug true
               :debug-body true
               :save-request? true}))

(defn- fetch-restaurants []
  "Fetch the list of restaurants closest to our location"
  (client/get (str api-uri "search")
              {:accept :json
               :headers {"user_key" user-key}
               :query-params {
                              "lat" latitude
                              "lon" longitude
                              "entity_id" entity_id
                              "entity_type" "city"
                              "sort" "real_distance"
                              "order" "desc"
                              "start" 0
                              "count" 40}}))

(defn- get-rest-name [restaurant]
  (:name (:restaurant restaurant)))

(defn- get-rest-rating [restaurant]
  (:aggregate_rating (:user_rating restaurant)))

(defn get-restaurants []
  (loop [restaurant-list (:restaurants (cheshire/parse-string (:body (fetch-restaurants)) true))
         resta ()]
    (if (empty? restaurant-list)
      resta
      (recur (rest restaurant-list)
             (conj resta (get-rest-name (first restaurant-list)))))))
