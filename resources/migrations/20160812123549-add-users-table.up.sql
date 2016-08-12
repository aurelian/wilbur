create table users (
  id serial primary key,
  name varchar(255) not null,
  created_at timestamp default localtimestamp,
  updated_at timestamp default localtimestamp
);
