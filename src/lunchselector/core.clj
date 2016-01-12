(ns lunchselector.core
  (:require [ring.util.response :as res]
            [bidi.ring :refer (make-handler)]
            [ring.middleware.session :refer [wrap-session]]
            [lunchselector.fetch :refer [get-categories]]
            [cheshire.core :refer :all]))

(defn homepage [request]
  (res/response "<html>
<head>
<title>HomePage!!</title>
</head>
<body>
<form action=\"article\" method=\"post\">
Name: <input type=\"text\" name=\"name\"><br/>
<input type=\"submit\" value=\"submit\">
</form>"))

(defn not-found [request]
  {:status 404
   :body "Not found!"})

(defn article [request]
  (let [params (:params request)]
    (res/response (str "Hi " (get params "name")))))

(defn categories [request]
  (let [resp-map (parse-string (:body (get-categories)))
        cats (get resp-map "categories")
        ]
    (res/response (str cats))))

(def handler
  (make-handler ["/" {
                      "home" homepage
                      "article" article
                      "categories" categories
                      }]))

(def app
  (-> handler
      wrap-session))
