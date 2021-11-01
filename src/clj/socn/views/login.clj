(ns socn.views.login
  (:require [hiccup.core :refer [html]]
            [hiccup.form :as form]
            [socn.views.common :refer [notification]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view [& {:keys [error error-signup username]}]
  (html
   [:section.section
    [:div.container
     (when error
       (notification error))
     [:h1.title "Login"]
     [:div.block
      [:form {:method "post" :action "login"}
       [:div.field
        [:div.control
         (form/text-field {:class "input"
                           :placeholder "Username"
                           :value username}
                          "username")]]
       [:div.field
        [:div.control
         (form/password-field {:class "input" :placeholder "Password"}
                              "password")]]
       (anti-forgery-field)
       (form/submit-button {:class "button is-info" :name "submit"}
                           "Login")]]
     [:div.block
      [:a {:href "/register"} "forgot your password?"]]]
    [:div.container
     (when error-signup
       (notification error-signup))
     [:h1.title "Create Account"]
     [:div.block
      [:form {:method "post" :action "signup"}
       [:div.field
        [:div.control
         (form/text-field {:class "input" :placeholder "Username"}
                          "username")]]
       [:div.field
        [:div.control
         (form/password-field {:class "input" :placeholder "Password"}
                              "password")]]
       (anti-forgery-field)
       (form/submit-button {:class "button is-info" :name "submit"}
                           "Sign up")]]]]))
