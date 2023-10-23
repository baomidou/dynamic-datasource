CREATE TABLE  IF NOT EXISTS p_order
(
    id             INT NOT NULL AUTO_INCREMENT,
    user_id        INT DEFAULT NULL,
    product_id     INT DEFAULT NULL,
    amount         INT DEFAULT NULL,
    total_price    DOUBLE       DEFAULT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS account
(
    id               INT NOT NULL AUTO_INCREMENT,
    balance          DOUBLE   DEFAULT NULL,
    PRIMARY KEY (id)
);
insert into account (id, balance) VALUES (1, 50.0);

CREATE TABLE IF NOT EXISTS product
(
    id               INT NOT NULL AUTO_INCREMENT,
    price            DOUBLE   DEFAULT NULL,
    stock            INT DEFAULT NULL,
    PRIMARY KEY (id)
);
insert into product (id, price, stock) VALUES (1, 10.0, 20);