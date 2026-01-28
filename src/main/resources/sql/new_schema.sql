alter table ingredient drop column id_dish , required_quantity;
alter table dish add column price numeric ;
alter table dish
    rename column price to selling_price;

create type unit_type as enum ('PCS','KG','L');
CREATE TABLE dishIngredient (
    id serial PRIMARY KEY,
    id_dish int NOT NULL REFERENCES dish(id) ON DELETE CASCADE ON UPDATE CASCADE,
    id_ingredient int NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE ON UPDATE CASCADE,
    quantity_required numeric NOT NULL,
    unit unit_type NOT NULL,
    UNIQUE (id_dish, id_ingredient)
);

-- Insert --

INSERT INTO dish (id, name, dish_type, selling_price) VALUES
    (1, 'Salade fraîche', 'START', 3500.00),
    (2, 'Poulet grillé', 'MAIN', 12000.00),
    (3, 'Riz aux légumes', 'MAIN', NULL),
    (4, 'Gâteau au chocolat', 'DESSERT', 8000.00),
    (5, 'Salade de fruits', 'DESSERT', NULL);

INSERT INTO dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),
    (3, 2, 3, 1.00, 'KG'),
    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG');

create type movement_type as enum ('IN', 'OUT');

create table if not exists stock_movement
(
    id                serial primary key,
    id_ingredient     int,
    quantity          numeric(10, 2),
    unit              unit,
    type              movement_type,
    creation_datetime timestamp without time zone,
    foreign key (id_ingredient) references ingredient (id)
);

alter table ingredient
    add column if not exists initial_stock numeric(10, 2);

create table if not exists "order"
(
    id                serial primary key,
    reference         varchar(255),
    creation_datetime timestamp without time zone
    );

create table if not exists dish_order
(
    id       serial primary key,
    id_order int references "order" (id),
    id_dish  int references dish (id),
    quantity int
    );