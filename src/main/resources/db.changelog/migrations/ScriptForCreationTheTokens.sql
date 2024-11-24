create table netology.tokens (
id serial primary key,
account_id int references netology.users(id) on delete cascade,
token varchar(255) not null,
token_type varchar(255) not NULL DEFAULT 'BEARER',
revoked BOOLEAN not null default false,
expired BOOLEAN not null default false
)