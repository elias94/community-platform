(ns socn.controllers.items
  (:require [clojure.spec.alpha :as s]
            [socn.db.core :as db]
            [conman.core :as conman]
            [buddy.hashers :as hashers]
            [socn.utils :as utils]
            [socn.validations]))

(defn create-item!
  "Create a new item record in the db."
  [params]
  (if (s/valid? :item/create! params)
    (db/create-item! params)
    (throw (ex-info
            "Error creating item"
            {:validation (s/explain-data :item/create! params)}))))

(defn create-comment!
  "Create a new comment record in the db."
  [params]
  (println params)
  (if (s/valid? :comment/create! params)
    (db/create-comment! params)
    (throw (ex-info
            "Error creating comment"
            {:validation (s/explain-data :comment/create! params)}))))
