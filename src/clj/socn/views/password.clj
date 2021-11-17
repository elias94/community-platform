(ns socn.views.password
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view [_]
  (html
   [:div.container
    [:div.content
     [:div.profile
      [:h1.title "Change password"]
      [:form.form {:method "post" :action "change-password"}
       [:div.form-field
        [:div.control
         [:input.form-input {:name "current"
                             :type "password"
                             :placeholder "Current password"}]]]
       [:div.form-field
        [:div.control
         [:input.form-input {:name "new"
                             :type "password"
                             :placeholder "New password"}]]]
       (anti-forgery-field)
       [:button.form-button {:name "submit"} "Change"]]]]]))
