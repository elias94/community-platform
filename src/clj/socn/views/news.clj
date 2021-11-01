(ns socn.views.news
  (:require [hiccup.core :refer [html]]
            [socn.views.utils :refer [plural]]
            [java-time :as t]))

(defn news-desc [news]
  (let [{:keys [score author submitted]} news]
    (str score
         " "
         (plural score "point")
         " by "
         author
         " "
         "==date==")))

(defn news-view [news index]
  (let [{:keys [id domain]} news]
    [:div
     [:div
      [:span (str index ".")]
      [:a {:href (str "/item?id=" id)}
       [:h1 (:title news)]]
      [:span (str "(" domain ")")]]
     [:div
      [:span (news-desc news)]
      " | "
      [:span "flag"]
      " | "
      [:span "hide"]
      " | "
      [:span "comments"]]]))

(defn view [& {:keys [items]}]
  (html
   [:section.section
    [:div.container
     (for [[index item] (map-indexed vector items)]
       ^{:keys (:id item)}
       (news-view item index))]]))
