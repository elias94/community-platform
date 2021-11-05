(ns socn.utils)

(defn parse-int
  "Convert a string to an integer."
  [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn trunc
  "Truncate float value to int using MAth/floor."
  [n]
  (int (Math/floor n)))

(defn parse-bool
  "Parse a string or nil value to boolean."
  [v]
  (boolean (Boolean/valueOf v)))

(defn parse-bool-map
  "Parse a specific key of a map to boolean."
  [m k]
  (assoc m k (parse-bool (k m))))
