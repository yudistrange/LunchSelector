(ns lunchselector.slack
  (:require [ring.util.codec :as codec]
            [ring.util.response :as res]
            [clj-http.client :as client]
            [gniazdo.core :as ws]
            [cheshire.core :as cheshire]
            [clojure.string :as string]
            [lunchselector.model :as model])
  (:import  [org.eclipse.jetty.websocket.client WebSocketClient]))

;; Application config atoms
(def config-map (atom {}))
(def msg-id (atom 1))


;; Slack OAuth keys
(def slack-client-id "2216371543.19541544934")
(def slack-client-secret "91d548e5050c3b0f872a908f78173a11")
(def slack-bot-token "xoxb-19220038372-ZGp9heQYGJJNrzNRtqXfere1")
(def slack-team-name "Nilenso")
(def slack-bot-name "luncher")

;; Slack API uris
(def slack-redirect-uri "http://lunch.nilenso.com/slack")
(def slack-oauth-code-uri "https://slack.com/oauth/authorize?")
(def slack-oauth-access-uri "https://slack.com/api/oauth.access?")
(def slack-channel-info-uri "https://slack.com/api/channels.list?")
(def slack-chat-post-uri "https://slack.com/api/chat.postMessage?")
(def slack-rtm-start-uri "https://slack.com/api/rtm.start?")
(def slack-button-img "https://platform.slack-edge.com/img/add_to_slack@2x.png")

;; Slack
(def slack-oauth-1st-step
  (str slack-oauth-code-uri
       "client_id=" (codec/url-encode slack-client-id) "&"
       "scope=client,read,post&"
       "redirect_uri=" (codec/url-encode slack-redirect-uri) "&"
       "team=" (codec/url-encode slack-team-name)))

(defn slack-oauth-2nd-step [code]
  (client/get
   (str slack-oauth-access-uri
        "client_id=" (codec/url-encode slack-client-id) "&"
        "client_secret=" (codec/url-encode slack-client-secret) "&"
        "redirect_uri=" (codec/url-encode slack-redirect-uri) "&"
        "code=" (codec/url-encode code))))

(defn slack-send-msg [access-token channel-id msg]
  (client/get
   (str slack-chat-post-uri
        "token=" (codec/url-encode access-token) "&"
        "channel=" (codec/url-encode channel-id) "&"
        "as_user=false&"
        "username=" (codec/url-encode slack-bot-name) "&"
        "mrkdwn=true&"
        "text=" (codec/url-encode msg))))

(defn slack-channel-list [access-token]
  (client/get
   (str slack-channel-info-uri
        "token=" (codec/url-encode access-token))))

(defn slack-rtm-start [access-token]
  (let [token access-token]
    (swap! config-map assoc :token token)
    (client/get
     (str slack-rtm-start-uri
          "token=" access-token "&"))))

(defn- message-for-bot? [msg chan-id bot-id]
  (let [type (:type msg)
        channel (:channel msg)
        text (:text msg)
        bot (re-pattern bot-id)]
    (if (and (= "message" type)
             (= channel chan-id)
             (not (nil? (re-find bot text))))
      true
      false)))

(defn- parse-commands [text]
  (if (seq? text)
    (second (string/split (first text) #" "))
    (second (string/split text #" "))))

(defn- add-quotes [text]
  (str "`" text "`"))

(defn slack-command-result [result]
  (add-quotes (apply str result)))

(defn slack-exec-commands [chan-id text]
  (let [cmd (parse-commands text)
        input (rest text)
        token (:token @config-map)]
    (cond
      (= cmd ":top") (slack-send-msg token chan-id (slack-command-result (model/top-restaurants)))
      (= cmd ":stat") (slack-send-msg token chan-id (slack-command-result (model/votes-today)))
      :else  (slack-send-msg token chan-id "I dont get it!"))))

(defn slack-process-msg [message chan-id bot-id]
  (let [msg (cheshire/parse-string message true)
        text (:text msg)]
    (when (message-for-bot? msg chan-id bot-id)
      (slack-exec-commands chan-id text))))

(defn slack-establish-conn [ws-uri channels users]
  (let [lunch-chan (filter (fn [x] (= "lunch" (:name x))) channels)
        chan-id (:id (first lunch-chan))
        bot-info (filter (fn [x] (= "luncher" (:name x))) users)
        bot-id (:id (first bot-info))
        java-uri (java.net.URI/create ws-uri)
        client (ws/client java-uri)]

    (swap! config-map assoc :client client :bot-id bot-id :channel-id chan-id )
    (.start client)
    (ws/connect ws-uri
                :on-connect #(prn (str "Connected to " %))
                :on-receive #(slack-process-msg
                              %
                              (:channel-id @config-map)
                              (:bot-id @config-map))
                :client (:client @config-map))))
