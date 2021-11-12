(ns socn.views.item
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [socn.views.utils :refer [plural age text-age author? encode-url]]
            [socn.views.common :as common :refer [with-sep]]
            [buddy.auth :refer [authenticated?]]))

(defn item-desc [news]
  (let [{:keys [id score author submitted]} news]
    [:span
     (str (plural score "point") " by ")
     [:a.news-info {:href (str "/user?id=" author)} author]
     [:span " "]
     [:a.news-info {:href (str "/item?id=" id)}
      (text-age (age submitted :minutes))]]))

(defn item-view [{:keys [id title domain comments] :as news}]
  [:div.news
   [:div.news-pre
    (common/upvote news)]
   [:div.news-header
    [:a.news-link {:href (str "/item?id=" id)}
     [:h1.news-title title]]
    [:a.news-info {:href (str "?site=" domain)}
     [:span.news-domain (str "(" domain ")")]]]
   [:span]
   [:div.news-footer
    (item-desc news)
    [:span.separator]
    [:span "flag"]
    [:span.separator]
    [:span "hide"]
    [:span.separator]
    [:a.news-info {:href (str "/item?id=" id) :title "Open discussion"}
     [:span (plural comments "comment")]]]])

(defn comment-form [item]
  [:div.item-comment
   [:form {:method "post" :action (str "comment?item=" (:id item))}
    (anti-forgery-field)
    [:textarea.textarea {:name "comment" :cols 49 :rows 5}]
    common/markdown-symbol
    [:div
     [:button {:type "submit"} "Comment"]]]])

(defn comment-view [comment req lvl]
  (let [{:keys [id author content submitted score]} comment
        owned (author? comment req)]
    [:div.comment {:id id}
     [:div.comment-content
      (if-not owned
        (common/upvote comment)
        [:span "*"])
      [:div.comment-header
       [:span (str (plural score "point")
                   " by "
                   author
                   " "
                   (text-age (age submitted :minutes)))]
       [:span " | prev | next"]
       (when owned
         (with-sep
           [:a.link {:href (str "/edit?id=" id "&type=c") :title "Edit comment"}
            "edit"]))
       (when owned
         (with-sep
           [:a.link {:href (encode-url "delete" {:id id :type "c"})
                     :title "Delete comment"}
            "delete"]))]]
     [:div.comment-content
      [:span]
      [:span content]]
     [:div.comment-content
      [:span]
      [:div
       [:a.link {:href (encode-url "reply" {:id id})}
        "reply"]]]]))

(defn comment-with-sub [comment req lvl]
  (list
   (comment-view comment req lvl)
   (doseq [c (:children comment)]
     (comment-with-sub c req (inc lvl)))))

(defn view [& {:keys [item comments req]}]
  (html
   [:div.container
    [:div.content
     (item-view (assoc item :comments (count comments)))
     (when (authenticated? req)
       (comment-form item))
     [:div.comments
      (for [comment comments]
        (comment-with-sub comment req 0))]]]))
