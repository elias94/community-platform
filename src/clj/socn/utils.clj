(ns socn.utils
  (:require [clojure.string :as string]))

(defn trunc
  "Truncate float value n to int using MAth/floor."
  [n]
  (int (Math/floor n)))

(defn parse-int
  "Convert a string s to an integer safely."
  [s]
  (if (string? s)
    (Integer/parseInt (re-find #"\A-?\d+" s))
    s))

(defn parse-char
  "Convert a string s returning the first char safely."
  [s]
  (if (string? s)
    (char (first (char-array s)))
    s))

(defn parse-bool
  "Parse a string or nil value v to boolean."
  [v]
  (boolean (Boolean/valueOf v)))

(defn parse-bool-map
  "Parse a specific key k of m to boolean."
  [m k]
  (assoc m k (parse-bool (k m))))

(defn in?
  "True if coll contains el."
  [coll el]
  (some #(= el %) coll))

(defn domain-name
  "Extract the domain from the url."
  [url]
  (let [domain (.getHost (java.net.URI. url))]
    (if (string/starts-with? domain "www.")
      (subs domain 4)
      domain)))

(defn get-client-ip
  "Extract client ip from a http(s) request."
  [req]
  (if-let [ips (get-in req [:headers "x-forwarded-for"])]
    (-> ips (string/split #",") first)
    (:remote-addr req)))
