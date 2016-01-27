(ns lunchselector.utils
  (:require [cheshire.core :as cheshire]))

(defn parse-to-clj-map [entity]
  (cheshire/parse-string (:body entity) true))

(defn get-restaurant-name-from-db [dbresult]
  (map :restaurant dbresult))
