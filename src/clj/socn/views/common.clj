(ns socn.views.common
  (:require [hiccup.core :refer [html]]
            [socn.views.utils :refer [class-names]]
            [socn.config :refer [env]]
            [buddy.auth :refer [authenticated?]]))

(defn navbar-item
  "Navbar menu item, highlighted if is the current path."
  [route-name current-route & {:keys [path]}]
  (let [route     (or path (str "/" route-name))
        is-active (= route-name current-route)]
    [:a {:class (class-names ["navbar-item" (when is-active "active")])
         :href route}
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

(defn notification
  "Displays a notification message."
  [content]
  [:div.notification content])
