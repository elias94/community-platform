(ns socn.views.reply
  (:require [hiccup.core :refer [html]]
            [socn.views.edit :as edit]))



(defn view [& {:keys [parent req]}]
  (html
   [:div.container
    [:div.content
     (edit/comment-view
      parent
      [:upvote :parent :context :flag :on]
      :req req)]]))
