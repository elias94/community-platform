(ns socn.middleware
  (:require
   [socn.env :refer [defaults]]
   [clojure.tools.logging :as log]
   [socn.layout :refer [error-page]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [socn.middleware.formats :as formats]
   [muuntaja.middleware :refer [wrap-format wrap-params]]
   [socn.config :refer [env]]
   [ring.middleware.flash :refer [wrap-flash]]
   [ring.adapter.undertow.middleware.session :refer [wrap-session]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [buddy.auth.middleware :refer [wrap-authentication]]
   [buddy.auth.backends.session :refer [session-backend]]
   [buddy.auth.accessrules :refer [restrict]]
   [buddy.auth :refer [authenticated?]]))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-response (error-page {:status 403
                                 :title "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn wrap-restricted [handler]
  (restrict handler
            {:handler authenticated?
             :on-error (error-page {:status 403
                                    :title "403 - Not authorized"})}))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      (wrap-authentication (session-backend))
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))
      wrap-internal-error))
