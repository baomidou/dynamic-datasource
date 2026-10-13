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
-- 幂等且不重置：H2 的 INIT=RUNSCRIPT 会在库重开/连接重开时重复执行脚本。
-- 不能用 MERGE（upsert）：重复执行会把测试运行中的数据覆盖回初始值（曾使 testDsTransactional 的扣款断言随机失败）。
-- 也不能用裸 INSERT：重复执行会主键冲突并使连接创建失败。
INSERT INTO account (id, balance) SELECT 1, 50.0 WHERE NOT EXISTS (SELECT 1 FROM account WHERE id = 1);

CREATE TABLE IF NOT EXISTS product
(
    id               INT NOT NULL AUTO_INCREMENT,
    price            DOUBLE   DEFAULT NULL,
    stock            INT DEFAULT NULL,
    PRIMARY KEY (id)
);
INSERT INTO product (id, price, stock) SELECT 1, 10.0, 20 WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 1);

CREATE TABLE IF NOT EXISTS tx_probe
(
    id  INT NOT NULL AUTO_INCREMENT,
    tag VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (id)
);