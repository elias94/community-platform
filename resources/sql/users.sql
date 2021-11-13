-- :name create-user! :<! :n
-- :doc creates a new user record
INSERT INTO users
(id, password, created, karma, showall)
VALUES (:id, :password, :created, :karma, :showall)
RETURNING id

-- :name update-user-password! :! :n
-- :doc updates user password
UPDATE users
SET password = :password
WHERE id = :id

-- :name update-user-email! :! :n
-- :doc updates user email
UPDATE users
SET email = :email
WHERE id = :id

-- :name update-user-about! :! :n
-- :doc updates user about
UPDATE users
SET about = :about
WHERE id = :id

-- :name update-user-karma! :! :n
-- :doc updates user karma
UPDATE users
SET karma = :karma
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id

-- :name update-user! :! :n
-- :doc updates user about, email and showall
UPDATE users
SET about = :about, email = :email, showall = :showall
WHERE id = :id

-- :name exists-user? :! :1
-- :doc check if a user exists given author and item
SELECT exists(
  SELECT 1 FROM users
  WHERE id = :id
)
