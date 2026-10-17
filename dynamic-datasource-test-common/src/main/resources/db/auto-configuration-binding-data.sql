-- 由 AutoConfigurationBindingTest 中 slave_1 的 init.data 执行，分隔符同 schema 脚本。
-- 幂等：ac_slave1 是 DB_CLOSE_DELAY=-1 的内存库，同一 JVM 内重建上下文会再跑一遍脚本，裸 INSERT 会主键冲突。
INSERT INTO init_probe (id, tag) SELECT 1, 'init-data' WHERE NOT EXISTS (SELECT 1 FROM init_probe WHERE id = 1)
@@
