(ns lunchselector.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]))

(def votes "votes")
(def users "users")
(def restaurants "restaurants")

(def db-path "localhost:5432/lunches")
(def db-spec {:subprotocol "postgresql"
              :subname (str "//" db-path)})

(defn add-user [user]
  (let [name (:name user)
        email (:email user)]
    (jdbc/insert! db-spec
                  (keyword users)
                  {:name name :email email :timestamp (coerce/to-sql-time (time/now))}
                  :transaction? true)))

(defn fetch-user-id [email]
  (jdbc/query db-spec
              [(str "SELECT user_id "
                    "FROM " users " WHERE email = ?")
               email]))

(defn add-restaurant [restaurant]
  (let [name (:name restaurant)
        added-by (:added-by restaurant)]
    (jdbc/insert! db-spec
                  (keyword restaurants)
                  {:name name :added_by added-by :timestamp (coerce/to-sql-time (time/now))}
                  :transaction? true)))

(defn fetch-restaurant-id [restaurant]
  (jdbc/query db-spec
              [(str "SELECT rest_id "
                    "FROM " restaurants " WHERE name = ?")
               restaurant]))

(defn cast-vote [row]
  (let [email (:email row)
        restaurant (:restaurant row)
        user-id (fetch-user-id email)
        rest-id (fetch-restaurant-id restaurant)
        ts-now (time/now)
        today (time/today)]
    (jdbc/insert! db-spec
                  (keyword votes)
                  nil
                  [(:user_id (first user-id))
                   (:rest_id (first rest-id))
                   (coerce/to-sql-date today)
                   (coerce/to-sql-time ts-now)])))

(defn fetch-votes-for-today []
  (jdbc/query db-spec
              [(str  "SELECT name as restaurant, count(*) as vote "
                     "FROM " votes " v, " restaurants " r "
                     "WHERE v.rest_id = r.rest_id "
                     "AND v.date = ? "
                     "GROUP BY restaurant ORDER BY vote DESC "
                     "LIMIT 10 ")
               (coerce/to-sql-date (time/today))]))

(defn fetch-my-votes [email]
  (let [user-info (fetch-user-id email)
        user-id (:user_id (first user-info))]
    (jdbc/query db-spec
                [(str "SELECT name as restaurant, count (*) as vote "
                      "FROM " votes " v, " restaurants " r "
                      "WHERE v.rest_id = r.rest_id "
                      "AND v.user_id = ? "
                      "GROUP BY restaurant ORDER BY vote DESC "
                      "LIMIT 10 ")
                 user-id])))

(defn fetch-popular-restaurants []
  (jdbc/query db-spec
              [(str "SELECT name as restaurant, count(*) as vote "
                    "FROM " votes " v, " restaurants " r "
                    "WHERE v.rest_id = r.rest_id "
                    "GROUP BY restaurant ORDER BY vote DESC "
                    "LIMIT 10 ")]))

(defn fetch-offline-restaurants []
  (jdbc/query db-spec
              [(str "SELECT name as restaurant "
                    "FROM " restaurants " r "
                    "WHERE added_by != 'Zomato' ")]))

(defn check-votes [user-id rest-id]
  (jdbc/query db-spec
              [(str "SELECT * "
                    "FROM " votes " v "
                    "WHERE v.user_id = ? "
                    "AND v.rest_id = ? "
                    "AND v.date = ? ")
               user-id
               rest-id
               (coerce/to-sql-date (time/today))]))

(defn add-user-safe [user-details]
  (let [name (:name user-details)
        email (:email user-details)
        now (coerce/to-sql-time (time/now))]
    (jdbc/execute!
     db-spec
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
      email]
     :transaction? true)))

(defn cast-vote-safe [row]
  (let [email (:email row)
        restaurant (:restaurant row)
        user-do (fetch-user-id email)
        rest-do (fetch-restaurant-id restaurant)
        user-id (:user_id (first user-do))
        rest-id (:rest_id (first rest-do))
        today (coerce/to-sql-date (time/today))
        now (coerce/to-sql-time (time/now))]
    (jdbc/execute!
     db-spec
     [(str "INSERT into " votes
           "(user_id, rest_id, date, timestamp) "
           "SELECT ?, "           ;; User ID
           " ?, "                 ;; Restaurant ID
           " ?, "                 ;; Date
           " ?, "                 ;; Timestamp
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
      today]
      :transaction? true)))

(defn add-restaurant-safe [restaurant]
  (let [name (:name restaurant)
        added-by (:added-by restaurant)
        now (time/now)]
    (jdbc/execute!
     db-spec
     [(str "INSERT into " restaurants
           "(name, added_by, timestamp) "
           "SELECT ?,"            ;; Restaurant Name
           "?, "                  ;; Added by
           "?, "                  ;; Timestamp
           "WHERE NOT EXISTS "
           "(SELECT * FROM " restaurants
           " WHERE name = ? )" )  ;; Restaurant Name
      name
      added-by
      (coerce/to-sql-time now)
      name]
     :transaction? true)))
