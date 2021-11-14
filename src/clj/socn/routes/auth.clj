(ns socn.routes.auth
  (:require [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page]]
            [socn.controllers.core :as controller]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated?]]))

(defn login-page [req]
  (if (authenticated? req)
    (redirect "/")
    (default-page req "login" {})))

(defn handle-login
  "Check the request for username and password
  and if present, check the match against the db record."
  [{:keys [params session] :as req}]
  (let [{:keys [username password]} params]
    (if (and (seq username) (seq password))
      ;; login and redirect
      (let [user (db/get-user {:id username})]
        (if (and user (hashers/verify password (:password user)))
          (-> (redirect "/")
              (assoc :session (assoc session :identity (dissoc user :password)))) ; set-user!
          (default-page req "login" {:error "Login failed. Wrong username or password."
                                     :username username})))
      (default-page req "login" {:error "Empty login fields."}))))

(defn handle-signup
  "Create a new account is parameters pass
  the conditions."
  [{:keys [params session] :as req}]
  (let [{:keys [username password]} params]
    (cond
      ;; Return the login page with an error field
      (not (and (seq username) (seq password)))
      (default-page req "login" {:error-signup "Empty account fields."})

      (not (<= 2 (count username) 18))
      (default-page req "login" {:error-signup "Wrong username length."})

      :else ; create the new record and redirect to homepage
      (let [user (controller/create-user! {:id username :password password})]
          (-> (redirect "/")
              (assoc :session (assoc session :identity (dissoc user :password))))))))

(defn handle-logout
  "Clear the user session."
  [_]
  (-> (redirect "/")
      (assoc :session nil)))

(defn auth-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/login"  {:get  login-page
               :post handle-login}]
   ["/signup" {:post handle-signup}]
   ["/logout" {:get  handle-logout}]])
