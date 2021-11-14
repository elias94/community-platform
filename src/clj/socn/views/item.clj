(ns socn.views.item
  (:require [hiccup.core :refer [html]]
            [socn.views.utils :refer [plural age text-age author? encode-url
                                      with-sep]]
            [socn.views.common :as common]
            [buddy.auth :refer [authenticated?]]))

(defn item-desc [news]
  (let [{:keys [id score author submitted]} news]
    [:span
     (str (plural score "point") " by ")
     [:a.news-info {:href (encode-url "user" {:id author})} author]
     [:span " "]
     [:a.news-info {:href (encode-url "item" {:id id})}
      (text-age (age submitted :minutes))]]))

(defn item-view [{:keys [id title domain] :as news} links comments]
  [:div.news
   [:div.news-pre
    (when (:vote links)
      (common/upvote news))]
   [:div.news-header
    [:a.news-link {:href (encode-url "item" {:id id})}
     [:h1.news-title title]]
    [:a.news-info {:href (str "?site=" domain)}
     [:span.news-domain (str "(" domain ")")]]]
   [:span]
   [:div.news-footer
    (item-desc news)
    (when (:flag links)
      (with-sep
        [:a.news-info {:href (encode-url "flag" {:id id})} "flag"]))
    (when (:hide links)
      (with-sep [:span "hide"]))
    (with-sep
      [:a.news-info {:href (encode-url "item" {:id id})
                     :title "Open discussion"}
       [:span (plural comments "comment")]])
    (when (:delete links)
      (with-sep
        [:a.news-info {:href (encode-url "delete" {:id id})
                       :title "Delete item"}
         "delete"]))]])

(defn comment-view [comment req lvl]
  (let [{:keys [id author content submitted score links]} comment
        owned (author? comment req)]
    [:div.comment-wrapper
     [:div.comment-indent
      {:style (str "width: " (* 25 lvl) "px")}]
     [:div.comment {:id id}
      [:div.comment-content
       (if-not owned
         (common/upvote comment)
         [:span "*"])
       [:div.comment-header
        [:span (str (plural score "point")
                    " by " author " "
                    (text-age (age submitted :minutes)))]
        [:span " | prev | next"]
        (when owned
          (with-sep
            [:a.link {:href (encode-url "edit" {:id id :type "c"})
                      :title "Edit comment"}
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
         "reply"]]]]]))

(defn comment-with-sub [comment req lvl]
  (list
   (comment-view comment req lvl)
   (doall
    (for [c (:children  comment)]
      (comment-with-sub c req (inc lvl))))))

(defn view [{:keys [item links comments req]}]
  (html
   [:div.container
    [:div.content
     (item-view item links (count comments))
     (when (authenticated? req)
       (common/comment-form
        item
        (encode-url "comment" {:item (:id item)})
        "Comment"))
     [:div.comments
      (for [comment comments]
        (comment-with-sub comment req 0))]]]))
