-- :name create-item! :! :n
-- :doc creates a new item record
INSERT INTO items
(author, score, url, submitted, domain, content, title)
VALUES (:author, :score, :url, :submitted, :domain, :content, :title)

-- :name get-item :? :1
-- :doc retrieves a item record given the id
SELECT * FROM items
WHERE id = :id

-- :name get-items :? :*
-- :doc retrieves items using offset and limit
SELECT * FROM items
ORDER BY score DESC
OFFSET :offset
LIMIT :limit
