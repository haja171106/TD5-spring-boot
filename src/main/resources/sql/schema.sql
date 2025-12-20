CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');

CREATE TABLE dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    dish_type dish_type
);

CREATE TYPE ingredient_category AS ENUM (
    'VEGETABLE',
    'ANIMAL',
    'MARINE',
    'DAIRY',
    'OTHER'
);

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    price NUMERIC(10, 2),
    category ingredient_category,
    id_dish INT REFERENCES dish(id)
);
