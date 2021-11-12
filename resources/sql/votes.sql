-- :name create-vote! :! :n
-- :doc creates a new vote record
INSERT INTO votes
(author, item, submitted, type)
VALUES (:author, :item, :submitted, :type)

-- :name get-votes-by-item :? :1
-- :doc retrieves a vote record given the id
SELECT * FROM votes
WHERE item = :item

-- :name get-votes-by-author :? :1
-- :doc retrieves vote records given offset and limit
SELECT * FROM votes
WHERE author = :author

-- :name exists-vote? :! :1
-- :doc check if a vote exists given author and item
SELECT exists(
  SELECT 1 FROM votes
  WHERE author = :author AND item = :item
)

-- :name delete-vote! :! :n
-- :doc delete a vote record given author and item
DELETE FROM votes
WHERE author = :author AND item = :item
