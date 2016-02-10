(ns lunchselector.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [lunchselector.utils :as utils]))

(def votes "votes")
(def users "users")
(def restaurants "restaurants")

(def db-path (fn [] (utils/get-config :database-path)))
(def db-protocol (fn [] (utils/get-config :database-subprotocol)))

(defn- db-spec []
  {:subprotocol (db-protocol)
   :subname (str "//" (db-path))})

(defn add-user
  "Adds user to the database in a transaction. However it doesnt check if the user
  is already present and will throw an exception"
  [user]
  (let [name (:name user)
        email (:email user)]
    (jdbc/insert! (db-spec)
                  (keyword users)
                  {:name name :email email :timestamp (coerce/to-sql-time (time/now))})))

(defn fetch-user-id
  "Fetches the user-id of the user with given email address"
  [email]
  (jdbc/query (db-spec)
              [(str "SELECT user_id "
                    "FROM " users " WHERE email = ?")
               email]))

(defn add-restaurant
  "Adds restaurant to the database in a transaction. Doesnt check if the restaurant
  is already present and will throw exception in such cases"
  [restaurant]
  (let [name (:name restaurant)
        added-by (:added-by restaurant)]
    (jdbc/insert! (db-spec)
                  (keyword restaurants)
                  {:name name :added_by added-by :timestamp (coerce/to-sql-time (time/now))})))

(defn fetch-restaurant-id
  "Fetches the restaurant-id given a restaurant name"
  [restaurant]
  (jdbc/query (db-spec)
              [(str "SELECT rest_id "
                    "FROM " restaurants " WHERE name = ?")
               restaurant]))

(defn cast-vote
  "Saves a userid-restaurantid row as a vote to the database. Will throw exception when the vote is already present."
  [row]
  (let [email (:email row)
        restaurant (:restaurant row)
        user-id (fetch-user-id email)
        rest-id (fetch-restaurant-id restaurant)
        ts-now (time/now)
        today (time/today)]
    (jdbc/insert! (db-spec)
                  (keyword votes)
                  nil
                  [(:user_id (first user-id))
                   (:rest_id (first rest-id))
                   (coerce/to-sql-date today)
                   (coerce/to-sql-time ts-now)])))

(defn fetch-votes-for-today
  "Fetches votes casted for today"
  []
  (jdbc/query (db-spec)
              [(str  "SELECT name as restaurant, count(*) as vote "
                     "FROM " votes " v, " restaurants " r "
                     "WHERE v.rest_id = r.rest_id "
                     "AND v.date = ? "
                     "GROUP BY restaurant ORDER BY vote DESC "
                     "LIMIT 10 ")
               (coerce/to-sql-date (time/today))]))

(defn fetch-my-votes
  "Fetch the historical votes casted by the user, sorted by decreasing order with a limit of ten top votes."
  [email]
  (let [user-info (fetch-user-id email)
        user-id (:user_id (first user-info))]
    (jdbc/query (db-spec)
                [(str "SELECT name as restaurant, count (*) as vote "
                      "FROM " votes " v, " restaurants " r "
                      "WHERE v.rest_id = r.rest_id "
                      "AND v.user_id = ? "
                      "GROUP BY restaurant ORDER BY vote DESC "
                      "LIMIT 10 ")
                 user-id])))

(defn fetch-popular-restaurants
  "Fetches the ten top votes restaurants of all time"
  []
  (jdbc/query (db-spec)
              [(str "SELECT name as restaurant, count(*) as vote "
                    "FROM " votes " v, " restaurants " r "
                    "WHERE v.rest_id = r.rest_id "
                    "GROUP BY restaurant ORDER BY vote DESC "
                    "LIMIT 10 ")]))

(defn fetch-offline-restaurants
  "Fetches the list of restaurants which were added offline"
  []
  (jdbc/query (db-spec)
              [(str "SELECT name as restaurant "
                    "FROM " restaurants " r "
                    "WHERE added_by != 'Zomato' ")]))

(defn check-votes
  "Checks if a user has already voted for a restaurant for today. Used in combination with cast-vote.
  It's better to use the cast-vote-safe"
  [user-id rest-id]
  (jdbc/query (db-spec)
              [(str "SELECT * "
                    "FROM " votes " v "
                    "WHERE v.user_id = ? "
                    "AND v.rest_id = ? "
                    "AND v.date = ? ")
               user-id
               rest-id
               (coerce/to-sql-date (time/today))]))

(defn add-user-safe
  "Adds user to the database in a transaction but checks if it is absent first"
  [user-details]
  (let [name (:name user-details)
        email (:email user-details)
        now (coerce/to-sql-time (time/now))]
    (jdbc/execute!
     (db-spec)
     [(str "INSERT into "
           users
           "(name, email, timestamp) "
           "SELECT ?, "           ;; User name
           " ?, "                 ;; User email
           " ?  "                 ;; Timestamp
           "WHERE NOT EXISTS "
           "(SELECT * FROM " users
           " WHERE email = ? )" ) ;; User email
      name
      email
      now
      email])))

(defn cast-vote-safe
  "Adds a vote to the database in a transaction but checks if the vote is absent first"
  [row]
  (let [email (:email row)
        restaurant (:restaurant row)
        user-do (fetch-user-id email)
        rest-do (fetch-restaurant-id restaurant)
        user-id (:user_id (first user-do))
        rest-id (:rest_id (first rest-do))
        today (coerce/to-sql-date (time/today))
        now (coerce/to-sql-time (time/now))]
    (jdbc/execute!
     (db-spec)
     [(str "INSERT into " votes
           "(user_id, rest_id, date, timestamp) "
           "SELECT ?, "           ;; User ID
           " ?, "                 ;; Restaurant ID
           " ?, "                 ;; Date
           " ? "                 ;; Timestamp
           "WHERE NOT EXISTS "
           "(SELECT * FROM " votes
           " WHERE user_id = ? "  ;; User ID
           " AND rest_id = ? "    ;; Restaurant ID
           " AND date = ? )" )    ;; Date (today)
      user-id
      rest-id
      today
      now
      user-id
      rest-id
      today])))

(defn add-restaurant-safe
  "Adds a restaurant to the database in a transaction but checks if the restaurant is absent first"
  [name added-by]
  (let [now (time/now)]
    (jdbc/execute!
     (db-spec)
     [(str "INSERT into " restaurants
           "(name, added_by, timestamp) "
           "SELECT ?," ;; Restaurant Name
           "?, "       ;; Added by
           "? "       ;; Timestamp
           "WHERE NOT EXISTS "
           "(SELECT * FROM " restaurants
           " WHERE name = ? )" ) ;; Restaurant Name
      name
      added-by
      (coerce/to-sql-time now)
      name])))
