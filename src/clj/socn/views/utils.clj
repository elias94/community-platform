(ns socn.views.utils
  (:require [clojure.string :as string]
            [socn.utils :refer [trunc]]))

(defn class-names [classes]
  (string/join " " classes))

(defn plural [n s]
  (when (> n 1) (str s "s")))

(defn text-age [mins]
  (str 
   (cond
     (>= mins 1440) (plural (trunc (/ mins 1440)) "day")
     (>= mins 60)   (plural (trunc (/ mins 60))   "hour")
     :else          (plural (trunc mins)          "minute"))
   " ago"))
