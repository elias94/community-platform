(ns socn.session)

(defn auth
  "Return the value of key k inside
  the session identity."
  ([req]
   ((comp :identity :session) req))
  ([k req]
   ((comp k :identity :session) req)))
