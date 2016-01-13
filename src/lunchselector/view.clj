(ns lunchselector.view
  (:require [hiccup.core :as hiccup]))

(defn create-table [content]
  (hiccup/html
   [:html
    [:head
     [:title "Restaurants"]]
    [:body
     [:div {:id "rest-table"}
      [:table (doall (for [x content]
                       [:div
                        [:tr x]
                        [:br]]))]]]]))
