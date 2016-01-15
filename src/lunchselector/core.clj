(ns lunchselector.core
  (:require [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as res]
            [bidi.ring :as bidi]
            [cheshire.core :as cheshire]
            [lunchselector.model :as lm]
            [lunchselector.view :as lv]
            [lunchselector.oauth :as lo]
            [lunchselector.db :as ldb]))

(defn restaurants [request]
  (let [cookies (:cookies request)
        params (:params request)
        user (:value (get cookies "user"))
        keyword (get params "keyword")
        restaurant-list (lm/get-restaurants keyword)]
    (res/response (lv/render-restaurants restaurant-list user))))

(defn login [request]
  (let [params (:params request)
        token-response (lo/get-oauth-token (get params "code"))
        access-token (get (cheshire/parse-string (:body token-response))
                          "access_token")
        user-details (lo/get-user-details access-token)]
    (-> (res/redirect "/")
        (assoc :cookies {"user" {:value (:name (cheshire/parse-string (:body user-details) true))}}))))

(defn oauth [request]
  (res/redirect lo/oauth-redirect))

(defn search [request]
  (let [cookies (:cookies request)
        user (:value (get cookies "user"))]
    (if (nil? user)
      (res/redirect "/oauth")
      (res/response (lv/render-search user)))))

(defn vote [request]
  (let [params (:params request)
        votes (get params "restaurant")
        cookies (:cookies request)
        user (:value (get cookies "user"))]
    (lm/submit-votes user votes)
    (res/response (lv/render-vote votes user))))

(defn result [request]
  (res/response (ldb/query)))

(def handler
  (bidi/make-handler ["/" {
                           "" search
                           "restaurants" restaurants
                           "login" login
                           "oauth" oauth
                           "vote" vote
                           "result" result
                           }]))

(def app
  (-> handler
      (wrap-params handler)
      wrap-session))

(defn -main []
  ((jetty/run-jetty app {:port 3000})))
