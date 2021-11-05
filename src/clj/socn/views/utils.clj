(ns socn.views.utils
  (:require [clojure.string :as string]
            [socn.utils :refer [trunc]]
            [java-time :as t]))

(defn class-names [classes]
  (string/join " " classes))

(defn plural [n s]
  (str n " " (if (not= n 1) (str s "s") s)))

(defn age [ts k]
  (let [interval (t/interval ts (t/instant))]
    (t/as interval k)))

(defn text-age [mins]
  (str 
   (cond
     (>= mins 1440) (plural (trunc (/ mins 1440)) "day")
     (>= mins 60)   (plural (trunc (/ mins 60))   "hour")
     :else          (plural (trunc mins)          "minute"))
   " ago"))
