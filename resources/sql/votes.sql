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
-- :doc retrieves votes using offset and limit
SELECT * FROM votes
WHERE author = :author

-- :name exists-vote :? :n
-- :doc check if vote with author and item exists
SELECT exists(
  SELECT 1 FROM votes
  WHERE author = :author, item = :item
)
