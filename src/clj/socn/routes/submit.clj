(ns socn.routes.submit
  (:require [clojure.string :as string]
            [socn.layout :as layout]
            [socn.db.core :as db]
            [conman.core :as conman]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [with-template]]))

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
  (let [{{user-id :id} :identity} session
        {:keys [title url content]} params]
    (let [item-id (db/create-item!
                   {:author    user-id
                    :score     0
                    :submitted (java.util.Date.)
                    :url       url
                    :domain    (domain-name url)
                    :content   content
                    :title     title})]
      (redirect (str "/item?id=" item-id)))))

(defn submit-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                 middleware/wrap-restricted]}
   ["/submit" {:get submit-page
               :post submit-item}]])
