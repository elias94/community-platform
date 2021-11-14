(ns socn.routes.home
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page]]
            [socn.config :refer [env]]
            [socn.session :as session]
            [socn.utils :as utils]
            [socn.controllers.core :as controller]
            [socn.controllers.items :refer [sort-comments can-flag?
                                            can-edit?]]))

(defn- item-links
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
       :vote   (controller/exists-vote user-id (:id item))})
    {}))

(defn- add-links
  "Add links to the sequence of items."
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
                      :vote   (controller/exists-vote
                               user-id
                               (:id item))}))]
      (map map-fn items))
    items))

(defn- comment-links
  "Add links to comment recursively."
  [comment req parent opts]
  (let [{:keys [prev next lvl root]} opts]
    {:parent parent
     :prev   prev
     :next   next
     :edit   ()
     :delete}))

(defn- add-links-comments
  "Add links to the sequence of comment."
  [comments req]
  ())

(defn home-page [req]
  (let [{{:keys [site]} :params} req
        page-size (:items-per-page env)
        items     (if (s/valid? :socn.validations/domain site)
                    (db/get-items-with-comments-by-domain
                     {:domain site
                      :offset 0
                      :limit  page-size})
                    (db/get-items-with-comments {:offset 0
                                                 :limit  page-size}))]
    (default-page req "home" {:items (add-links items req false)})))

(defn item-page [req]
  (let [{{:keys [id]} :params} req]
    (if (string/blank? id)
      (redirect "/")
      (try
        (let [id       (utils/parse-int id)
              item     (controller/get "item" {:id id})
              comments (-> (db/get-comments-by-item
                            {:item id :offset 0 :limit 100})
                           (sort-comments)
                           (add-links-comments req))]
          (default-page req "item"
            {:item     item
             :links    (item-links item req true)
             :comments comments}))
        (catch Exception e
          (if-not (:dev env)
            (redirect "/")
            (log/error e)))))))

(defn user-page [req]
  (let [{{:keys [id]}       :params
         {:keys [identity]} :session} req]
    (if (string/blank? id)
      (redirect "/")
      (let [is-user (= id (:id identity))
            user (if is-user
                   identity
                   (dissoc (controller/get "user" {:id id}) :password))]
        (default-page req "user" {:user user :is-user is-user})))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"       {:get home-page}]
   ["/news"   {:get home-page}]
   ["/item"   {:get item-page}]
   ["/user"   {:get user-page}]])

