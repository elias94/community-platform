(ns socn.views.submit
  (:require [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn view []
  (html
   [:section.section
    [:div.container
     [:h1.title "Submit"]
     [:div.block
      [:form {:method "post" :action "submit"}
       (anti-forgery-field)
       [:div.field
        [:div.control
         [:input {:class "input" :name "title" :placeholder "Title"}]]]
       [:div.field
        [:div.control
         [:input {:class "input" :name "url" :placeholder "Url"}]]
        [:p.help "Leave the url field blank to submit a question for discussion below."]]
       [:label.label "or"]
       [:div.field
        [:div.control
         [:textarea {:class "textarea" :name "content" :placeholder "Question" :rows "5" :cols "49"}]]]
       [:button {:class "button is-info" :name "submit"} "Submit"]]]]]))
