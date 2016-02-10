(ns lunchselector.utils
  (:require [cheshire.core :as cheshire]
            [clojure.edn :as edn]
            [org.httpkit.client :as client]))

(def global-appconfig (atom {}))

(defn initialize-app-configuration
  "Initializes the app config."
  []
  (with-open [config (java.io.PushbackReader. (clojure.java.io/reader "appconfig.edn"))]
    (binding [*read-eval* false]
      (swap! global-appconfig merge (read config)))))

(defn get-config
  "Returns the config value for the key passed.
  Throws runtime exception if the key was not found."
  [config-key]
  (if (contains? @global-appconfig config-key)
    (get @global-appconfig config-key)
    (throw (new RuntimeException (str "The config key " config-key " was not found!" @global-appconfig)))
    ))

(defn parse-response-body-map
  "Parses the body from the response map"
  [entity]
  (cheshire/parse-string (:body entity) true))

(defn parse-string
  "Parses the text and tokenizes it as a clj map"
  [entity]
  (cheshire/parse-string entity true))

(defn post-request
  "Sends a Post request to the specified URI with the params (if provided)"
  ([uri] @(client/post uri))
  ([uri params] @(client/post uri params)))

(defn get-request
  "Sends a Get request to the specified URI"
  ([uri] @(client/get uri))
  ([uri options] @(client/get uri options)))
