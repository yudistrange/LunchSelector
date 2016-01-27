(ns lunchselector.app
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [bidi.ring :as bidi]
            [lunchselector.core :as core]))

(def handler
  (bidi/make-handler
   ["/" { "" core/home
          "restaurants" core/restaurants
          "login" core/login
          "oauth" core/oauth
          "vote" core/vote
          "result" core/result
          "add-offline-restaurants" core/add-offline-restaurants
          "slack" core/slack}]))

(def lunch-app
  (-> handler
      wrap-params
      wrap-session))

(defn -main []
  (server/run-server lunch-app {:port 3000}))
