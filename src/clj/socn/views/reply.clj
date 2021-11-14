(ns socn.views.reply
  (:require [hiccup.core :refer [html]]
            [socn.views.common :as common]
            [socn.views.utils :refer [encode-url]]))

(defn view [{:keys [parent req]}]
  (html
   [:div.container
    [:div.content
     (binding [common/*comment-view-upvote*  true
               common/*comment-view-parent*  true
               common/*comment-view-context* true
               common/*comment-view-flag*    true
               common/*comment-view-on*      true]
       (common/comment-view parent :req req))
     (common/comment-form
      parent
      (encode-url "reply" {:id (:id parent)})
      "Reply")]]))
