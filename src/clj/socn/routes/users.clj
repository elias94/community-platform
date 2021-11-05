(ns socn.routes.users
  (:require [clojure.string :as string]
            [socn.layout :as layout]
            [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [with-template]]
            [buddy.auth :refer [authenticated?]]
            [socn.layout :refer [error-page]]
            [socn.controllers.users :as users-controller]))

(defn user-page [{:keys [params session] :as req}]
  (let [{:keys [id]} params
        {:keys [identity]} session]
    (if (string/blank? id)
      (redirect "/")
      (let [is-user (= id (:id identity))
            user (if is-user
                   identity
                   (dissoc (db/get-user {:id id}) :password))]
        (layout/render-page
         "home.html"
         {:content (with-template req "user"
                     :user user
                     :is-user is-user)})))))

(defn update-user [{:keys [params session] :as req}]
  (if (authenticated? req)
    (let [{{:keys [id]} :identity} session
          user (users-controller/update-user! (assoc params :id id))]
      (-> (redirect (str "/user?id=" id))
          ;; update the identity into the current session
          (assoc :session (assoc session :identity (dissoc user :password)))))
    (error-page {:status 401, :title "401 - Unauthorized"})))

(defn users-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/user"   {:get user-page}]
   ["/update" {:post update-user}]])
