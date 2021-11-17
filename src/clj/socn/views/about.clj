(ns socn.views.about
  (:require [hiccup.core :refer [html]]))

(defn view [_]
  (html
   [:div.container
    [:div.content
     [:h1.title "About"]
     [:p "demo about"]]]))
