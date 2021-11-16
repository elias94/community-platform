(ns socn.views.edit
  (:require [hiccup.core :refer [html]]
            [socn.views.common :as common]
            [socn.views.utils :refer [encode-url]]))

(defn view [{:keys [item links type req]}]
  (html
   [:div.container
    [:div.content
     (if (= type :comment)
       (binding [common/*comment-view-upvote*  true
                 common/*comment-view-parent*  true
                 common/*comment-view-context* true
                 common/*comment-view-delete*  true
                 common/*comment-view-on*      true]
         (common/comment-view item :req req))
       (binding [common/*item-view-edit* true]
         (common/item-view item links nil)))
     (binding [common/*comment-form-content* true]
       (let [id         (:id item)
             is-comment (= type :comment)
             goto-id    (if is-comment (:id (:item item)) id)]
         (common/comment-form
          item
          (encode-url
           "update?"
           {:id   id
            :type (second (str type))
            ;; encode url /item?id=12#245 if type is :comment
            :goto (encode-url
                   "item"
                   {:id goto-id}
                   (when is-comment id))})
          "Update")))]]))
