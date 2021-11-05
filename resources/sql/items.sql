-- :name create-item! :! :n
-- :doc creates a new item record
INSERT INTO items
(author, score, url, submitted, domain, content, title)
VALUES (:author, :score, :url, :submitted, :domain, :content, :title)
RETURNING id

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

-- :name get-items-with-comments :? :*
-- :doc retrieves items using offset and limit
SELECT items.*, COUNT(comments.id) AS comments
FROM items
LEFT JOIN comments
ON comments.item = items.id
GROUP BY items.id
ORDER BY score DESC
OFFSET :offset
LIMIT :limit

-- :name get-items-with-comments-by-domain :? :*
-- :doc retrieves items using offset and limit
SELECT items.*, COUNT(comments.id) AS comments
FROM items
LEFT JOIN comments
ON comments.item = items.id
WHERE items.domain = :domain
GROUP BY items.id
ORDER BY score DESC
OFFSET :offset
LIMIT :limit

