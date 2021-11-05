(ns socn.views.item
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [socn.views.utils :refer [plural age text-age]]
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
    [:span.arrow-vote {:title "Upvote"}]]
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
    [:div.markdown-container
     [:a.markdown {:href "https://docs.github.com/github/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax"
                   :target "_blank"
                   :title "Markdown styling supported"}
      [:svg {:viewBox "0 0 16 16" :height "18"}
       [:path {:fill-rule "evenodd" :d "M14.85 3H1.15C.52 3 0 3.52 0 4.15v7.69C0 12.48.52 13 1.15 13h13.69c.64 0 1.15-.52 1.15-1.15v-7.7C16 3.52 15.48 3 14.85 3zM9 11H7V8L5.5 9.92 4 8v3H2V5h2l1.5 2L7 5h2v6zm2.99.5L9.5 8H11V5h2v3h1.5l-2.51 3.5z"}]]]]
    [:div
     [:button {:type "submit"} "Comment"]]]])

(defn comment-view [{:keys [author content submitted score]}]
  [:div.comment
   [:div.comment-content
    [:span.arrow-vote {:title "Upvote comment"}]
    [:div.comment-header
     [:span (str (plural score "point")
                 " by "
                 author
                 " "
                 (text-age (age submitted :minutes)))]
     [:span " | prev | next | edit | delete [-]"]]]
   [:div.comment-content
    [:span]
    [:span content]]
   [:div.comment-content
    [:span]
    [:a.link "reply"]]])

(defn view [& {:keys [item comments req]}]
  (html
   [:div.container
    [:div.content
     (item-view (assoc item :comments (count comments)))
     (when (authenticated? req)
       (comment-form item))
     (for [comment comments]
       (comment-view comment))]]))
