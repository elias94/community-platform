(ns socn.views.error
  (:require [hiccup.core :refer [html]]))

(defn view [{:keys [error]}]
  (html
   [:div.container
    [:div.content
     [:p error]]]))
