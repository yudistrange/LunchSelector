(ns lunchselector.core
  (:gen-class)
  (:require [ring.util.response :as res]
            [ring.middleware.params :as params]
            [lunchselector.model :as model]
            [lunchselector.view :as view]
            [lunchselector.utils :as utils]
            [lunchselector.oauth :as oauth]
            [lunchselector.slack :as slack]
            [lunchselector.io.oauth-io :as oauth-io]
            [lunchselector.io.slack-io :as slack-io]))

(defn restaurants
  "This page displays a list of offline + online restaurants"
  [request]
  (let [keyword         (get-in request [:params "keyword"])
        online-list     (model/online-restaurants keyword)
        offline-list    (model/offline-restaurants)
        restaurant-list (concat offline-list online-list)]
    (res/response (view/render-restaurants restaurant-list))))

(defn login
  "This page is used to set the user information in cookies and save them into DB"
  [request]
  (if (contains? (:params request) "error")
    (res/response (str "Failed to login with error " (get-in request [:params "error"])))
    (let [access-token (oauth-io/fetch-google-oauth-token (get-in request [:params "code"]))
          user-resp    (oauth-io/fetch-google-user-details access-token)
          user-details (utils/parse-response-body-map user-resp)]
      (model/submit-users (:name user-details) (:email user-details))
      (assoc (res/redirect "/")
             :cookies {"username" {:value (:name user-details)}
                       "useremail" {:value (:email user-details)}}))))

(defn home
  "Home Page. Displays a bunch of stuff"
  [request]
  (let [user  (get-in request [:cookies "username" :value])
        email (get-in request [:cookies "useremail" :value])]
    (if (or (nil? user) (nil? email))
      (res/redirect (oauth/google-oauth-redirect-uri))
      (do (model/submit-users user email)
          (res/response
           (view/render-home user
                             (model/top-restaurants)
                             (model/votes-today)
                             (model/my-votes email)
                             (slack/slack-oauth-1st-step-uri)))))))

(defn vote
  "Casts yours votes!"
  [request]
  (let [votes (get-in request [:params "restaurant"])
        email (get-in request [:cookies "useremail" :value])
        user  (get-in request [:cookies "username" :value])]
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
  (let [rest-name (get-in request [:params "restaurant"])
        email     (get-in request [:cookies "useremail" :value])]
    (model/submit-restaurants rest-name email)
    (res/redirect "/")))

(defn slack
  "Slack's redirection page"
  [request]
  (let [oauth-code  (get-in request [:params "code"])
        oauth-token (slack-io/get-slack-token oauth-code)
        rtm-start   (slack-io/start-rtm-connection oauth-token)
        ws-uri      (:url (utils/parse-response-body-map rtm-start))
        channel     (utils/get-config :slack-lunch-channel-name)]
    (when (not (slack-io/is-channel-present? channel oauth-token))
      (slack-io/create-channel channel oauth-token))
    (slack-io/establish-slack-websocket ws-uri)
    (res/response "Slack integration done successfully")))
