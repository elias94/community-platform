-- :name create-flagged! :<! :1
-- :doc creates a new flagged record
INSERT INTO flagged
("user", item)
VALUES (:user, :item)

-- :name get-flagged-count :? :1
-- :doc count flagged by item
SELECT COUNT("user")
FROM flagged
WHERE item = :item
GROUP BY "user"

-- :name get-flagged :? :1
-- :doc get flagged
SELECT *
FROM flagged
WHERE item = :item AND "user" = :user

-- :name delete-flagged! :! :1
-- :doc delete flagged
DELETE FROM flagged
WHERE item = :item AND "user" = :user
