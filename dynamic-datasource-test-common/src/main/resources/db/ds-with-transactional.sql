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
-- MERGE 而非 INSERT：H2 的 INIT=RUNSCRIPT 在库重开时会重复执行脚本，非幂等 insert 会造成主键冲突并使连接创建失败
MERGE INTO account (id, balance) VALUES (1, 50.0);

CREATE TABLE IF NOT EXISTS product
(
    id               INT NOT NULL AUTO_INCREMENT,
    price            DOUBLE   DEFAULT NULL,
    stock            INT DEFAULT NULL,
    PRIMARY KEY (id)
);
MERGE INTO product (id, price, stock) VALUES (1, 10.0, 20);

CREATE TABLE IF NOT EXISTS tx_probe
(
    id  INT NOT NULL AUTO_INCREMENT,
    tag VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (id)
);