(ns socn.routes.home
  (:require [socn.layout :as layout]
            [socn.db.core :as db]
            [conman.core :as conman]
            [clojure.java.io :as io]
            [socn.middleware :as middleware]
            [ring.util.response]
            [socn.routes.common :refer [with-template]]
            [socn.utils :as utils]))

(defn news-page [req]
  (let [items (db/get-items {:offset 0 :limit 30})]
    (layout/render-page
     "home.html"
     {:content (with-template req "home" :items items)})))

(defn item-page [{:keys [params] :as req}]
  (let [item-id (:id params)
        item (db/get-item {:id (utils/parse-int item-id)})]
    (layout/render-page
     "home.html"
     {:content (with-template req "item" :item item)})))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"       {:get news-page}]
   ["/news"   {:get news-page}]
   ["/item"   {:get item-page}]])

