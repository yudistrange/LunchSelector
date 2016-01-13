(ns lunchselector.core
  (:require [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :as res]
            [bidi.ring :as bidi]
            [lunchselector.model :as lm]
            [lunchselector.view :as lv]))

(defn homepage [request]
  (res/response "Home!"))

(defn not-found [request]
  {:status 404
   :body "Not found!"})

(defn article [request]
  (let [params (:params request)]
    (res/response (str "Hi " (get params "name")))))

(defn categories [request]
  (let [resp-map (:body (lm/get-categories))
        cats (get resp-map "categories")
        ]
    (res/response (str cats))))

(defn restaurants [request]
  (let [restaurant-list (lm/get-restaurants)]
    (res/response (lv/create-table restaurant-list))))

(defn trial [request]
  (res/response "Hello!"))

(def handler
  (bidi/make-handler ["/" {
                      "" article
                      "home" homepage
                      "article" article
                      "categories" categories
                      "restaurants" restaurants
                      "trial" trial
                      }]))

(def app
  (-> handler
      wrap-session))
