(ns socn.controllers.items
  (:require [clojure.math.numeric-tower :as math]
            [java-time :as t]
            [socn.views.utils :refer [age]]))

;;;;;;;;;;;;;;;;;;;
;; thresholds
;;;;;;;;;;;;;;;;;;;
(def downvote-threshold* 100)
(def downvote-time* 1440)
(def flag-threshold* 30)
(def flag-kill-threshold* 7)
(def many-flags* 1)
; Un-flagging something doesn't unkill it, if it's now no longer
; over flag-kill-threshold.  Ok, since arbitrary threshold anyway.
(defn flag-link [])
(defn kill-link [])

;; Turn this on when SPAM becomes a problem
(def enforce-oversubmitting false)

; New user can't submit more than 2 stories in a 2 hour period.
; Give overeager users the key toofast to make limit permanent.

;; (defn oversubmitting [user ip kind (o url)] 
;;   (and enforce-oversubmit*
;;        (or (check-key user 'toofast)
;;            (ignored user)
;;            (< (user-age user) new-age-threshold*)
;;            (< (karma user) new-karma-threshold*))
;;        (len> (recent-items [or (author user _) (is _!ip ip)] 180)
;;              (if (is kind 'story)
;;                (if (ignored user) 0 1)
;;                (if (ignored user) 1 10)))))

(def user-changetime 120)

(defn author? [user item]
  (= (:author item) (:id user)))

(defn item-age [item]
  (age (:submitted item) :minutes))

(defn can-edit?
  "Check if user is allowed to edit the item."
  [user item]
  (and (author? user item)
       (< (item-age item) user-changetime)))

(def gravity 1.8)
(def timebase 120)
(def front-threshold 1)
(def nonurl-factor 0.4)
(def lightweight-factor 0.17)
(def gag-factor 0.1)

(defn age-hours [item]
  1)

(defn frontpage-rank [item realscore age])

(defn item-score [item votes]
  (let [penalties 1]
    ((/ (math/expt (- (count votes) 1) 0.8)
        (math/expt (+ (age-hours item) 2) gravity)))))

(defn- comp-comments 
  "Compare two comments by score and by date-time submitted."
  [a b]
  (let [comp (compare (:score b) (:score a))]
    (if (= comp 0)
      (t/after? (:submitted a) (:submitted b))
      comp)))

(defn- sort-recur [coll parent]
  (let [childs (filter #(= (:parent %) parent) coll)
        sorted (sort comp-comments childs)]
    (if (empty? sorted)
      nil
      (into [] (map
                #(assoc % :children (sort-recur coll (:id %)))
                sorted)))))

(defn sort-comments
  "Sort comments by score and parent."
  [comments]
  (sort-recur comments nil))

