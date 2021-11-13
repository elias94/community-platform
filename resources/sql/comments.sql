-- :name create-comment! :<! :n
-- :doc creates a new comment record
INSERT INTO comments
(author, item, content, submitted, score, parent)
VALUES (:author, :item, :content, :submitted, :score, :parent)
RETURNING id

-- :name get-comment :? :1
-- :doc retrieves a comment record given the id
SELECT * FROM comments
WHERE id = :id

-- :name get-comments-by-item :? :*
-- :doc retrieves comments by item using offset and limit
SELECT * FROM comments
WHERE item = :item
ORDER BY score DESC
OFFSET :offset
LIMIT :limit

-- :name get-comments-by-author :? :*
-- :doc retrieves comments by author using offset and limit
SELECT * FROM comments
WHERE author = :author
ORDER BY score DESC
OFFSET :offset
LIMIT :limit

-- :name get-comments-by-parent :? :*
-- :doc retrieves comments by parent using offset and limit
SELECT * FROM comments
WHERE parent = :parent
ORDER BY score DESC
OFFSET :offset
LIMIT :limit

-- :name update-comment! :! :n
-- :doc updates comment content
UPDATE comments
SET content = :content, edited = :edited
WHERE id = :id

-- :name delete-comment! :! :n
-- :doc deletes comment by id
DELETE FROM comments
WHERE id = :id
