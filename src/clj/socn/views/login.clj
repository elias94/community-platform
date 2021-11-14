(ns socn.views.login
  (:require [hiccup.core :refer [html]]
            [hiccup.form :as form]
            [socn.views.common :refer [notification]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view [{:keys [error error-signup username]}]
  (html
   [:div.container
    [:div.content
     [:section.section
      (when error
        (notification error))
      [:h1.title "Login"]
      [:form.form {:method "post" :action "login"}
       [:div.form-field
        [:div.control
         [:input.form-input {:name "username"
                             :placeholder "Username"
                             :value username}]]]
       [:div.form-field
        [:div.control
         [:input.form-input {:name "password"
                             :type "password"
                             :placeholder "Password"}]]]
       (anti-forgery-field)
       [:button.form-button {:name "submit"} "Login"]]
      [:div
       [:a.link {:href "/reset-password"} "forgot your password?"]]]
     [:section.section
      (when error-signup
        (notification error-signup))
      [:h1.title "Create Account"]
      [:form.form {:method "post" :action "signup"}
       [:div.form-field
        [:div.control
         [:input.form-input {:name "username"
                             :placeholder "Username"}]]]
       [:div.form-field
        [:div.control
         [:input.form-input {:name "password"
                             :type "password"
                             :placeholder "Password"}]]]
       (anti-forgery-field)
       [:button.form-button {:name "submit"} "Sign up"]]]]]))
