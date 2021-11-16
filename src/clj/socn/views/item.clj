(ns socn.views.item
  (:require [hiccup.core :refer [html]]
            [socn.views.utils :refer [plural age text-age author? encode-url
                                      with-sep]]
            [socn.views.common :as common]
            [buddy.auth :refer [authenticated?]]))

(defn comment-view [comment req lvl]
  (let [{:keys [id author content submitted score links]} comment]
    [:div.comment-wrapper
     [:div.comment-indent
      {:style (str "width: " (* 40 lvl) "px")}]
     [:div.comment {:id id}
      [:div.comment-content
       [:div.comment-toggle
        [:div "[-]"]]
       [:div.comment-header
        [:span (str (plural score "point")
                    " by " author " "
                    (text-age (age submitted :minutes)))]
        (when (:parent links)
          (with-sep
            [:a.link {:href (str "#" (:parent links))} "parent"]))
        (when (:prev links)
          (with-sep
            [:a.link {:href (str "#" (:prev links))} "prev"]))
        (when (:next links)
          (with-sep
            [:a.link {:href (str "#" (:next links))} "next"]))
        (when (:edit links)
          (with-sep
            [:a.link {:href (encode-url "edit" {:id id :type "c"})
                      :title "Edit comment"}
             "edit"]))
        (when (:delete links)
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
        (if (:vote links)
          (common/upvote comment)
          [:span "*"])
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
