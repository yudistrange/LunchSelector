(ns lunchselector.app
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [bidi.ring :as bidi]
            [lunchselector.utils :as utils]
            [lunchselector.core :as core]))

(def handler
  (bidi/make-handler
   ["/" { "" core/home
          "restaurants" core/restaurants
          "login" core/login
          "vote" core/vote
          "result" core/result
          "add-offline-restaurants" core/add-offline-restaurants
          "slack" core/slack}]))

(def lunch-app
  (-> handler
      wrap-cookies
      wrap-params
      wrap-session))

(defn -main []
  (utils/initialize-app-configuration)
  (server/run-server lunch-app {:port (utils/get-config :server-port)}))
