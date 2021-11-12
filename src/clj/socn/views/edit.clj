(ns socn.views.edit
  (:require [hiccup.core :refer [html]]
            [hiccup.form :as form]
            [ring.util.codec :refer [form-encode]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [socn.views.common :as common :refer [with-sep]]
            [socn.views.utils :refer [plural text-age age author? encode-url]]
            [socn.utils :refer [in?]]))

(def comment-opts
  "List of supported options for the comment component."
  [:upvote
   :parent
   :context
   :edit
   :delete
   :on
   :reply])

(defn comment-view
  [{:keys [id score submitted author content parent item] :as comment}
   opts ; array of keywords to enable functionalities
   & {:keys [req]}]
  [:div.comment {:id id}
   [:div.comment-content
    (if (in? opts :upvote)
      (if-not (author? comment req)
        (common/upvote comment)
        [:span "*"])
      [:span])
    [:div.comment-header
     ;; standard info
     [:span (str (plural score "point") " by ")]
     [:a.link {:href (str "/user?id=" author)} author]
     [:span " "]
     [:a.link {:href (str "/comment?id=" id)}
      (text-age (age submitted :minutes))]
     ;; extra actions
     (when (and (in? opts :parent)
                (int? parent))
       (with-sep
         [:a.link {:href (encode-url "comment" {:id parent})} "parent"]))
     (when (in? opts :context)
       (with-sep
         [:a.link {:href (encode-url "item" {:id (:id item)} id)}
          "context"]))
     (when (in? opts :delete)
       (with-sep
         [:a.link {:href (encode-url "delete" {:id id})} "delete"]))
     (when (in? opts :on)
       (list
        [:span " | on: "]
        [:a.link {:href (str "/item?id=" (:id item))} (:title item)]))]]
   [:div.comment-content
    [:span]
    [:span content]]
   (when (in? opts :reply)
     [:div.comment-content
      [:span]
      [:div.comment-footer
       [:a.link "reply"]]])])

(defn view [& {:keys [item type req]}]
  (html
   [:div.container
    [:div.content
     (if (= type :comment)
       (comment-view
        item
        [:upvote :parent :context :delete :on]
        :req req))
     [:div.item-form
      [:form {:method "post"
              :action (encode-url
                       "update?"
                       {:id   (:id item)
                        :type (second (str type))
                        :goto (let [is-comment (= type :comment)]
                                ;; encode url /item?id=12#245 if type is :comment
                                (encode-url
                                 "item"
                                 {:id (get (if is-comment (:item item) item)
                                           :id)}
                                 (when is-comment (:id item))))})}
       (anti-forgery-field)
       [:textarea.textarea {:name "content" :cols 49 :rows 5}
        (:content item)]
       common/markdown-symbol
       [:div
        [:button {:type "submit"} "Update"]]]]]]))
