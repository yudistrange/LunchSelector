(ns lunchselector.slack-test
  (:require [lunchselector.slack :refer :all]
            [lunchselector.utils :as utils]
            [clojure.test :refer :all])
  (:import  [org.apache.commons.validator.routines UrlValidator]))

(utils/initialize-app-configuration)
(def url-validator (new UrlValidator))

(deftest bot-name-test
  (is (= (utils/get-config :slack-bot-name)
         (bot-name))))

(deftest slack-oauth-1st-step-uri-test
  (is (true? (.isValid url-validator (slack-oauth-1st-step-uri)))))

(deftest slack-oauth-2nd-step-uri-test
  (is (true? (.isValid url-validator (slack-oauth-2nd-step-uri "code")))))

(deftest slack-user-info-uri-test
  (is (true? (.isValid url-validator (slack-user-info-uri "token" "user")))))

(deftest slack-send-msg-uri-test
  (is (true? (.isValid url-validator (slack-send-msg-uri "token" "channel" "message")))))

(deftest slack-rtm-start-uri-test
  (is (true? (.isValid url-validator (slack-rtm-start-uri "token")))))

(deftest slack-channel-present-uri-test
  (is (true? (.isValid url-validator (slack-channel-present-uri "token")))))

(deftest slack-channel-create-uri-test
  (is (true? (.isValid url-validator (slack-channel-create-uri "token" "channel")))))
