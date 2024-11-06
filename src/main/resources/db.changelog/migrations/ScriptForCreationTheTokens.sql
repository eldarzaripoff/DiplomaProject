create table netology.tokens (
id serial primary key,
account_id int references netology.users(id) on delete cascade,
token varchar(50) not null,
is_active BOOLEAN NOT NULL DEFAULT TRUE
)