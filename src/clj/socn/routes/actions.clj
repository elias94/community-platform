(ns socn.routes.actions
  (:require [clojure.spec.alpha :as s]
            [socn.db.core :as db]
            [socn.utils :as utils]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page]]
            [socn.controllers.core :as controller]
            [socn.controllers.items :as items-controller]
            [socn.session :as session]))

(defn submit-page [req]
  (default-page req "submit"))

(defn edit-page [req]
  (let [{:keys [params]}  req
        {id :id item-type :type} params
        id        (utils/parse-int id)
        item-type (utils/parse-char item-type)]
    (if (and (s/valid? :item/id id)
             (s/valid? :socn.validations/type item-type))
      (let [item-type (if (= item-type \c) :comment :item)
            item      (if (= item-type :comment)
                        (controller/get "comment" {:id id})
                        (controller/get "item" {:id id}))
            ext-item  (if (= item-type :comment)
                        (->> (controller/get "item" {:id (:item item)})
                             (assoc item :item))
                        item)]
        (default-page req "edit"
          :item ext-item
          :type item-type))
      (throw (Exception. "Invalid id parameter.")))))

(defn submit-item [req]
  (let [{:keys [params session]}    req
        {{user-id :id} :identity}   session
        {:keys [title url content]} params]
    (->> (controller/create!
          "item"
          {:author    user-id
           :score     0
           :submitted (java.util.Date.)
           :url       url
           :domain    (utils/domain-name url)
           :content   content
           :title     title})
         :id ; query return a map {:id id}
         (str "/item?id=")
         (redirect))))

(defn save-comment [req]
  (let [{:keys [params session]}      req
        {{user-id :id} :identity}     session
        {:keys [comment item parent]} params]
    (->> (controller/create!
          "comment"
          {:author    user-id
           :score     1
           :submitted (java.util.Date.)
           :content   comment
           :parent    parent
           :item      (utils/parse-int item)})
         :id
         (str "/item?id=" item "#") ; navigate to comment by id
         (redirect))))

(defn save-vote [req]
  (let [{:keys [id type dir goto]} (:params req)
        user-id (session/auth :id req)
        item    (utils/parse-int id)
        exists  (controller/exists? "vote" {:author user-id :item item})]
    (if (s/valid? :vote/direction dir)
      ((if (= dir "up")
         (if (not exists) ; upvote
           (do
             (controller/create!
              "vote"
              {:author    user-id
               :item      item
               :type      (first type)
               :submitted (java.util.Date.)})
             (redirect goto))
           (throw (Exception. "Item already voted.")))
         (if exists ; downvote
           (do
             (controller/delete!
              "vote"
              {:author    user-id
               :item      item})
             (redirect goto))
           (throw (Exception. "Invalid vote reference.")))))
      (throw (Exception. "Invalid direction parameter.")))))

(defn update-user [{:keys [params session] :as req}]
  (let [{{:keys [id]} :identity} session
        conv-params (utils/parse-bool-map params :showall)]
    (controller/update! "user" (assoc conv-params :id id))
    (let [user (-> (db/get-user {:id id})
                   (dissoc :password))]
      (-> (redirect (str "/user?id=" id))
          ;; update the identity into the current session
          (assoc :session (assoc session :identity user))))))

(defn update-item [req]
  (let [{{:keys [id type content goto]} :params} req
        item-type (utils/parse-char type)]
    (if (s/valid? :socn.validations/type item-type)
      (let [entity (if (= item-type \c) "comment" "item")
            id     (utils/parse-int id)
            user   (session/auth req)
            item   (controller/get entity {:id id})]
        (if (items-controller/can-edit? user item)
          (do
            (controller/update! entity {:id      id
                                        :content content
                                        :edited  (java.util.Date.)})
            (redirect (or goto (str "/item?id=" id))))
          (throw (Exception. "User cannot edit."))))
      (throw (Exception. "Invalid parameters.")))))

(defn delete-item [req]
  (let [{{:keys [id type goto]} :params} req
        item-type (utils/parse-char type)]
    (if (s/valid? :socn.validations/type item-type)
      (let [entity (if (= item-type \c) "comment" "item")
            id     (utils/parse-int id)
            user   (session/auth req)
            item   (controller/get entity {:id id})]
        (if (items-controller/can-edit? user item)
          (do
            (controller/delete! entity {:id id})
            (redirect (or goto (str "/item?id=" id))))
          (throw (Exception. "User cannot edit."))))
      (throw (Exception. "Invalid parameters.")))))

;; Actions route are restricted to authenticated users only
(defn actions-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                 middleware/wrap-restricted]}
   ["/submit"      {:get submit-page
                    :post submit-item}]
   ["/comment"     {:post save-comment}]
   ["/vote"        {:get save-vote}]
   ["/edit"        {:get edit-page}]
   ["/update"      {:post update-item}]
   ["/delete"      {:get delete-item}]
   ["/update-user" {:post update-user}]])
