(ns socn.routes.home
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page]]
            [socn.config :refer [env]]
            [socn.utils :as utils]
            [socn.controllers.core :as controller]
            [socn.controllers.items :as item-controller]))

(defn home-page [{:keys [params] :as req}]
  (let [{:keys [site]} params
        page-size (:items-per-page env)
        items     (if (s/valid? :socn.validations/domain site)
                    (db/get-items-with-comments-by-domain {:domain site
                                                           :offset 0
                                                           :limit page-size})
                    (db/get-items-with-comments {:offset 0 :limit page-size}))]
    (default-page req "home" :items items)))

(defn item-page [{:keys [params] :as req}]
  (if (string/blank? (:id params))
    (redirect "/")
    (try
      (let [id       (utils/parse-int (:id params))
            item     (db/get-item {:id id})
            comments (db/get-comments-by-item {:item id :offset 0 :limit 100})
            sorted   (item-controller/sort-comments comments)]
        (default-page req "item" :item item :comments sorted))
      (catch Exception _
        (when-not (:dev env)
          (redirect "/"))))))

(defn user-page [req]
  (let [{{:keys [id]}       :params
         {:keys [identity]} :session} req]
    (if (string/blank? id)
      (redirect "/")
      (let [is-user (= id (:id identity))
            user (if is-user
                   identity
                   (dissoc (controller/get "user" {:id id}) :password))]
        (default-page req "user" :user user :is-user is-user)))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"       {:get home-page}]
   ["/news"   {:get home-page}]
   ["/item"   {:get item-page}]
   ["/user"   {:get user-page}]])

