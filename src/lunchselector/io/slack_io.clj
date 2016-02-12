(ns lunchselector.io.slack-io
  (:require [lunchselector.slack :as slack]
            [lunchselector.utils :as utils]
            [lunchselector.model :as model]
            [gniazdo.core :as ws])
  (:import  [org.eclipse.jetty.websocket.client WebSocketClient]))

(def connection-config-map (atom {}))

(defn slack-exec-commands
  "The command dispatcher. This function actually sends out the messages to slack
  depending upon what the user has asked for"
  [chan-id user-id text]
  (let [cmd   (second (slack/parse-message text))
        input (nthrest (slack/parse-message text) 2)
        token (:token @connection-config-map)]
    (cond
      (= cmd ":history") (let [user-info (slack/slack-user-info-uri token user-id)
                               parsed-info (utils/parse-response-body-map user-info)
                               email (:email (:profile (:user parsed-info)))]
                           (utils/get-request (slack/slack-send-msg-uri
                                               token user-id
                                               (slack/slack-quoted-result (model/my-votes email)))))

      (= cmd ":vote")    (let [user-info (slack/slack-user-info-uri token user-id)
                               parsed-info (utils/parse-response-body-map user-info)
                               email (:email (:profile (:user parsed-info)))]
                           (utils/get-request (slack/slack-send-msg-uri
                                               token chan-id
                                               (slack/slack-quoted-result
                                                (str "I cant vote yet. But this is what you sent: "
                                                     email input)))))

      (= cmd ":popular")   (utils/get-request (slack/slack-send-msg-uri
                                               token chan-id
                                               (slack/slack-quoted-result (model/top-restaurants))))

      (= cmd ":status")    (utils/get-request (slack/slack-send-msg-uri
                                               token chan-id
                                               (slack/slack-quoted-result (model/votes-today))))

      (= cmd ":help")      (utils/get-request (slack/slack-send-msg-uri
                                               token user-id
                                               (slack/slack-result (slack/slack-bot-help))))

      :else                (utils/get-request (slack/slack-send-msg-uri
                                               token chan-id
                                               "I dont get it!")))))

(defn slack-process-msg
  "Processes the message to fetch the user-id and message and sends it for dispatch"
  [message chan-id]
  (let [msg     (utils/parse-string message)
        text    (:text msg)
        user-id (:user msg)
        bot     (slack/bot-name)]
    (when (slack/message-for-bot? msg chan-id bot)
      (slack-exec-commands chan-id user-id text))))

(defn slack-establish-conn
  "Establishes the websocket connection and adds the connection parameters to the connection-map atom"
  [ws-uri]
  (let [java-uri (java.net.URI/create ws-uri)
        client   (ws/client java-uri)]
    (swap! connection-config-map assoc :client client)
    (.start client)
    (ws/connect ws-uri
                :on-connect #(prn (str "Connected to " %))
                :on-receive #(do (prn (str "Recieved " %))
                                 (slack-process-msg
                                  % (:channel-id @connection-config-map)))
                :client (:client @connection-config-map))))


(defn establish-slack-oauth
  "Sends a Get request to establish slack oauth connection"
  [oauth-code]
  (utils/get-request (slack/slack-oauth-2nd-step-uri oauth-code)))

(defn get-slack-token
  "Establishes slack oauth and gets the oauth token"
  [oauth-code]
  (let [response (establish-slack-oauth oauth-code)
        token (:access_token (utils/parse-response-body-map response))]
    (swap! connection-config-map assoc :token token)
    token))

(defn start-rtm-connection
  "Sends a Get request to the RTM API with the given access token"
  [access-token]
  (utils/get-request (slack/slack-rtm-start-uri access-token)))

(defn establish-slack-websocket
  "Establishes the websocket connection with slack"
  [ws-uri]
  (utils/get-request (slack-establish-conn ws-uri)))

(defn is-channel-present?
  "Checks whether the channel on which the bot has to reside is present"
  [channel-name access-token]
  (let [response  (utils/get-request (slack/slack-channel-present-uri access-token))
        chan-list (:channels (utils/parse-response-body-map response))
        req-chan  (filter (fn [x] (= channel-name (get x "name"))) chan-list)]
    (if (empty? req-chan)
      false
      (do
        (swap! connection-config-map assoc :channel-id (get req-chan "id"))
        true))))

(defn create-channel
  "Creates a slack channel with the specified name and token"
  [channel-name access-token]
  (let [response    (utils/get-request (slack/slack-channel-create-uri access-token channel-name))
        chan-detail (:channel (utils/parse-response-body-map response))]
    (swap! connection-config-map assoc :channel-id (get chan-detail "id"))))
