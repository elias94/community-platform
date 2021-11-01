(ns socn.routes.common
  (:require [clojure.string :as string]
            [socn.layout :as layout]
            [socn.views.common :as common]
            [socn.views.login :as login]
            [socn.views.news :as news]
            [socn.views.item :as item]
            [socn.views.submit :as submit]))

(defn with-template
  "Render the page with the default components as header,..."
  [req page-name & args]
  (let [view-render (case page-name
                      "home"   (apply news/view   args)
                      "login"  (apply login/view  args)
                      "submit" (apply submit/view args)
                      "item"   (apply item/view   args))]
    (when view-render
      (string/join [(common/navbar req page-name)
                    view-render]))))

(defn default-page
  "Default page template for the entire app."
  [req route & args]
  (layout/render-page
   "home.html"
   {:content (apply with-template req route args)}))
