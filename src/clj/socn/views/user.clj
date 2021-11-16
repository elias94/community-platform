(ns socn.views.user
  (:require [clojure.string :as string]
            [hiccup.core :refer [html]]
            [socn.views.utils :refer [plural age text-age]]
            [buddy.auth :refer [authenticated?]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view [{:keys [user is-user req]}]
  (let [{:keys [id created karma about email showall]} user]
    (html
     [:div.container
      [:div.content
       [:div.profile
        [:form {:method "post" :action "update-user"}
         (anti-forgery-field)
         [:div.grid
          [:span "Username:"]
          [:span id]
          [:span "Created:"]
          [:span (text-age (age created :minutes))]
          [:span "Karma:"]
          [:span {:title "Your karma points achieved"} karma]
          (when (or is-user (not (string/blank? about)))
            (list
             [:span "About:"]
             (if is-user
               [:textarea.textarea {:name "about" :cols 45 :rows 5}
                about]
               [:span about])))
          (when is-user
            (list
             [:span "Email:"]
             [:div.form-field
              [:input {:class "input" :type "text"
                       :name "email" :value email}]
              [:span.help "Your email is private and used only for password reset."]]
             [:span "Show all:"]
             [:div
              [:select {:name "showall"
                        :title "Show all posts and comments, including flagged"}
               [:option {:value true  :selected (true? showall)} "on"]
               [:option {:value false :selected (not showall)}   "off"]]]
             [:span]
             [:a.link {:href "/change-password"} "change password"]))]
         [:div.spacer]
         (when is-user
           [:button {:type "submit"} "Update"])]
        [:div.spacer]
        [:a.link {:href "/change-password" :title "User submissions"}
         "Submission"]
        [:a.link {:href "/change-password" :title "User comments"}
         "Comments"]]]])))
