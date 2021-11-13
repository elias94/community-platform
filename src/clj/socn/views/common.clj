(ns socn.views.common
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [socn.views.utils :refer [class-names encode-url plural
                                      author? text-age age with-sep]]
            [buddy.auth :refer [authenticated?]]
            [ring.util.codec :refer [url-encode]]))

;;;;;;;;;;;;;;;;;;;
;; Elements
;;;;;;;;;;;;;;;;;;;

(def markdown-symbol
  [:div.markdown-container
   [:a.markdown {:href "https://docs.github.com/github/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax"
                 :target "_blank"
                 :title "Markdown styling supported"}
    [:svg {:viewBox "0 0 16 16" :height "18"}
     [:path {:fill-rule "evenodd" :d "M14.85 3H1.15C.52 3 0 3.52 0 4.15v7.69C0 12.48.52 13 1.15 13h13.69c.64 0 1.15-.52 1.15-1.15v-7.7C16 3.52 15.48 3 14.85 3zM9 11H7V8L5.5 9.92 4 8v3H2V5h2l1.5 2L7 5h2v6zm2.99.5L9.5 8H11V5h2v3h1.5l-2.51 3.5z"}]]]])

(def ^:dynamic *comment-form-content* false)

(defn comment-form
  "Form to add a comment."
  [item action button-name]
    [:div.item-form
     [:form {:method "post" :action action}
      (anti-forgery-field)
      [:textarea.textarea {:name "content" :cols 49 :rows 5}
       (when *comment-form-content*
         (:content item))]
      markdown-symbol
      [:div
       [:button {:type "submit"} button-name]]]])

(defn upvote
  "Upvote component for item and comment."
  ([item]
   (let [url (encode-url
              "vote"
              {:id (:id item)
               :type "item"
               :dir "up"
               :goto (url-encode (encode-url
                                  "item"
                                  {:id (:id item)}))})]
     (if (:voted item)
       [:span]
       [:a.arrow-vote
        {:title "Upvote" :href url
         :onclick "return vote(event, this)"}])))
  ([comment item]
   (let [url (encode-url
              "vote"
              {:id (:id comment)
               :type "comment"
               :dir "up"
               :goto (url-encode (encode-url
                                  "item"
                                  {:id (:id item)}
                                  (:id comment)))})]
     (if (:voted comment)
       [:span]
       [:a.arrow-vote
        {:title "Upvote comment" :href url
         :onclick "return vote(event, this)"}]))))

;; List of supported options for the comment component
(def ^:dynamic *comment-view-upvote*  false)
(def ^:dynamic *comment-view-parent*  false)
(def ^:dynamic *comment-view-context* false)
(def ^:dynamic *comment-view-edit*    false)
(def ^:dynamic *comment-view-flag*    false)
(def ^:dynamic *comment-view-delete*  false)
(def ^:dynamic *comment-view-on*      false)
(def ^:dynamic *comment-view-reply*   false)

(defn comment-view
  "Comment view for comment page - not item page.
   
  Available bindings:
   *comment-view-upvote*
   *comment-view-parent*
   *comment-view-context*
   *comment-view-edit*
   *comment-view-flag*
   *comment-view-delete*
   *comment-view-on*
   *comment-view-reply*"
  [{:keys [id score submitted author
           content parent item] :as comment}
   & {:keys [req]}]
  [:div.comment {:id id}
   [:div.comment-content
    (if *comment-view-upvote*
      (if-not (author? comment req)
        (upvote comment)
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
     (when (and *comment-view-parent* (int? parent))
       (with-sep
         [:a.link {:href (encode-url "comment" {:id parent})} "parent"]))
     (when *comment-view-context*
       (with-sep
         [:a.link {:href (encode-url "item" {:id (:id item)} id)}
          "context"]))
     (when *comment-view-flag*
       (with-sep
         [:a.link {:href (encode-url "flag" {:id id})}
          "flag"]))
     (when *comment-view-delete*
       (with-sep
         [:a.link {:href (encode-url "delete" {:id id})}
          "delete"]))
     (when *comment-view-on*
       (list
        [:span " | on: "]
        [:a.link {:href (str "/item?id=" (:id item))}
         (:title item)]))]]
   [:div.comment-content
    [:span]
    [:span content]]
   (when *comment-view-reply*
     [:div.comment-content
      [:span]
      [:div.comment-footer
       [:a.link "reply"]]])])

(defn navbar-item
  "Navbar menu item, highlighted if is the current path."
  [route-name current-route & {:keys [path]}]
  (let [is-active (= route-name current-route)]
    [:a {:class (class-names ["navbar-item"
                              (when is-active "active")])
         :href (or path (str "/" route-name))}
     route-name]))

(defn navbar
  "Navbar component. Receive the current path. Ex: (navbar \"home\")"
  [req route]
  (html
   [:nav.navbar
    [:div.container
     [:div.navbar-menu
      [:div.navbar-start
       [:div.navbar-brand
        [:a.navbar-item {:href "/"} "SOC"]]
       (navbar-item "projects" route)
       (when (authenticated? req) (navbar-item "submit" route))]
      [:div.navbar-end
       (if (authenticated? req)
         (let [{{:keys [id karma]} :identity} (:session req)]
           [:a {:href (str "/user?id=" id)}
            [:span.navbar-user (str id " (" karma ")")]
            (navbar-item "logout" route)])
         (navbar-item "login" route))]]]]))

(defn footer
  "Footer component for the page template."
  []
  (html
   [:footer.footer
    [:div
     [:a.link {:href "/guidelines"} "Guidelines"]
     [:a.link {:href "/faq"}        "FAQ"]
     [:a.link {:href "/about"}      "About"]]]))

(defn notification
  "Displays a notification message."
  [content]
  [:div.notification content])
