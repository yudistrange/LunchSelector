(ns lunchselector.model
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [lunchselector.db :as ldb]
            [lunchselector.utils :as lu]))

;; Zomato specific keys
(def user-key "5ef7ba0cfc27725aefcd7fb20014485e")
(def api-uri "https://developers.zomato.com/api/v2.1/")
(def entity_id 4) ;; Bangalore entity id

;; Nilenso Office lat-long
(def latitude 12.981860)
(def longitude 77.638664)

(def zomato "Zomato") ;; Used for adding restaurants automatically

;; API to fetch data from Zomato
(defn get-categories []
  (client/get (str api-uri "categories")
              {:headers {"user_key" user-key}
               :save-request? true}))

(defn- fetch-restaurants [keyword]
  "Fetch the list of restaurants closest to our location"
  (client/get (str api-uri "search")
              {:accept :json
               :headers {"user_key" user-key}
               :query-params {"q" keyword
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

(defn online-restaurants [keyword]
  (loop [restaurant-list (:restaurants
                          (lu/parse-to-clj-map
                           (fetch-restaurants keyword)))
         resta ()]
    (if (empty? restaurant-list)
      resta
      (recur (rest restaurant-list)
             (conj resta (get-rest-name (first restaurant-list)))))))

(defn- cast-new-vote? [email restaurant]
  (let [user-id (:user_id (first (ldb/fetch-user-id email)))
        rest-id (:rest_id (first (ldb/fetch-restaurant-id restaurant)))]
    (if (empty? (ldb/check-votes user-id rest-id))
      true
      false)))

(defn submit-votes [email votes]
  (if (coll? votes)
    (doseq [x votes]
      (ldb/cast-vote-safe {:email email :restaurant x}))
    (ldb/cast-vote-safe {:email email :restaurant votes})))

(defn submit-users [user email]
  (ldb/add-user-safe {:name user :email email}))

(defn submit-restaurants [restaurant added-by]
  (if (coll? restaurant)
    (doseq [x restaurant] (ldb/add-restaurant-safe x added-by))
    (ldb/add-restaurant-safe restaurant added-by)))

(defn submit-online-restaurants [rest]
  (submit-restaurants rest zomato))

(defn offline-restaurants []
  (let [offline-list (ldb/fetch-offline-restaurants)]
    (lu/get-restaurant-name-from-db offline-list)))

(defn my-votes [email]
  (ldb/fetch-my-votes email))

(defn votes-today []
  (ldb/fetch-votes-for-today))

(defn top-restaurants []
  (ldb/fetch-popular-restaurants))
