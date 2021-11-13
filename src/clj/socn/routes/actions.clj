(ns socn.routes.actions
  (:require [clojure.spec.alpha :as s]
            [socn.db.core :as db]
            [socn.utils :as utils]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page]]
            [socn.controllers.core :as controller]
            [socn.controllers.items :refer [can-edit?]]
            [socn.session :as session]
            [socn.views.utils :refer [encode-url]]))

(defn submit-page [req]
  (default-page req "submit"))

(defn edit-page [req]
  (let [{{id :id item-type :type} :params} req
        id        (utils/parse-int id)
        item-type (utils/parse-char item-type)]
    (if (and (s/valid? :item/id id)
             (s/valid? :socn.validations/type item-type))
      (let [item-type (if (= item-type \c) :comment :item)
            user-id   (session/auth :id req)
            user      (when (s/valid? :user/id user-id)
                        (controller/get "user" {:id user-id}))
            item      (if (= item-type :comment)
                        (controller/get "comment" {:id id})
                        (controller/get "item" {:id id}))]
        (if (can-edit? user item)
          (default-page req "edit"
          ;; set :item to the item object related to the comment
            :item (if (= item-type :comment)
                    (->> (controller/get "item" {:id (:item item)})
                         (assoc item :item))
                    item)
            :type item-type)
          (-> (encode-url "item" {:id id})
              (redirect))))
      (throw (Exception. "Invalid id parameter.")))))

(defn submit-item [req]
  (let [{{:keys [title url content]} :params} req
        user-id (session/auth :id req)
        item-id (-> (controller/create!
                     "item"
                     {:author    user-id
                      :score     0
                      :submitted (java.util.Date.)
                      :url       url
                      :domain    (utils/domain-name url)
                      :content   content
                      :title     title})
                    :id)]
    ;; user automatically vote the item (increasing the karma)
    (controller/create-vote user-id item-id \i)
    (redirect (str "/item?id=" item-id))))

(defn save-comment [req]
  (let [{:keys [params session]}      req
        {{user-id :id} :identity}     session
        {:keys [content item parent]} params]
    (->> (controller/create!
          "comment"
          {:author    user-id
           :score     1
           :submitted (java.util.Date.)
           :content   content
           :parent    parent
           :item      (utils/parse-int item)})
         :id
         (str "/item?id=" item "#") ; navigate to comment by id
         (redirect))))

(defn reply-page [req]
  (let [{{:keys [id]} :params} req
        parent (controller/get
                "comment"
                {:id (utils/parse-int id)})
        ext    (->> (controller/get
                     "item"
                     {:id (:item parent)})
                    (assoc parent :item))]
    (default-page req "reply" :parent ext)))

(defn save-vote [req]
  (let [{:keys [id type dir goto]} (:params req)
        user-id (session/auth :id req)
        item    (utils/parse-int id)
        exists  (controller/exists? "vote" {:author user-id :item item})]
    (if (s/valid? :vote/direction dir)
      (do
        (if (= dir "up")
          (if (not exists) ; upvote
            (controller/create-vote user-id item (first type))
            (throw (Exception. "Item already voted.")))
          (if exists ; downvote
            (controller/delete-vote user-id item)
            (throw (Exception. "Invalid vote reference."))))
        (redirect goto)) ; probably not reached
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
        item-type (first type)] ; \c or \i
    (if (s/valid? :socn.validations/type item-type)
      (let [entity (if (= item-type \c) "comment" "item")
            id     (utils/parse-int id)]
        (controller/update! entity {:id      id
                                    :content content
                                    :edited  (java.util.Date.)})
        (redirect (or goto (str "/item?id=" id))))
      (throw (Exception. "Invalid parameters.")))))

(defn delete-item [req]
  (let [{{:keys [id type goto]} :params} req
        item-type (utils/parse-char type)]
    (let [entity (if (= item-type \c) "comment" "item")
          id     (utils/parse-int id)
          user   (session/auth req)
          item   (controller/get entity {:id id})
          goto   (or goto
                     (if (= item-type \c)
                       (encode-url "item" {:id (:item item)})
                       "/"))]
      (if (can-edit? user item)
        (do
          (controller/delete! entity {:id id})
          (redirect goto))
        (throw (Exception. "User cannot edit."))))))

(defn save-comment-reply [req]
  (let [{{:keys [id content]} :params} req
        user-id (session/auth :id req)
        parent  (controller/get "comment" {:id (utils/parse-int id)})]
    (->> (controller/create!
          "comment"
          {:author    user-id
           :score     1
           :submitted (java.util.Date.)
           :content   content
           :parent    (:id parent)
           :item      (utils/parse-int (:item parent))})
         :id
         (str "/item?id=" (:item parent) "#")
         (redirect))))

;; Actions route are restricted to authenticated users only
(defn actions-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats
                 middleware/wrap-restricted]}
   ["/submit"      {:get submit-page
                    :post submit-item}]
   ["/comment"     {:post save-comment}]
   ["/reply"       {:get reply-page
                    :post save-comment-reply}]
   ["/vote"        {:get save-vote}]
   ["/edit"        {:get edit-page}]
   ["/update"      {:post update-item}]
   ["/delete"      {:get delete-item}]
   ["/update-user" {:post update-user}]
   ;; a post flagged becomes [flagged] or [dead]
   ["/flag"]])
