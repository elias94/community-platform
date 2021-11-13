(ns socn.views.home
  (:require [hiccup.core :refer [html]]
            [socn.views.common :as common]
            [socn.views.utils :refer [plural age text-age]]))

(defn news-desc [news]
  (let [{:keys [id score author submitted]} news]
    [:span
     (str (plural score "point")
          " by ")
     [:a.news-info {:href (str "/user?id=" author)} author]
     [:span " "]
     [:a.news-info {:href (str "/item?id=" id)}
      (text-age (age submitted :minutes))]]))

(defn news-view [news index]
  (let [{:keys [id domain comments url]} news]
    [:div.news
     [:div.news-pre
      [:span.news-index (str (+ index 1) ".")]
      (common/upvote news)]
     [:div.news-header
      [:a.news-link {:href url}
       [:h1.news-title (:title news)]]
      [:a.news-info {:href (str "?site=" domain)}
       [:span.news-domain (str "(" domain ")")]]]
     [:span]
     [:div.news-footer
      (news-desc news)
      [:span.separator]
      [:span "flag"]
      [:span.separator]
      [:span "hide"]
      [:span.separator]
      [:a.news-info {:href (str "/item?id=" id) :title "Open discussion"}
       [:span (plural comments "comment")]]]]))

(defn view [& {:keys [items]}]
  (html
   [:div.container
    [:div.content
     (for [[index item] (map-indexed vector items)]
       ^{:keys (:id item)}
       (news-view item index))]]))
