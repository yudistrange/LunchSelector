(ns lunchselector.oauth-test
  (:require [lunchselector.oauth :refer :all]
            [lunchselector.utils :as utils]
            [clojure.test :refer :all])
  (:import  [org.apache.commons.validator.routines UrlValidator]))

(utils/initialize-app-configuration)
(def url-validator (new UrlValidator))

(deftest google-oauth-redirect-uri-test
  (is (true? (.isValid url-validator (google-oauth-redirect-uri)))))

(deftest google-oauth-token-uri-test
  (is (true? (.isValid url-validator (google-oauth-token-uri)))))

(deftest google-user-details-uri-test
  (is (true? (.isValid url-validator (google-user-details-uri "token")))))
