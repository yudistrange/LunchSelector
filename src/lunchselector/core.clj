(ns lunchselector.core
  (:gen-class)
  (:require [ring.util.response :as res]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [cheshire.core :as cheshire]
            [lunchselector.model :as model]
            [lunchselector.view :as view]
            [lunchselector.oauth :as oauth]
            [lunchselector.utils :as utils]
            [lunchselector.slack :as slack]
            [lunchselector.utils :as utils]))

(defn restaurants
  "This page displays a list of offline + online restaurants"
  [request]
  (let [params (:params request)
        keyword (get params "keyword")
        online-list (model/online-restaurants keyword)
        offline-list (model/offline-restaurants)
        restaurant-list (concat offline-list online-list)]
    (res/response (view/render-restaurants restaurant-list))))

(defn login
  "This page is used to set the user information in cookies and save them into DB"
  [request]
  (let [params (:params request)
        token-response (oauth/get-oauth-token (get params "code"))
        access-token (:access_token (utils/parse-to-clj-map token-response))
        user-details-resp (oauth/get-user-details access-token)
        user-details (utils/parse-to-clj-map user-details-resp)]
    (model/submit-users (:name user-details) (:email user-details))
    (-> (res/redirect "/")
        (assoc :cookies {"username" {:value (:name user-details)}
                         "useremail" {:value (:email user-details)}}))))

(defn oauth
  "This page redirects to the Google OAuth URI for the OAuth dance"
  [request]
  (res/redirect oauth/oauth-redirect))

(defn home
  "Home Page. Displays a bunch of stuff"
  [request]
  (let [cookies (:cookies request)
        user (get-in cookies ["username" :value])
        email (get-in cookies ["useremail" :value])]
    (if (or (nil? user) (nil? email))
      (res/redirect "/oauth")
      (do (model/submit-users user email)
          (res/response (view/render-home user email))))))

(defn vote
  "Casts yours votes!"
  [request]
  (let [params (:params request)
        votes (get params "restaurant")
        cookies (:cookies request)
        email (get-in cookies ["useremail" :value])
        user (get-in cookies ["username" :value])]
    (model/submit-online-restaurants votes)
    (model/submit-votes email votes)
    (res/response (view/render-vote votes user))))

(defn result
  "Displays the votes for the day"
  [request]
  (res/response (view/render-result
                 (model/votes-today))))

(defn add-offline-restaurants
  "Adds offline restaurants to the DB and redirects to home page"
  [request]
  (let [params (:params request)
        rest-name (get params "restaurant")
        cookies (:cookies request)
        email (get-in cookies ["useremail" :value])]
    (model/submit-restaurants {:name  rest-name :added-by email})
    (res/redirect "/")))

(defn slack
  "Slack's redirection page"
  [request]
  (let [params (:params request)
        oauth-code (get params "code")
        oauth-token (slack/slack-oauth-2nd-step oauth-code)
        access_token (:access_token (utils/parse-to-clj-map oauth-token))
        rtm-start (slack/slack-rtm-start access_token)
        response (utils/parse-to-clj-map rtm-start)
        ws-uri (:url response)
        channels (:channels response)
        users (:users response)
        ws-conn (slack/slack-establish-conn ws-uri channels users)]
    (res/response "Slack integration done successfully")))
