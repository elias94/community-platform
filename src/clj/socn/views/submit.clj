(ns socn.views.submit
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view [_]
  (html
   [:div.container
    [:div.content
     [:section.section
      [:h1.title "Submit"]
      [:div.block
       [:form.form {:method "post" :action "submit"}
        (anti-forgery-field)
        [:div.form-field
         [:div.control
          [:input.form-input {:name "title" :placeholder "Title"
                              :maxlength "80"}]]]
        [:div.form-field
         [:div.control
          [:input.form-input {:name "url" :placeholder "Url"
                              :maxlength "65535"}]]
         [:p.help
          "Leave the url field blank to submit a discussion below."]]
        [:div.label "or"]
        [:div.form-field
         [:div.control
          [:textarea.textara {:class "textarea" :name "content"
                              :placeholder "Write a question or open a discussion"
                              :rows "5" :cols "49"}]]]
        [:button {:class "button is-info" :name "submit"} "Submit"]]]]]]))
  