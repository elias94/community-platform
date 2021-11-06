(ns socn.controllers.core
  (:require [clojure.spec.alpha :as s]
            [socn.validations]))

(defn- db-action [action entity params]
  (let [valid   (keyword (str entity action "!"))
        fn-name (str "socn.db.core/" action "-" entity "!")
        ;; will call db/create-entity!
        db-fn   (ns-resolve *ns* (symbol fn-name))]
    (if (s/valid? valid params)
      (apply db-fn [params])
      (throw (ex-info
              (str "Error during " entity " " action)
              {:validation (s/explain-data valid params)})))))

(defn create!
  "Create a new db record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "create" entity params))

(defn update!
  "Update the record of the specified entity.
  It performs a validation of the params."
  [entity params]
  (db-action "update" entity params))
