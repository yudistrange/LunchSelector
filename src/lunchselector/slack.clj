(ns lunchselector.slack
  (:require [ring.util.codec :as codec]
            [clojure.string :as string]
            [lunchselector.utils :as utils]))

;; Conneciton config maps
(def bot-name (fn [] (utils/get-config :slack-bot-name)))

(defn slack-bot-help []
  (let [bot (bot-name)]
    (str "Hi! This is help section for the Lunch! App.\n"
         "List of commands:\n"
         "@" bot " :status \tPrints today's vote tally\n"
         "@" bot " :top \tPrints list of popular restaurants\n"
         "@" bot " :history \tPrints your vote history\n"
         "@" bot " :help \tPrints this message\n")))


;; Slack API end points
(defn slack-oauth-1st-step-uri
  "Creates the uri for Slack's OAuth 1st step endpoint with appropriate params"
  []
  (str (utils/get-config :slack-oauth-code-uri)
       "client_id="     (codec/url-encode (utils/get-config :slack-oauth-client-id))
       "&scope="        (codec/url-encode (utils/get-config :slack-oauth-scope))
       "&redirect_uri=" (codec/url-encode (utils/get-config :slack-oauth-redirect-uri))
       "&team="         (codec/url-encode (utils/get-config :slack-team-name))))

(defn slack-oauth-2nd-step-uri
  "Sends the request to Slack's OAuth 2nd step endpoint,
  fetching the access tokens for an authenticated user."
  [code]
  (str (utils/get-config :slack-oauth-token-uri)
       "client_id="      (codec/url-encode (utils/get-config :slack-oauth-client-id))
       "&client_secret=" (codec/url-encode (utils/get-config :slack-oauth-client-secret))
       "&redirect_uri="  (codec/url-encode (utils/get-config :slack-oauth-redirect-uri))
       "&code="          (codec/url-encode code)))

(defn slack-user-info-uri
  "Fetches the user information from slack. The information"
  [token user-id]
  (str (utils/get-config :slack-user-info-uri)
       "token=" (codec/url-encode token)
       "&user=" (codec/url-encode user-id)))

(defn slack-send-msg-uri
  "Create the uri with parameters to send the message to slack via chat.PostMessage API"
  [access-token channel-id msg]
  (str (utils/get-config :slack-post-message-uri)
       "token="     (codec/url-encode access-token)
       "&channel="  (codec/url-encode channel-id)
       "&username=" (codec/url-encode (bot-name))
       "&text="     (codec/url-encode msg)
       "&as_user=false"
       "&mrkdwn=true"))

(defn slack-rtm-start-uri
  "Creates a URI to initiate the websocket connection to Slack using the RTM API"
  [token]
  (str (utils/get-config :slack-initiate-rtm-uri)
       "token=" token))

(defn slack-channel-present-uri
  "Creates a URI to search for channels"
  [token]
  (str (utils/get-config :slack-list-channel-uri)
       "token=" token))

(defn slack-channel-create-uri
  "Creates a URI which will create a new channel"
  [token channel]
  (str (utils/get-config :slack-create-channel-uri)
       "token=" token
       "&name=" channel))


;; Slack helper functions
(defn message-for-bot?
  "Checks whether the message received is for the bot"
  [msg chan-id bot-id]
  (let [type    (:type msg)
        channel (:channel msg)
        text    (:text msg)
        bot     (re-pattern bot-id)]
    (if (and (= "message" type)
             (= channel chan-id)
             (not (nil? (re-find bot text))))
      true
      false)))

(defn parse-message
  "Tokenize the text from slack into "
  [text]
  (if (seq? text)
    (string/split (first text) #" ")
    (string/split text #" ")))

(defn- add-quotes
  "Addes quotes to the messages. This helps in better rendering at slack"
  [text]
  (str "`" text "`"))

(defn slack-result
  "Prepares result by joining it"
  [result]
  (string/join result))

(defn slack-quoted-result
  "Prepares quoted result for displaying on slack"
  [result]
  (add-quotes (slack-result result)))
