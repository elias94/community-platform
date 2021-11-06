(ns socn.controllers.users
  (:require [clojure.spec.alpha :as s]
            [socn.db.core :as db]
            [conman.core :as conman]
            [buddy.hashers :as hashers]
            [socn.utils :as utils]
            [socn.validations]))

;; Default hashing algorithm for password store
(def hash-config {:alg :bcrypt+blake2b-512})

(defn create-user!
  "Create a new user record in the db."
  [{:keys [id password]}]
  (let [params {:id       id
                :password (hashers/derive password hash-config)
                :created  (java.util.Date.)
                :karma    1
                :showall  false}]
    (if (and (nil? (db/get-user {:id id}))
             (s/valid? :user/create! params))
      (conman/with-transaction [db/*db*]
        (db/create-user! params)
        (db/get-user {:id id}))
      (throw (ex-info "User already present" {:id id})))))
