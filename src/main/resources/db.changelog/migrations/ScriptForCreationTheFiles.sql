create table netology.FILES (
id serial primary key,
account_id int references netology.users(id) on delete cascade,
file_name varchar(250) not null,
file_type varchar(250) not null,
file_data bytea not null,
file_size int not null
);