(ns socn.views.item
  (:require [hiccup.core :refer [html]]
            [hiccup.form :as form]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view [& {:keys [item]}]
  (html
   [:section.section
    [:div.container
     [:h1 (:title item)]]]))
