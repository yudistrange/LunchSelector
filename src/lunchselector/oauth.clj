(ns lunchselector.oauth
  (:require [ring.util.codec :as codec]
            [lunchselector.utils :as utils]))

(defn google-oauth-redirect-uri
  "Creates the URI from which to request the OAuth code from google"
  []
  (str (utils/get-config :google-oauth-code-uri)
       "scope="            (codec/url-encode (utils/get-config :google-oauth-scope))
       "&redirect_uri="    (codec/url-encode (utils/get-config :google-oauth-redirect-uri))
       "&client_id="       (codec/url-encode (utils/get-config :google-oauth-client-id))
       "&response_type="   (codec/url-encode (utils/get-config :google-oauth-response-type))
       "&approval_prompt=" (codec/url-encode (utils/get-config :google-oauth-approval-prompt))))

(defn google-oauth-token-uri
  "Creates the URI from to request Google's OAuth API for authentication"
  []
  (str (utils/get-config :google-oauth-token-uri)))

(defn google-oauth-token-params
  "Creates a map of the parameters to be passed for Google OAuth"
  [code]
  {:form-params
   {:code          code
    :client_id     (utils/get-config :google-oauth-client-id)
    :client_secret (utils/get-config :google-oauth-client-secret)
    :redirect_uri  (utils/get-config :google-oauth-redirect-uri)
    :grant_type    (utils/get-config :google-oauth-grant-type)}})


(defn google-user-details-uri
  "Creates the URI to fetch the google account details"
  [token]
  (str (utils/get-config :google-oauth-user-info-uri)
       "access_token=" token))
