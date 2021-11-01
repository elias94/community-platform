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
    [:a {:class (class-names ["navbar-item" (when is-active "is-active")])
         :href route}
     route-name]))

(defn navbar
  "Navbar component. Receive the current path. Ex: (navbar \"home\")"
  [req route]
  (let [{:keys [identity]} (:session req)]
    (println identity)
    (println (:identity req))
    (println (authenticated? req)))
  (html
   [:nav.navbar
     [:div.navbar-menu
      [:div.navbar-start
       [:div.navbar-brand
        [:a.navbar-item {:href "/"} (:project-name env)]]
       (navbar-item "home" route :path "/")
       (when (authenticated? req) (navbar-item "submit" route))]
      [:div.navbar-end
       (if (authenticated? req)
         (navbar-item "logout" route)
         (navbar-item "login" route))]]]))

(defn notification
  "Displays a notification component."
  [content]
  [:div.notification content])
