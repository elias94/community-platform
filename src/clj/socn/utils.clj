(ns socn.utils)

(defn parse-int
  "Convert a string to an integer."
  [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn trunc [n]
  (int (Math/floor n)))
