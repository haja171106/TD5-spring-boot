drop database if exists mini_dish_db;

drop user if exists mini_dish_db_manager;

create database mini_dish_db;

create user mini_dish_db_manager with password '123456';

grant connect on database mini_dish_db to mini_dish_db_manager;

grant usage on schema public to mini_dish_db_manager;

grant select, insert, update, delete on all tables in schema public to mini_dish_db_manager;

grant all privileges on all sequences in schema public to mini_dish_db_manager;

grant create on schema public to mini_dish_db_manager;

alter default privileges in schema public
grant select, insert, update, delete on tables to mini_dish_db_manager;

alter default privileges in schema public
grant all privileges on sequences to mini_dish_db_manager;

