(ns socn.routes.common
  (:require [clojure.string :as string]
            [ring.util.response :refer [redirect]]
            [socn.layout :as layout]
            [socn.views.common :as common]
            [socn.views.login :as login]
            [socn.views.home :as home]
            [socn.views.item :as item]
            [socn.views.user :as user]
            [socn.views.edit :as edit]
            [socn.views.reply :as reply]
            [socn.views.submit :as submit]
            [socn.views.password :as password]
            [socn.views.error :as error]
            [socn.views.about :as about]
            [socn.session :as session]
            [socn.controllers.items :refer [can-flag? can-edit? author?
                                            flag-kill-threshold]]
            [socn.controllers.core :as controller]))

(defn with-template
  "Render the view using the default page template.
  Includes a parameter :req into the view args."
  [req page-name args]
  (let [ext-args    (assoc args :req req)
        view-render (case page-name
                      "home"     (apply home/view   [ext-args])
                      "login"    (apply login/view  [ext-args])
                      "submit"   (apply submit/view [ext-args])
                      "item"     (apply item/view   [ext-args])
                      "user"     (apply user/view   [ext-args])
                      "edit"     (apply edit/view   [ext-args])
                      "reply"    (apply reply/view  [ext-args])
                      "password" (apply password/view [ext-args])
                      "error"    (apply error/view  [ext-args])
                      "about"    (apply about/view  [ext-args]))]
    (when view-render
      (string/join [(common/navbar req page-name)
                    view-render
                    (common/footer)]))))

(defn default-page
  "Default page template for the entire app."
  [req route args]
  (layout/render-page
   "home.html"
   {:content (apply with-template req route [args])}))

(defn error-page
  "Redirect to error page with the message body."
  [error]
  (-> (redirect "/error")
      (assoc :body error)))

(defn vote-link
  "Return true if vote link should be visible."
  [user item]
  (and (not (author? user item))
       (not (controller/exists-vote (:id user) (:id item)))))

(defn item-links
  "Create a map of available links for display
  the item.
  
  Set extend to true if inside the item page."
  [item req extended]
  (if (session/authenticated? req)
    (let [user-id (session/auth :id req)
          user    (controller/get-user user-id)]
      {:flag   (can-flag? user)
       :hide   true
       :delete (and extended (can-edit? user item))
       :vote   (vote-link user item)})
    {}))

(defn add-links
  "Add :links to the sequence of items."
  [items req extended]
  (if (session/authenticated? req)
    (let [user-id (session/auth :id req)
          user    (controller/get-user user-id)
          map-fn  (fn [item]
                    (assoc
                     item
                     :links
                     {:flag   (can-flag? user)
                      :hide   true
                      :delete (and extended (can-edit? user item))
                      :vote   (vote-link user item)}))]
      (map map-fn items))
    items))

(defn- comment-links
  "Add :links to comments recursively."
  [comments parent user user-id]
  (loop [index 0
         items comments
         coll  []]
    (let [curr (first items)
          prev (nth comments (dec index) nil)
          next (nth comments (inc index) nil)]
      (if curr
        (->> (-> (assoc
                  curr
                  :children
                  (comment-links (:children curr) curr user user-id))
                 (assoc :links {:parent parent
                                :prev   (when (> index 0) (:id prev))
                                :next   (when (< index (count comments))
                                          (:id next))
                                :edit   (can-edit? user curr)
                                :delete (can-edit? user curr)
                                :vote   (vote-link user curr)}))
             (conj coll)
             (recur (inc index) (rest items)))
        coll))))

(defn add-links-comments
  "Add :links to the sequence of comments."
  [comments req]
  (let [user-id (session/auth :id req)
        user    (controller/get-user user-id)]
    (comment-links comments nil user user-id)))

(defn filter-items
  "Remove items."
  [items]
  (filter
   (fn [item]
     ;; Remove flagged items
     (< (controller/get-flagged-count (:id item)) flag-kill-threshold))
   items))
