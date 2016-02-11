(ns lunchselector.view
  (:require [hiccup.core :as hiccup]
            [lunchselector.utils :as utils]))

(defn- create-restaurant-div [restaurants]
  [:div
   [:form {:action "/vote" :method "post"}
    [:table
     (doall
      (for  [x restaurants]
        [:tr
         [:td x]
         [:td [:input {:type "checkbox" :name "restaurant" :value x}]]]))]
    [:input {:type "submit" :name "vote"}]]])

(defn render-restaurants [restaurants]
  (hiccup/html
   [:div "Please choose from the following"]
   (create-restaurant-div restaurants)))

(defn- create-vote-div [votes]
  [:div
    [:table
     (if (coll? votes)
       (doall
        (for [x votes]
          [:tr
           [:td x]]))
       [:td
        [:td votes]])]])

(defn render-vote [votes user]
  (hiccup/html
   [:div (str "Congrats " user "! Your vote for following restaurants has been submitted")]
   (create-vote-div votes)
   [:div "You can check the results " [:a {:href "/result"} "here!"]]))

(defn- render-results-helper [rows]
  (doall
   (for [x rows]
     [:div
      [:div (str  (:restaurant x) " : " (:vote x))]])))

(defn render-result [rows]
  (hiccup/html
   [:div [:b "The restaurants voted for the day are:"]
    (render-results-helper rows)]
   [:div [:a {:href "/"} "Go back!"]]))

(defn render-my-votes [rows]
  (hiccup/html
   [:div [:b "Your past votes"]
    (render-results-helper rows)]))

(defn render-popular-restaurants [rows]
  (hiccup/html
   [:div [:b "Popular restaurants"]
    (render-results-helper rows)]))

(defn render-home [user restaurants votes my-votes slack-uri]
  (hiccup/html
   [:div [:b (str  "Welcome  " user)]]
   [:div
    [:table {:width "100%"}
     [:br]
     [:tr
      [:td [:b "Today's votes"]
       [:br]
       (render-results-helper votes)]]
     [:tr]
     [:tr
      [:td (render-popular-restaurants restaurants)]
      [:td (render-my-votes my-votes)]]]]
   [:br][:br]
   [:div [:b "Search based on cuisine/restaurant names"]
    [:form {:action "/restaurants" :method "post"}
     [:input {:type "text" :name "keyword"}]
     [:input {:type "submit" :value "Search"}]]]
   [:br][:br]
   [:div [:b "Add offline restaurants/tiffin services"]
    [:form {:action "/add-offline-restaurants" :method "post"}
     [:input {:type "text" :name "restaurant"}]
     [:input {:type "submit" :name "Submit"}]]]
   [:br][:br]
   [:div
    {:style {:position "absolute"
             :top "100%"
             :right "0%"}}
    [:b "Slack Integration"]
    [:br]
    [:a {:href slack-uri}
     [:img {:src (utils/get-config :slack-button-image) :height "40" :width "140"}]]]))
