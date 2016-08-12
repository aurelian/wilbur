create table posts (
  id serial primary key,
  category_id integer not null,
  user_id integer not null,
  title varchar(255) not null,
  body text,
  created_at timestamp default localtimestamp,
  updated_at timestamp default localtimestamp
);

