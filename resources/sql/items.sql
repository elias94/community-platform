-- :name create-item! :<! :1
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
SELECT items.*, count(flagged)
FROM items LEFT JOIN flagged
ON items.id = flagged.item
GROUP BY items.id, flagged
HAVING count(flagged) < :threshold
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

-- :name get-discussions-with-comments :? :*
-- :doc retrieves only discuissions
SELECT items.*, COUNT(comments.id) AS comments
FROM items
LEFT JOIN comments
ON comments.item = items.id
WHERE items.url is null
GROUP BY items.id
ORDER BY score DESC
OFFSET :offset
LIMIT :limit

-- :name update-item! :! :n
-- :doc updates item content
UPDATE items
SET content = :content, edited = :edited
WHERE id = :id

-- :name delete-item! :! :1
-- :doc deletes item by id
DELETE FROM items
WHERE id = :id

