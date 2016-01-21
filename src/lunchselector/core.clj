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
            [lunchselector.utils :as lu]))

(defn restaurants [request]
  "This page displays a list of offline + online restaurants"
  (let [cookies (:cookies request)
        params (:params request)
        user (:value (get cookies "user"))
        keyword (get params "keyword")
        online-list (lm/online-restaurants keyword)
        offline-list (lm/offline-restaurants)
        restaurant-list (concat offline-list online-list)]
    (res/response (lv/render-restaurants restaurant-list))))

(defn login [request]
  "This page is used to set the user information in cookies and save them into DB"
  (let [params (:params request)
        token-response (lo/get-oauth-token (get params "code"))
        access-token (:access_token (lu/parse-to-clj-map token-response))
        user-details-resp (lo/get-user-details access-token)
        user-details (lu/parse-to-clj-map user-details-resp)]
    (lm/submit-users (:name user-details) (:email user-details))
    (-> (res/redirect "/")
        (assoc :cookies {"username" {:value (:name user-details)}
                         "useremail" {:value (:email user-details)}}))))

(defn oauth [request]
  "This page redirects to the Google OAuth URI for the OAuth dance"
  (res/redirect lo/oauth-redirect))

(defn home [request]
  "Home Page. Displays a bunch of stuff"
  (let [cookies (:cookies request)
        user (:value (get cookies "username"))
        email (:value (get cookies "useremail"))]
    (if (nil? user)
      (res/redirect "/oauth")
      (do
        (lm/submit-users user email)
        (res/response (lv/render-home user email))))))

(defn vote [request]
  "Casts yours votes!"
  (let [params (:params request)
        votes (get params "restaurant")
        cookies (:cookies request)
        email (:value (get cookies "useremail"))
        user (:value (get cookies "username"))]
    (do  (lm/submit-online-restaurants votes)
         (lm/submit-votes email votes)
         (res/response (lv/render-vote votes user)))))

(defn result [request]
  "Displays the votes for the day"
  (res/response (lv/render-result
                 (lm/votes-today))))

(defn add-offline-restaurants [request]
  "Adds offline restaurants to the DB and redirects to home page"
  (let [params (:params request)
        rest-name (get params "restaurant")
        cookies (:cookies request)
        email (:value (get cookies "useremail"))]
    (do (lm/submit-restaurants {:name  rest-name :added-by email})
        (res/redirect "/"))))

(def handler
  (bidi/make-handler ["/" {
                           "" home
                           "restaurants" restaurants
                           "login" login
                           "oauth" oauth
                           "vote" vote
                           "result" result
                           "add-offline-restaurants" add-offline-restaurants
                           }]))

(def app
  (-> handler
      (wrap-params handler)
      wrap-session))

(defn -main []
  ((jetty/run-jetty app {:port 3000})))
