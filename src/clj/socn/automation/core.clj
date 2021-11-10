(ns socn.automation.core
  "Namespace for automated actions functionalities."
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as html]
            [socn.controllers.core :as controller]
            [socn.utils :as utils]))

(defn rand-str
  "Generate a random string."
  [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn create-item
  "Create a new item record in the db."
  [author url content title]
  (controller/create!
   "item"
   {:author    author
    :score     0
    :submitted (java.util.Date.)
    :url       url
    :domain    (utils/domain-name url)
    :content   content
    :title     title}))

(defn create-comment
  "Create a new comment record in the db."
  [author item content parent score]
  (controller/create!
   "comment"
   {:author    author
    :item      (utils/parse-int item)
    :content   content
    :score     score
    :parent    (utils/parse-int parent)
    :submitted (java.util.Date.)}))

(defn- create-fake-user [id]
  (when (not (controller/get "user" {:id id}))
    (controller/create-user! {:id id :password (rand-str 8)})))

(def ^:private hn-comment-footer
  #"                      reply")

(defn- valid-hn-url [url]
  (boolean (re-matches #"^https:\/\/news\.ycombinator\.com\/item\?id=[0-9]{8}$" url)))

(comment
  (def url "https://news.ycombinator.com/item?id=29139884")
  (valid-hn-url url)
  (re-find #"(id=)([0-9]{8})" url))

(defn clone-hn
  "Clone a news and its comments from hn."
  [url]
  (when (valid-hn-url url)
    (let [page   (html/html-resource (java.net.URL. url))
          header (first (html/select page [:td.title :a]))
          title  (html/text header)
          link   ((comp :href :attrs) header)
          author (html/text (first (html/select page [:table.fatitem :a.hnuser])))
          comms  (html/select page [:table.comment-tree :tr.comtr])]
      (log/info "HN - Retrivied page")
      ;; create news author
      (create-fake-user author)
      ;; create news record
      (let [item-id (:id (create-item author link nil title))
            tot     (count comms)]
        (log/info (str "ID: " item-id))
        (log/info (str "HN - Created news \"" title "\" with id: " item-id))
        (doseq [c comms]
          (let [user    (html/text (first (html/select c [:span.comhead :a.hnuser])))
                content (first (html/select c [:div.comment]))
                clean   (string/trim
                         (string/replace
                          (html/text content)
                          hn-comment-footer ""))
                navs    (html/select c [:span.navs :> :a])
                nav     (first (filter #(= (first (:content %)) "parent") navs))
                parent  (if (not-empty nav) (subs ((comp :href :attrs) nav) 1) nil)]
          ;; create comment author
            (create-fake-user user)
          ;; create comment
            (create-comment user item-id clean parent (utils/trunc (rand (/ tot 2)))))))
      (log/info "HN - Created comments"))))

(comment
  (clone-hn "https://news.ycombinator.com/item?id=29139884"))

(comment
  (def url "https://news.ycombinator.com/item?id=29139884")
  (def page (html/html-resource (java.net.URL. url)))
  (def title (first (html/select page [:td.title :a])))
  (html/text title) ;title
  ((comp :href :attrs) title) ;url
  (def comms (html/select page [:table.comment-tree :tr.comtr]))
  (count comms)
  (def c (nth comms 2))
  (html/text (first (html/select c [:span.comhead :a.hnuser]))) ; user
  (def content (html/text (first (html/select c [:div.comment])))) ;content
  (string/trim (string/replace content #"                      reply" ""))
  (html/text (first (html/select page [:table.fatitem :a.hnuser]))) ; news author
  (def navs (html/select c [:span.navs :> :a]))
  (def nav (first (filter #(= (first (:content %)) "parent") navs)))
  (subs ((comp :href :attrs) nav) 1) ; parent id
  )
