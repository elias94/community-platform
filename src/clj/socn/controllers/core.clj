(ns socn.controllers.core
  (:refer-clojure :exclude [get])
  (:require [clojure.spec.alpha :as s]
            [buddy.hashers :as hashers]
            [socn.validations]
            [socn.db.core :as db]))

;; Default hashing algorithm for password store
(def ^:private hash-config {:alg :bcrypt+blake2b-512})

(defn- db-action [action entity params & {:keys [mark] :or {mark ""}}]
  (let [valid   (keyword entity (str action mark))
        fn-name (str "socn.db.core/" action "-" entity mark)
        ;; will call db/create-entity!
        db-fn   (ns-resolve *ns* (symbol fn-name))]
    (if (s/valid? valid params)
      (apply db-fn [params])
      (throw (ex-info
              (str "Error during " entity " " action)
              {:validation (s/explain-data valid params)})))))

(defn get
  "Get the record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "get" entity params))

(defn create!
  "Create a new db record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "create" entity params :mark "!"))

(defn update!
  "Update the record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "update" entity params :mark "!"))

(defn delete!
  "Delete the record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "delete" entity params :mark "!"))

(defn exists?
  "Check if exist the record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "extists" entity params :mark "?"))

(defn create-user!
  "Create a new user record in the db hashing the password."
  [params]
  (let [{:keys [id password]} params
        params {:id       id
                :password (hashers/derive password hash-config)
                :created  (java.util.Date.)
                :karma    1
                :showall  false}]
    (if (and (nil? (db/get-user {:id id}))
             (s/valid? :user/create! params))
      (db/create-user! params)
      (throw (ex-info "User already present" {:id id})))))
