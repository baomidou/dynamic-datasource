# Atomikos XA 跨库事务 | Atomikos XA

> 关联 Issue：#822（Atomikos 6 下 `NoSuchMethodError: setReapTimeout`，修复见 #823）
>
> 可运行示例：<https://github.com/dynamic-datasource/dynamic-datasource-samples/tree/master/spring-boot-4/tx-atomikos-sample>

## 一、先选机制：@DSTransactional 还是 Atomikos XA

| 方案 | 机制 | 适用 |
| --- | --- | --- |
| `@DSTransactional` | 自研的**本地**多库事务：各库连接独立提交，失败时尽力回滚 | 同库实例，或可接受最终一致 |
| **Atomikos XA** | JTA 两阶段提交（2PC），真正的分布式原子性 | 跨库实例 / 跨主机，要求强一致 |

本页只讲 Atomikos XA。注意 creator 责任链里 **HikariCP（默认）优先级高于 Atomikos**
（HikariCP > Druid > BeeCP > DBCP2 > C3P0 > OracleUCP > **Atomikos** > JNDI），
所以 Atomikos 数据源**必须**显式声明 `type`。

## 二、依赖与版本窗口

Atomikos 6.0.0 是首个 jakarta 构件线（`transactions-jdbc` / `transactions-jta` 带 `jakarta` classifier），
Atomikos 5 及更早只有 javax 构件：

| Spring Boot | starter | Atomikos |
| --- | --- | --- |
| 1.5 ~ 2.x | `dynamic-datasource-spring-boot-starter` | 4.x / 5.x（javax，无 classifier） |
| 3.x | `dynamic-datasource-spring-boot3-starter` | **6.0.x，必须带 `jakarta` classifier** |
| 4.x | `dynamic-datasource-spring-boot4-starter` | **6.0.x，必须带 `jakarta` classifier** |

两个容易漏的依赖细节：

- Spring Boot 的 BOM **不管理** Atomikos 版本，需要自己声明（示例仓库统一在 `spring-boot-4-samples`
  的 dependencyManagement 里声明 `atomikos.version`）；
- `transactions-jta` 把 `jakarta.transaction:jakarta.transaction-api` 声明为 **provided**，不会随依赖传递。
  不显式引入时，Spring Boot 的 JTA 自动配置会因缺少 `jakarta.transaction.Transaction` 而退让，
  事务管理器回退成 `JdbcTransactionManager`，XA 根本不会发生。

```xml
<dependency>
    <groupId>com.atomikos</groupId>
    <artifactId>transactions-jdbc</artifactId>
    <version>6.0.1</version>
    <classifier>jakarta</classifier>
</dependency>
<dependency>
    <groupId>com.atomikos</groupId>
    <artifactId>transactions-jta</artifactId>
    <version>6.0.1</version>
    <classifier>jakarta</classifier>
</dependency>
<dependency>
    <groupId>jakarta.transaction</groupId>
    <artifactId>jakarta.transaction-api</artifactId>
</dependency>
```

> Atomikos 6.0.0 移除了 `AtomikosDataSourceBean#setReapTimeout(int)`；dynamic-datasource 4.6.0 及更早版本
> 在 Atomikos 6 下启动即抛 `NoSuchMethodError`（[#822](https://github.com/baomidou/dynamic-datasource/issues/822)），
> 需升级到包含 [#823](https://github.com/baomidou/dynamic-datasource/pull/823) 修复的版本。

## 三、数据源配置：三个必填项

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      atomikos:                      # ① 全局连接池参数，单数据源下可用同样的 key 覆盖
        min-pool-size: 1
        max-pool-size: 10
        borrow-connection-timeout: 30
        max-idle-time: 60
        maintenance-interval: 60
      datasource:
        master:
          # ② type 必须显式声明为 AtomikosDataSourceBean。
          #    Hikari 创建器优先级更高，type 缺省会接管该数据源，并把 XA 数据源类
          #    误当作 java.sql.Driver 加载，启动报 "Failed to load driver class"。
          type: com.atomikos.jdbc.AtomikosDataSourceBean
          # ③ driver-class-name 指向 XA 数据源类（不是普通 JDBC Driver），
          #    url / username / password 原样交给 XA 数据源。
          driver-class-name: com.mysql.cj.jdbc.MysqlXADataSource
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
          username: root
          password: root
```

内置支持的 XA 数据源类（见 `XADataSourceEnum`）：

| 数据库 | XA 数据源类 |
| --- | --- |
| MySQL 8+ | `com.mysql.cj.jdbc.MysqlXADataSource` |
| MySQL 5.1 | `com.mysql.jdbc.jdbc2.optional.MysqlXADataSource` |
| Oracle | `oracle.jdbc.xa.client.OracleXADataSource` |
| PostgreSQL | `org.postgresql.xa.PGXADataSource` |
| H2 | `org.h2.jdbcx.JdbcDataSource` |

其他数据库的 XA 数据源类不在枚举内，不会被 Atomikos 创建器认领，需要自定义 `DataSourceCreator`。

连接池参数（`spring.datasource.dynamic.atomikos.*` 或 `datasource.<name>.atomikos.*`）：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `min-pool-size` | 1 | 最小连接数 |
| `max-pool-size` | 10 | 最大连接数 |
| `borrow-connection-timeout` | 30 | 池空时等待连接的秒数 |
| `reap-timeout` | 0 | 连接最大借用秒数；**Atomikos 6 已移除该配置**，4/5 生效 |
| `max-idle-time` | 60 | 超出 min-pool-size 的空闲连接保留秒数 |
| `test-query` | `SELECT 1` | 归还连接前的验证语句 |
| `maintenance-interval` | 60 | 池维护线程间隔秒数 |
| `default-isolation-level` | -1 | 默认隔离级别，负值忽略 |
| `max-lifetime` | 60 | 连接最大存活秒数，0 为不限制 |

`uniqueResourceName`（Atomikos 要求每个 XA 资源名唯一）自动取数据源的 key，无需配置；
手工 `addDataSource` 时注意不要重名。

## 四、事务管理器装配

Spring Boot 3 及以前会自动装配 Atomikos 的 `UserTransactionManager`；
**Boot 4 起 JTA 自动配置只剩 JNDI 变体，需要自行声明**：

```java
@Configuration
public class JtaConfig {
    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        return new UserTransactionManager();
    }

    @Bean
    public JtaTransactionManager transactionManager(UserTransactionManager tm) {
        return new JtaTransactionManager(tm, tm);
    }
}
```

Atomikos 的事务日志默认落在工作目录的 `transaction-logs`（及 `tmlog*.log`），
异常退出后重启会先做故障恢复，属预期行为。

## 五、事务用法：多数据源 MyBatis 请用编程式 JTA

**不要在多数据源 MyBatis 场景使用 Spring 的 `@Transactional`。**
MyBatis-Spring 在 Spring 事务同步激活时会把整个事务复用同一个 SqlSession（连同同一个连接），
该连接在事务内**第一条语句执行时**才按当时的 `@DS` 确定数据源，之后的 `@DS` 切换全部失效——
跨库写入会集中打到第一个数据源上（典型报错是第二个库的 "Table not found"）。

正确写法是编程式 JTA：不注册 Spring 事务同步，每次 mapper 调用各自取连接并按当前 `@DS` 路由，
Atomikos 自动把连接登记进当前 JTA 事务，`commit` 时对所有已登记的 XA 资源做两阶段提交：

```java
@DS("order")
public void placeOrder(PlaceOrderRequest request) {
    try {
        userTransactionManager.begin();
        orderMapper.insert(order);                          // -> order 数据源
        accountService.deduct(request.getUserId(), amount);  // @DS("account") -> account 数据源
        userTransactionManager.commit();                    // 两个 XA 资源 2PC
    } catch (RuntimeException e) {
        userTransactionManager.rollback();                  // 两个库一起回滚
        throw e;
    } catch (Exception e) {
        userTransactionManager.rollback();
        throw new IllegalStateException("JTA 事务提交失败", e);
    }
}
```

只操作单库时仍可用 Spring 的 `@Transactional`（配合第四节的 `JtaTransactionManager`）。

## 六、MyBatis 必须配 AtomikosTransactionFactory

```java
@Bean
public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDataSource(dataSource);
    // 每次 MyBatis 事务开始时先按当前 @DS 确定真实数据源，再交给 SpringManagedTransaction
    factoryBean.setTransactionFactory(new AtomikosTransactionFactory());
    return factoryBean.getObject();
}
```

同时 `@MapperScan` 显式声明 `sqlSessionFactoryRef = "sqlSessionFactory"`，避免 AOT 下按类型装配丢失。
注意该配置类要求 classpath 上同时有 MyBatis 与 mybatis-spring（由使用方的 MyBatis / MyBatis-Plus starter 提供），
否则 Atomikos 整组自动装配会被跳过。

## 七、XA 数据源不要用 init.schema 建表

`spring.datasource.dynamic.datasource.<name>.init.schema` 的脚本是在 XA 连接池取出的连接上执行的；
Atomikos 在事务管理器未运行时会话不提交，连接关闭时未提交的 DDL 会被回滚，
表根本建不出来（启动不报错，第一次查询才 "Table not found"）。

解法：用普通连接（auto-commit）先建表，或交给 Flyway / Liquibase 管理。
示例仓库的 `tx-atomikos-sample` 用一个 `ApplicationRunner` 连 H2 内存库执行 `schema-*.sql`，
Atomikos 连接池随后连上的是同一个 JVM 内的同名内存库。

## 八、相关链接

- 文档站（完整文档，含本篇的排版版）：<https://doc.xiuceyun.cn>（事务 → Atomikos XA 事务）
- 示例工程：<https://github.com/dynamic-datasource/dynamic-datasource-samples/tree/master/spring-boot-4/tx-atomikos-sample>
