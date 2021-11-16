(ns socn.views.home
  (:require [hiccup.core :refer [html]]
            [socn.views.common :as common]
            [socn.views.utils :refer [plural age text-age
                                      with-sep encode-url]]))

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
  (let [{:keys [id domain comments url links]} news]
    [:div.news
     [:div.news-head
      [:div.news-pre
       (if (:vote links)
         (common/upvote news))
       [:span.news-index (str (+ index 1) ".")]]
      [:div.news-header
       [:a.news-link {:href (if (string? url)
                              url
                              (encode-url "item" {:id id}))}
        [:h1.news-title (:title news)]]
       (when (string? domain)
         [:a.news-info {:href (str "?site=" domain)}
          [:span.news-domain (str "(" domain ")")]])]
      [:span]
      [:div.news-footer
       (news-desc news)
       (when (:flag links)
         (with-sep
           [:a.news-info {:href (encode-url "flag" {:id id})} "flag"]))
       (when (:hide links)
         (with-sep [:span "hide"]))
       (with-sep
         [:a.news-info {:href (encode-url "item" {:id id})
                        :title "Open discussion"}
          [:span (plural comments "comment")]])]]]))

(defn view [{:keys [items]}]
  (html
   [:div.container
    [:div.content
     (for [[index item] (map-indexed vector items)]
       ^{:keys (:id item)}
       (news-view item index))]]))
