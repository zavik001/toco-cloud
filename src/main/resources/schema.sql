create table if not exists Toco_Order (
  id identity,
  delivery_Name varchar(50) not null,
  delivery_Street varchar(50) not null,
  delivery_City varchar(50) not null,
  delivery_State varchar(2) not null,
  delivery_Zip varchar(10) not null,
  cc_number varchar(16) not null,
  cc_expiration varchar(5) not null,
  cc_cvv varchar(3) not null,
  placed_at timestamp not null
);
create table if not exists Taco (
  id identity,
  name varchar(50) not null,
  toco_order bigint not null,
  toco_order_key bigint not null,
  created_at timestamp not null
);
create table if not exists Ingredient (
  id varchar(4) not null primary key,
  name varchar(25) not null,
  type varchar(10) not null
);
create table if not exists Ingredient_Ref (
  ingredient varchar(4) not null,
  toco bigint not null,
  toco_key bigint not null
);
alter table Taco
    add foreign key (toco_order) references Toco_Order(id);
alter table Ingredient_Ref
    add foreign key (ingredient) references Ingredient(id);
