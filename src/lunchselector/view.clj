(ns lunchselector.view
  (:require [hiccup.core :as hiccup]))

(defn create-homepage []
  (hiccup/html
   [:html
    [:head
     [:title "LunchSelector homepage"]]
    [:body
     [:div "Please enter your name:"
      [:br]
      [:form {:action "/article" :method "post"}
       [:input {:type "text" :name "name"}]
       [:input {:type "text" :name "keyword"}]
       [:input {:type "submit" :name "Submit"}]]]]]))

(defn render-restaurants [restaurants user]
  (hiccup/html
   [:div "Please choose from the following"]
   [:div
    [:form {:action "/vote" :method "post"}
     [:table (doall (for [x restaurants]
                      [:tr
                       [:td x]
                       [:td [:input {:type "checkbox" :name "restaurant" :value x}]]]))]
     [:input {:type "submit" :name "vote"}]]]))

(defn render-search [user]
  (hiccup/html
   [:div (str  "Welcome  " user)]
   [:div "Please enter your search criteria"
    [:br]
    [:form {:action "/restaurants" :method "post"}
     [:input {:type "text" :name "keyword"}]
     [:input {:type "submit" :name "submit"}]]]))

(defn render-vote [votes user]
  (hiccup/html
   [:div (str "Congrats " user "! Your vote for following restaurants has been submitted")]
   [:div [:table (doall (for [x votes]
                          [:tr
                           [:td x]]))]]
   [:div "You can check the results " [:a {:href "/result"} "here!"]]))

(defn render-result [rows]
  (hiccup/html
   [:div (doall (for [x rows]
                  [:div
                   [:div (str  (:restaurant x) " : " (:votes x))]
                   [:br]]))]))
