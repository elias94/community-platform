(ns socn.views.item
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [socn.views.utils :refer [plural age text-age author?]]
            [socn.views.common :as common]
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

(defn comment-view [{:keys [id author content submitted score] :as comment} req]
  (let [own (author? comment req)]
    [:div.comment {:id id}
     [:div.comment-content
      (if-not own
        (common/upvote comment)
        [:span "*"])
      [:div.comment-header
       [:span (str (plural score "point")
                   " by "
                   author
                   " "
                   (text-age (age submitted :minutes)))]
       [:span " | prev | next | "]
       (when own
         [:a.link {:href (str "/edit?id=" id "&type=c") :title "Edit comment"}
          "edit"])
       [:span " | delete [-]"]]]
     [:div.comment-content
      [:span]
      [:span content]]
     [:div.comment-content
      [:span]
      [:a.link "reply"]]]))

(defn view [& {:keys [item comments req]}]
  (html
   [:div.container
    [:div.content
     (item-view (assoc item :comments (count comments)))
     (when (authenticated? req)
       (comment-form item))
     [:div.comments
      (for [comment comments]
        (comment-view comment req))]]]))
