create table netology.FILES (
id serial primary key,
account_id int references netology.users(id) on delete cascade,
file_path varchar(250) not null
);