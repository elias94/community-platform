(ns socn.routes.actions
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [socn.layout :as layout]
            [socn.utils :as utils]
            [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [with-template]]
            [socn.controllers.items :as items-controller]))

(defn domain-name
  "Extract the domain from the url."
  [url]
  (let [domain (.getHost (java.net.URI. url))]
    (if (string/starts-with? domain "www.")
      (subs domain 4)
      domain)))

(defn submit-page [req]
  (layout/render-page
   "home.html"
   {:content (with-template req "submit")}))

(defn submit-item [{:keys [params session]}]
  (let [{{user-id :id} :identity}   session
        {:keys [title url content]} params]
    ;; TODO Test input or try/catch
    (let [item-id (items-controller/create-item!
                   {:author    user-id
                    :score     0
                    :submitted (java.util.Date.)
                    :url       url
                    :domain    (domain-name url)
                    :content   content
                    :title     title})]
      (redirect (str "/item?id=" item-id)))))

(defn submit-comment [{:keys [params session]}]
  (let [{{user-id :id} :identity}     session
        {:keys [comment item parent]} params]
    (items-controller/create-comment!
     {:author    user-id
      :item      (if (string? item)
                   (utils/parse-int item)
                   item)
      :content   comment
      :score     1
      :parent    parent
      :submitted (java.util.Date.)})
    (redirect (str "/item?id=" item))))

(defn actions-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                 middleware/wrap-restricted]}
   ["/submit"  {:get submit-page
                :post submit-item}]
   ["/comment" {:post submit-comment}]])