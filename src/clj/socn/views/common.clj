(ns socn.views.common
  (:require [hiccup.core :refer [html]]
            [socn.views.utils :refer [class-names]]
            [socn.config :refer [env]]
            [buddy.auth :refer [authenticated?]]
            [ring.util.codec :refer [url-encode]]))

(defn upvote
  "Upvote component for item and comment."
  ([item]
   (let [url (str "/vote?id="
                  (:id item)
                  "&type=item&dir=up&goto="
                  (url-encode (str "/item?id=" (:id item))))]
     [:a.arrow-vote
      {:title "Upvote" :href url}]))
  ([comment item]
   (let [url (str "/vote?id="
                  (:id comment)
                  "&type=comment&dir=up&goto="
                  (url-encode (str "/item?id="
                                   (:id item)
                                   "#"
                                   (:id comment))))]
     [:a.arrow-vote
      {:title "Upvote comment" :href url}])))

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

(def markdown-symbol
  [:div.markdown-container
   [:a.markdown {:href "https://docs.github.com/github/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax"
                 :target "_blank"
                 :title "Markdown styling supported"}
    [:svg {:viewBox "0 0 16 16" :height "18"}
     [:path {:fill-rule "evenodd" :d "M14.85 3H1.15C.52 3 0 3.52 0 4.15v7.69C0 12.48.52 13 1.15 13h13.69c.64 0 1.15-.52 1.15-1.15v-7.7C16 3.52 15.48 3 14.85 3zM9 11H7V8L5.5 9.92 4 8v3H2V5h2l1.5 2L7 5h2v6zm2.99.5L9.5 8H11V5h2v3h1.5l-2.51 3.5z"}]]]])
