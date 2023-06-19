insert into users (user_id, activation_code, active, email, first_name, last_name, photo_name, password, phone)
values (1, '', true, 'admin@shopproduct.ru', 'Admin', '','admin.png', '$2a$12$FSkdIShsZmChfxxUyjirbupmUi4dMpT0vpdCgIOTg/zPwiSKvizP2', '');


insert into user_role (user_id, roles)
values (1, 'ADMIN');

update user_seq set next_val= 2 where next_val=1;

insert into carts (cart_id, cart_price, user_id)
values (1, 0.0, 1);

update cart_seq set next_val= 2 where next_val=1;