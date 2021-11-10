(ns socn.views.edit
  (:require [hiccup.core :refer [html]]
            [hiccup.form :as form]
            [ring.util.codec :refer [form-encode]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [socn.views.common :as common]
            [socn.views.utils :refer [plural text-age age author?]]
            [socn.utils :refer [in?]]))

(def comment-opts
  "List of supported options for the comment component."
  [:upvote
   :parent
   :context
   :edit
   :delete
   :ref   ; reference to the original item
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
       (list
        [:span " | "]
        [:a.link {:href (str "/comment?id=" parent)} "parent"]))
     (when (in? opts :context)
       (list
        [:span " | "]
        [:a.link {:href (str "/item?id=" (:id item) "#" id)} "context"]))
     (when (in? opts :delete)
       (list
        [:span " | "]
        [:a.link {:href (str "/delete?id=" id)} "delete"]))
     (when (in? opts :ref)
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
        [:upvote :parent :context :delete :ref]
        :req req))
     [:div.item-form
      [:form {:method "post"
              :action (str "update?"
                           (form-encode
                            {:id   (:id item)
                             :type (second (str type))
                             :goto (str "/item?id="
                                        (if (= type :comment)
                                          (str (:id (:item item))
                                               "#"
                                               (:id item))
                                          (:id item)))}))}
       (anti-forgery-field)
       [:textarea.textarea {:name "content" :cols 49 :rows 5}
        (:content item)]
       common/markdown-symbol
       [:div
        [:button {:type "submit"} "Update"]]]]]]))
