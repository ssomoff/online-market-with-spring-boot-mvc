-- User
create table user_seq (next_val bigint);
insert into user_seq values ( 1 );

create table users (
    user_id bigint not null,
    activation_code varchar(255),
    active bit,
    address varchar(255),
    email varchar(255) not null unique,
    first_name varchar(255),
    last_name varchar(255),
    photo_name varchar(255),
    password varchar(255) not null,
    phone varchar(255),
    primary key (user_id)
);

-- User role
create table user_role (
    user_id bigint not null,
    roles varchar(255)
);

alter table user_role
    add constraint user_role_fk_users foreign key (user_id) references users (user_id);


-- Cart
create table cart_seq (next_val bigint);
insert into cart_seq values ( 1 );

create table carts (
    cart_id bigint not null,
    cart_price double precision,
    user_id bigint not null,
    primary key (cart_id)
);

alter table carts
    add constraint carts_fk_users foreign key (user_id) references users (user_id);


-- Product
create table product_seq (next_val bigint);
insert into product_seq values ( 1 );

create table products (
    product_id bigint not null,
    active bit,
    description varchar(255),
    name varchar(255),
    picture_name varchar(255),
    popularity varchar(255),
    price double precision,
    weight double precision,
    primary key (product_id)
);

-- Product amount
create table product_amount_seq (next_val bigint);
insert into product_amount_seq values ( 1 );

create table products_amount (
    product_amount_id bigint not null,
    amount integer,
    product_id bigint not null,
    primary key (product_amount_id)
);

alter table products_amount
    add constraint products_amount_fk_products foreign key (product_id) references products (product_id);

-- CartProduct
create table cart_product_seq (next_val bigint);
insert into cart_product_seq values ( 1 );

create table cart_products (
    cart_product_id bigint not null,
    amount integer,
    amount_price double precision,
    cart_id bigint not null,
    product_id bigint not null,
    primary key (cart_product_id)
);

alter table cart_products
    add constraint cart_products_fk_carts foreign key (cart_id) references carts (cart_id);
alter table cart_products
    add constraint cart_products_fk_products foreign key (product_id) references products (product_id);

-- Carts and CartProduct
create table carts_cart_product (
    cart_id bigint not null,
    cart_product_id bigint not null unique,
    primary key (cart_id, cart_product_id)
);



alter table carts_cart_product
    add constraint carts_cart_product_fk_cart_products foreign key (cart_product_id) references cart_products (cart_product_id);
alter table carts_cart_product
    add constraint carts_cart_product_carts foreign key (cart_id) references carts (cart_id);


-- Order
create table order_seq (next_val bigint);
insert into order_seq values ( 1 );

create table orders (
    order_id bigint not null,
    created datetime(6),
    order_price double precision,
    status varchar(255),
    updated datetime(6),
    delivery_date varchar(255),
    user_address varchar(255),
    user_name varchar(255),
    user_phone varchar(255),
    user_id bigint not null,
    primary key (order_id)
);

alter table orders
    add constraint orders_fk_users foreign key (user_id) references users (user_id);

-- Orders and CartProduct
create table orders_cart_products (
    order_id bigint not null,
    cart_product_id bigint not null unique,
    primary key (order_id, cart_product_id)
);


alter table orders_cart_products
    add constraint orders_cart_products_fk_cart_products foreign key (cart_product_id) references cart_products (cart_product_id);
alter table orders_cart_products
    add constraint orders_cart_products_fk_orders foreign key (order_id) references orders (order_id);


