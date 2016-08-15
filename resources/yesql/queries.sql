-- name: posts
-- Returns all the posts with categories and users joined
select p.id, p.created_at, p.updated_at, p.title, p.body,
c.id as category_id, c.name as category_name, u.id as user_id, u.name as user_name
from posts p
left join categories c on c.id = p.category_id
left join users u on u.id = p.user_id

-- name: find-post
-- Finds a post by id
select p.id, p.created_at, p.updated_at, p.title, p.body,
c.id as category_id, c.name as category_name, u.id as user_id, u.name as user_name
from posts p
left join categories c on c.id = p.category_id
left join users u on u.id = p.user_id
where p.id = :id
offset 0
limit 1

-- name: create-post<!
-- Adds a new post
insert into posts (title, body, category_id, user_id) values (:title, :body, :category_id, :user_id)

-- name: truncate-posts!
delete from posts

-- name: create-category<!
-- Adds a new category
insert into categories (name) values (:name)

-- name: truncate-categories!
delete from categories

-- name: find-category
select * from categories where name = :name limit 1

-- name: categories
select * from categories

-- name: create-user<!
-- Adds a new user
insert into users (name) values (:name)

-- name: truncate-users!
delete from users

-- name: find-user
select * from users where name = :name limit 1

