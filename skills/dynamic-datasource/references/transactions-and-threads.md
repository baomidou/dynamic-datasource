# 事务、线程与编程式切换 | Transactions, Threads & Programmatic Switching

> 本文是 [SKILL.md](../SKILL.md) 的参考文档：事务内切换、`@DSTransactional` 行为、异步/线程池、手动 push/poll。

## 一、`@DS` 与 Spring 事务的关系（最核心的一条）

Spring `@Transactional` 的语义：**一个 DataSource 实例在一个事务中只绑定一条连接**。事务开启时用的哪个库，整个事务内就是哪个库。

- `@DS` 换 key 只影响"下次获取连接"，无法更换已绑定的连接 → **`@DS` 必须发生在事务开启之前**；
- 常见踩坑场景：`@Transactional` 方法内部调 `@DS` 的 Mapper、Spring Batch/Quartz/ShedLock 等框架先开了事务再执行 SQL；
- 想在一个事务里操作多个库：Spring `@Transactional` 做不到，用 `@DSTransactional`（或 `LocalTxUtil`）替代。

四种解法（以"框架先开事务"为例）：

- A. 不开事务（无事务时每次调用独立取连接，`@DS` 正常切换）；
- B. 给框架显式指定"目标库"的事务管理器（如 `new DataSourceTransactionManager(routingDataSource.getDataSource("slave_1"))`）；
- C. 让 `@DS` 发生在事务开启之前（把注解放在外层调用边界）；
- D. 跨多库且需要事务：用 `@DSTransactional`，且外层不要再叠加 Spring 事务。

## 二、`@DSTransactional`（本地多库事务）

`@DSTransactional` 是自研的本地多库事务（不经过 `PlatformTransactionManager`）：在一个事务 id（xid）下绑定各参与数据源的连接，到边界统一提交/整体回滚；嵌套由 savepoint 支撑。

| 传播 | 无外层事务 | 有外层事务 |
| --- | --- | --- |
| `REQUIRED`（默认） | 新建 | 加入（同一 xid） |
| `REQUIRES_NEW` | 新建 | 挂起外层，独立事务 |
| `NESTED` | 等同 REQUIRED | 当前事务内创建 savepoint |
| `NOT_SUPPORTED` | 非事务执行 | 挂起外层，非事务执行 |
| `SUPPORTS` | 非事务执行 | 加入 |
| `MANDATORY` | 抛异常 | 加入 |
| `NEVER` | 非事务执行 | 抛异常 |

回滚规则：`rollbackFor` 默认 `{Exception.class}`，`Error` 一律回滚，`noRollbackFor` 优先于 `rollbackFor`；提交阶段失败抛 `TransactionSystemException`。

**明确不支持**（与 Spring AOP 一致的红线）：

1. 跨线程不传播（xid 是 ThreadLocal，线程池任务不在事务中）；
2. 同类 `this.` 自调用不走切面；
3. 非 public 方法默认不生效（`aop.allowed-public-only=true`）；
4. 与 Spring `@Transactional` 互不感知——混用要显式规划边界（外层是 Spring 事务时 `@DS` 不切换）。

完整语义（含契约测试说明）见文档站：[@DSTransactional 语义边界](https://doc.xiuceyun.cn/docs/tx/local-tx)。

## 三、Atomikos XA（跨库强一致，与 @DSTransactional 二选一）

需要真正的分布式原子性时用 Atomikos XA（JTA 两阶段提交），与 `@DSTransactional` 是两套机制。要点：

- 数据源必须显式 `type: com.atomikos.jdbc.AtomikosDataSourceBean`（Hikari 创建器优先级更高，缺省会启动失败），`driver-class-name` 指向 XA 数据源类（MySQL `com.mysql.cj.jdbc.MysqlXADataSource`、PostgreSQL `org.postgresql.xa.PGXADataSource`、H2 `org.h2.jdbcx.JdbcDataSource` 等）；
- Boot 3/4 用 Atomikos 6 的 `jakarta` classifier，且需显式引入 `jakarta.transaction-api`（Atomikos 声明为 provided，不传递）；
- Boot 4 不再自动装配 Atomikos 事务管理器，需自行声明 `UserTransactionManager` + `JtaTransactionManager`；
- **多数据源 MyBatis 不要用 Spring `@Transactional`**：MyBatis-Spring 一个事务复用一个 SqlSession/连接，事务内 `@DS` 切换失效，跨库写入会集中到第一个库；改用编程式 JTA（`begin` / `commit` / `rollback`）；
- MyBatis 侧配 `AtomikosTransactionFactory`；XA 数据源不要用 `init.schema` 建表（Atomikos 非事务连接关闭时回滚未提交 DDL）。

完整指南（含参数表与可运行示例）见同目录 [atomikos-xa.md](atomikos-xa.md)，
示例：[tx-atomikos-sample](https://github.com/dynamic-datasource/dynamic-datasource-samples/tree/master/spring-boot-4/tx-atomikos-sample)。

## 四、线程 / 异步 / 并发

`DynamicDataSourceContextHolder` 基于 `ThreadLocal`：

- 父线程的切换结果**不会传给子线程**（`@Async`、线程池、`CompletableFuture`、`parallelStream` 子线程默认走 primary）；
- 池线程被复用：`push` 后漏 `poll`/`clear` 会污染后续任务（偶发串库的根因）；
- 事务上下文同样不跨线程。

解决方案（按推荐度）：

- **A（推荐）**：把切库动作放进子线程内部——让任务自己调用带 `@DS` 的代理方法；
- **B**：任务内手动 `push`/`poll` 包裹（`try { push } finally { poll }`）；
- **C（进阶）**：TTL（TransmittableThreadLocal）装饰线程池——隐式传播在复杂链路中易引出新串库问题，非全局需求慎用。

并发串库自查：所有 push 是否成对；拦截器/Filter 是否在异常路径也还原；是否依赖"子线程继承父线程数据源"的假设（不支持）。

## 五、编程式切换（手动 push / poll）

```java
DynamicDataSourceContextHolder.push("slave");
try {
    // 业务 SQL
} finally {
    DynamicDataSourceContextHolder.poll();   // 必须执行！
}
```

| 方法 | 语义 |
| --- | --- |
| `push(ds)` | 压入当前线程的**栈**（支持嵌套，LIFO） |
| `peek()` | 栈顶 key；未切换为 `null` |
| `poll()` | 弹出栈顶（栈空自动清理 ThreadLocal） |
| `clear()` | 强制清空（异常兜底） |

- 优先级：手动 push > 方法注解 > 类注解；
- 忘了 `poll` → key 残留在复用线程上 → 后续请求串库；
- 需要连续对多个库操作：每个操作前分别 push/poll，或在不同的注解方法中完成；
- 获取当前数据源：`peek()` → `routingDataSource.getDataSource(key)`。

详见文档站：[编程式切换](https://doc.xiuceyun.cn/docs/advanced/manual-switching) ·
[@DS 与线程](https://doc.xiuceyun.cn/docs/faq/threads)

## 相关文档

- 文档站：[@DS 在事务内不切换数据源](https://doc.xiuceyun.cn/docs/tx/basics)（含排查清单与四种方案详情）
- 文档站：[@DSTransactional 语义边界](https://doc.xiuceyun.cn/docs/tx/local-tx)
- 仓库内：[Atomikos XA 跨库事务](atomikos-xa.md)
