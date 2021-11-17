(ns socn.validations
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

;;=======================
;; Regex
;;=======================

;; source: https://emailregex.com/
(def re-email #"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])")

;; source: https://stackoverflow.com/a/26987741/15232490
(def re-domain #"^(((?!-))(xn--|_{1,1})?[a-z0-9-]{0,61}[a-z0-9]{1,1}\.)*(xn--)?([a-z0-9][a-z0-9\-]{0,60}|[a-z0-9-]{1,30}\.[a-z]{2})$")

(def re-int #"^\d+$")

;; source: https://regexr.com/39nr7
(def re-url #"[(http(s)?):\/\/(www\.)?a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")

;;=======================
;; Validation specs
;;=======================

(defmacro or-nil
  "A proper or condition for spec."
  [cond]
  `(s/or :nil nil? :value ~cond))

(comment
  (macroexpand-1 '(or-nil string?)))

;; Use them like (s/valid? :socn.validation/domain "wikipedia.org")
(s/def ::domain (s/and string? #(re-matches re-domain %)))
(s/def ::email  (s/and string? #(re-matches re-email %)))
(s/def ::url    (s/and string? #(re-matches re-url %)))
(s/def ::item   (s/and string? #(re-matches re-int %)))
(s/def ::type   (s/and char? (s/or :item    #(= % \i)
                                   :comment #(= % \c))))


(s/def :user/id       (s/and string? #(<= 2 (count %) 18)))
(s/def :user/email    (s/or :empty empty? :email ::email))
(s/def :user/about    string?)
(s/def :user/password (s/and string? #(<= 4 (count %))))
(s/def :user/created  inst?)
(s/def :user/karma    pos-int?)
(s/def :user/showall  boolean?)
(s/def :user/get
  (s/keys :req-un [:user/id]))
(s/def :user/create!
  (s/keys
   :req-un [:user/id
            :user/password
            :user/created
            :user/karma
            :user/showall]))
(s/def :user/update!
  (s/keys
   :req-un [:user/id
            :user/email
            :user/about
            :user/showall]))
(s/def :user/exists?
  (s/keys :req-un [:user/id]))

(s/def :comment/id        int?)
(s/def :comment/author    string?)
(s/def :comment/item      int?)
(s/def :comment/content   string?)
(s/def :comment/score     int?)
(s/def :comment/parent    (or-nil int?))
(s/def :comment/submitted inst?)
(s/def :comment/edited    inst?)
(s/def :comment/get
  (s/keys :req-un [:comment/id]))
(s/def :comment/create!
  (s/keys
   :req-un [:comment/author
            :comment/item
            :comment/content
            :comment/score
            :comment/parent
            :comment/submitted]))
(s/def :comment/update!
  (s/keys
   :req-un [:comment/id
            :comment/content
            :comment/edited]))
(s/def :comment/delete!
  (s/keys :req-un [:comment/id]))


(s/def :item/id        int?)
(s/def :item/author    string?)
(s/def :item/score     int?)
(s/def :item/content   (or-nil string?))
(s/def :item/title     string?)
(s/def :item/url       (or-nil ::url))
(s/def :item/domain    (or-nil ::domain))
(s/def :item/submitted inst?)
(s/def :item/edited    inst?)
(s/def :item/offset    int?)
(s/def :item/limit     int?)
(s/def :item/get
  (s/keys :req-un [:item/id]))
(s/def :item/create!
  (s/keys
   :req-un [:item/author
            :item/score
            :item/title
            :item/domain
            :item/submitted]
   :opt-un [:item/content
            :item/url]))
(s/def :item/update!
  (s/keys
   :req-un [:item/id
            :item/content
            :item/edited]))
(s/def :item/delete!
  (s/keys :req-un [:item/id]))
(s/def :item/discussion
  (s/keys
   :req-un [:item/offset
            :item/limit]))


(s/def :vote/author    string?)
(s/def :vote/item      int?)
(s/def :vote/type      ::type)
(s/def :vote/submitted inst?)
(s/def :vote/direction
  (s/and string? (s/or :up   #(= % "up")
                       :down #(= % "down"))))
(s/def :vote/create!
  (s/keys
   :req-un [:vote/author
            :vote/item
            :vote/submitted
            :vote/type]))
(s/def :vote/delete!
  (s/keys
   :req-un [:vote/author
            :vote/item]))
(s/def :vote/exists?
  (s/keys
   :req-un [:vote/author
            :vote/item]))


(s/def :flagged/user string?)
(s/def :flagged/item int?)
(s/def :flagged/get
  (s/keys
   :req-un [:flagged/item
            :flagged/user]))
(s/def :flagged/create! :flagged/get)
(s/def :flagged/delete! :flagged/get)
(s/def :flagged/id
  (s/keys
   :req-un [:item/id]))

;;=======================
;; Validation functions
;;=======================

;; ref: https://github.com/metosin/spec-tools/blob/master/docs/01_coercion.md#strict-coercion
(def strict-transf
  "Transformer for strict coercion using json."
  (st/type-transformer
   st/json-transformer
   st/strip-extra-keys-transformer
   st/strip-extra-values-transformer))

(def strict-transf-str
  "Transformer for strict coercion using string."
  (st/type-transformer
   st/string-transformer
   st/strip-extra-keys-transformer
   st/strip-extra-values-transformer))

(defn get-transformer
  "Return the tranfromer using a key between:
  :string, :json, :strict, :strict-str .
  
  Default null."
  [k]
  (case k
    :string     st/string-transformer
    :json       st/json-transformer
    :strict     strict-transf
    :strict-str strict-transf-str
    :else       nil))

(defn valid?
  "Clojure spec validation with data coercion.
  Must containt spec, data and transformer to use."
  [data spec transf]
  (s/valid?
   spec
   (st/coerce spec data (get-transformer transf))))

(defn coerce
  "Coerce the value to spec using a transformer
  pointet by the key."
  [data spec k]
  (st/coerce spec data (get-transformer k)))

(comment
  (require '[muuntaja.core :as m])
  (macroexpand (valid?
                {:author "es" :item "12"}
                :vote/delete!
                :string))
  (s/valid? :item/get (st/coerce :item/get {:id "1"} st/string-transformer))
  (st/coerce :item/get {:id "1" :prova 2} strict-transf-str)
  (valid? "1" :item/id :strict-str))
