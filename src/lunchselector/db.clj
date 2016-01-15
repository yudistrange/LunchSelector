(ns lunchselector.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]))

(def table "lunchvotes")
(def db-path "localhost:5432/lunches")
(def db-spec {:subprotocol "postgresql"
              :subname (str "//" db-path)})

(defn insert [row]
  (let [user (:user row)
        restaurant (:restaurant row)
        ts-now (time/now)
        today (time/today)]
    (jdbc/insert! db-spec
                  (keyword table)
                  nil
                  [user restaurant
                   (coerce/to-sql-date today)
                   (coerce/to-sql-time ts-now)])))

(defn query []
  (let [today (time/today)]
    (jdbc/query db-spec
                [(str  "SELECT restaurant, count (*) as votes "
                       "FROM " table " WHERE date = ? "
                       "GROUP BY restaurant ORDER BY votes DESC")
                 (coerce/to-sql-date today)])))
