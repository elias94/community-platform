(ns socn.routes.home
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [socn.layout :as layout]
            [socn.db.core :as db]
            [conman.core :as conman]
            [clojure.java.io :as io]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [with-template]]
            [socn.config :refer [env]]
            [socn.utils :as utils]
            [socn.validations :as validations]))

(defn home-page [{:keys [params] :as req}]
  (let [{:keys [site]} params
        page-size (:items-per-page env)
        items     (cond
                    (s/valid? :socn.validations/domain site)
                    (db/get-items-with-comments-by-domain {:domain site
                                                           :offset 0
                                                           :limit page-size})

                    :else
                    (db/get-items-with-comments {:offset 0 :limit page-size}))]
    (layout/render-page
     "home.html"
     {:content (with-template req "home" :items items)})))

(defn item-page [{:keys [params] :as req}]
  (if (string/blank? (:id params))
    (redirect "/")
    (let [id       (utils/parse-int (:id params))
          item     (db/get-item {:id id})
          comments (db/get-comments-by-item {:item id :offset 0 :limit 100})]
      (layout/render-page
       "home.html"
       {:content (with-template req "item"
                   :item item
                   :comments comments)}))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"       {:get home-page}]
   ["/news"   {:get home-page}]
   ["/item"   {:get item-page}]])

