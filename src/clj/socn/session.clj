(ns socn.session)

(defn authenticated?
  "True if the user is authenticated."
  [req]
  (boolean (:identity req)))

(defn auth
  "Return the value of key k inside
  the session identity."
  ([req]
   ((comp :identity :session) req))
  ([k req]
   ((comp k :identity :session) req)))
