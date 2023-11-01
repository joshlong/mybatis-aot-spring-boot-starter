create table if not exists customer
(
    id   serial primary key,
    name text not null
);


create table if not exists city
(
    id      serial primary key,
    name    text,
    state   text,
    country text
);