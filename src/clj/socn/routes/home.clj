(ns socn.routes.home
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page add-links item-links
                                         add-links-comments
                                         filter-items]]
            [socn.config :refer [env]]
            [socn.utils :as utils]
            [socn.controllers.core :as controller]
            [socn.controllers.items :refer [sort-comments]]))

(defn home-page [req]
  (let [{{:keys [site]} :params} req
        page-size (:items-per-page env)
        items (-> (if (s/valid? :socn.validations/domain site)
                    (db/get-items-with-comments-by-domain
                     {:domain site
                      :offset 0
                      :limit  page-size})
                    (db/get-items-with-comments
                     {:offset 0
                      :limit  page-size}))
                  (filter-items) ; filter flagged/hidden/dead items
                  (add-links req false))]
    (default-page req "home" {:items items})))

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

(defn discussions-page [req]
  (let [page-size (:items-per-page env)
        items (-> (controller/get-discussions 0 page-size)
                  (filter-items)
                  (add-links req false))]
    (default-page req "home" {:items items})))

(defn error-page [req]
  (let [{:keys [params]} req]
   (default-page req "error" {:error (:error params)})))

(defn about-page [req]
  (let [{:keys [params]} req]
    (default-page req "about" {})))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"            {:get home-page}]
   ["/news"        {:get home-page}]
   ["/item"        {:get item-page}]
   ["/user"        {:get user-page}]
   ["/discussions" {:get discussions-page}]
   ["/error"       {:get error-page}]
   ["/about"       {:get about-page}]])

