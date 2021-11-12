(ns socn.views.utils
  (:require [clojure.string :as string]
            [ring.util.codec :refer [form-encode]]
            [socn.utils :refer [trunc]]
            [java-time :as t]
            [buddy.auth :refer [authenticated?]]))

(defn class-names
  "Convert a vector of css class-names in a string."
  [classes]
  (string/join " " classes))

(defn plural
  "Return a string composed by the numeric
  value and it's name correctly pluralized."
  [n s]
  (str n " " (if (not= n 1) (str s "s") s)))

(defn age
  "Convert a Instant object into a numeric
  value based on the key. Ex :minutes ."
  [ts k]
  (let [interval (t/interval ts (t/instant))]
    (t/as interval k)))

(defn text-age
  "Convert age expressed in minutes into
  a readable format."
  [mins]
  (str
   (cond
     (>= mins 1440) (plural (trunc (/ mins 1440)) "day")
     (>= mins 60)   (plural (trunc (/ mins 60))   "hour")
     :else          (plural (trunc mins)          "minute"))
   " ago"))

(defn author?
  "Return true if the current user is the author of the comment."
  [comment req]
  (and (authenticated? req)
       (let [{{:keys [id]} :identity} (:session req)]
         (= id (:author comment)))))

(defn encode-url
  "Encode route and query-params in a url format.
  Optionally include an id appended with #."
  ([route query-params]
   (str "/" route "?" (form-encode query-params)))
  ([route query-params id]
   (str (encode-url route query-params) "#" id)))
