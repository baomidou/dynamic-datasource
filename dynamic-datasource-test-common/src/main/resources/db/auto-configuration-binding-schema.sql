-- 由 AutoConfigurationBindingTest 中 slave_1 的 init.schema 执行。语句分隔符取 init.separator 绑定的值而非默认分号：
-- 分隔符没绑定上时，ScriptUtils 会回落到按行切分，这条多行建表语句随之失败。
CREATE TABLE IF NOT EXISTS init_probe
(
    id  INT NOT NULL,
    tag VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (id)
)
@@
