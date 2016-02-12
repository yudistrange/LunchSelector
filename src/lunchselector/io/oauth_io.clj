(ns lunchselector.io.oauth-io
  (:require [lunchselector.oauth :as oauth]
            [lunchselector.utils :as utils]))

(defn establish-google-oauth
  "Establishes Google OAuth connection"
  [code]
  (utils/post-request (oauth/google-oauth-token-uri)
                      (oauth/google-oauth-token-params code)))

(defn fetch-google-oauth-token
  "Establishes connection and sends back the Oauth access token"
  [code]
  (let [response (establish-google-oauth code)]
    (:access_token (utils/parse-response-body-map response))))

(defn fetch-google-user-details
  "Fetches Google user details"
  [access-token]
  (utils/get-request (oauth/google-user-details-uri access-token)))
