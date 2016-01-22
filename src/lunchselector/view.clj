(ns lunchselector.view
  (:require [hiccup.core :as hiccup]
            [lunchselector.db :as ldb]))

(defn render-restaurants [restaurants]
  (hiccup/html
   [:div "Please choose from the following"]
   [:div
    [:form {:action "/vote" :method "post"}
     [:table (doall (for [x restaurants]
                      [:tr
                       [:td x]
                       [:td [:input {:type "checkbox" :name "restaurant" :value x}]]]))]
     [:input {:type "submit" :name "vote"}]]]))

(defn render-vote [votes user]
  (hiccup/html
   [:div (str "Congrats " user "! Your vote for following restaurants has been submitted")]
   [:div
    [:table
     (if (coll? votes)
       (doall (for [x votes]
                [:tr
                 [:td x]]))
       [:td
        [:td votes]])]]
   [:div "You can check the results " [:a {:href "/result"} "here!"]]))

(defn- render-results-helper [rows]
  (doall (for [x rows]
           [:div
            [:div (str  (:restaurant x) " : " (:vote x))]])))

(defn render-result [rows]
  (hiccup/html
   [:div "The restaurants voted for the day are:"
    (render-results-helper rows)]))

(defn render-my-votes [rows]
  (hiccup/html
   [:div "Your past votes"
    (render-results-helper rows)]))

(defn render-popular-restaurants [rows]
  (hiccup/html
   [:div "Popular restaurants"
    (render-results-helper rows)]))

(defn render-home [user email]
  (hiccup/html
   [:div (str  "Welcome  " user)]
   [:div
    [:table {:width "100%"}
     [:br]
     [:tr (render-result (ldb/fetch-votes-for-today))]
     [:br]
     [:tr
      [:td (render-popular-restaurants (ldb/fetch-popular-restaurants))]
      [:td (render-my-votes (ldb/fetch-my-votes email))]]]]
   [:br]
   [:div "Search based on cuisine/restaurant names"
    [:form {:action "/restaurants" :method "post"}
     [:input {:type "text" :name "keyword"}]
     [:input {:type "submit" :value "Search"}]]]
   [:br]
   [:div "Add offline restaurants/tiffin services"
    [:form {:action "/add-offline-restaurants" :method "post"}
     [:input {:type "text" :name "restaurant"}]
     [:input {:type "submit" :name "Submit"}]]]))
