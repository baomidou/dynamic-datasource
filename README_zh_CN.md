<p align="center">
  <img src="https://images.gitee.com/uploads/images/2019/0626/231046_f44892b9_709883.png" alt="dynamic-datasource" width="150" />
</p>

<h1 align="center">Dynamic Datasource 动态数据源</h1>

<p align="center">
  <strong>Spring Boot 多数据源快速集成启动器 —— 一个注解完成切换。</strong><br/>
  读写分离 · 一主多从 · 异构多库 · 动态路由 · 分布式与本地多数据源事务
</p>

<p align="center">
  <a href="./README.md">English</a> · <b>简体中文</b>
</p>

<p align="center">
  <a href="https://github.com/baomidou/dynamic-datasource/actions/workflows/ci.yml">
    <img src="https://github.com/baomidou/dynamic-datasource/actions/workflows/ci.yml/badge.svg" alt="CI" />
  </a>
  <a href="https://github.com/baomidou/dynamic-datasource/releases">
    <img src="https://img.shields.io/github/v/release/baomidou/dynamic-datasource?label=release&color=blue" alt="Release" />
  </a>
  <a href="https://central.sonatype.com/artifact/com.baomidou/dynamic-datasource-spring-boot-starter">
    <img src="https://img.shields.io/maven-central/v/com.baomidou/dynamic-datasource-spring-boot-starter.svg?label=boot2%20starter&color=green" alt="Boot 2 starter" />
  </a>
  <a href="https://central.sonatype.com/artifact/com.baomidou/dynamic-datasource-spring-boot3-starter">
    <img src="https://img.shields.io/maven-central/v/com.baomidou/dynamic-datasource-spring-boot3-starter.svg?label=boot3%20starter&color=green" alt="Boot 3 starter" />
  </a>
  <a href="https://central.sonatype.com/artifact/com.baomidou/dynamic-datasource-spring-boot4-starter">
    <img src="https://img.shields.io/maven-central/v/com.baomidou/dynamic-datasource-spring-boot4-starter.svg?label=boot4%20starter&color=green" alt="Boot 4 starter" />
  </a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0.html">
    <img src="https://img.shields.io/badge/license-Apache--2.0-brightgreen.svg" alt="License" />
  </a>
  <img src="https://img.shields.io/badge/JDK-8%2B%20(Boot3%2F4%2017%2B)-orange.svg" alt="JDK" />
  <img src="https://img.shields.io/badge/Spring%20Boot-1.5%20~%204.x-99ccff.svg" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/GraalVM%20Native-CI%20tested-4299f1.svg" alt="GraalVM Native Image" />
  <a href="https://scorecard.dev/viewer/?uri=github.com/baomidou/dynamic-datasource">
    <img src="https://api.securityscorecards.dev/projects/github.com/baomidou/dynamic-datasource/badge" alt="OpenSSF Scorecard" />
  </a>
  <a href="https://baomidou.github.io/dynamic-datasource/">
    <img src="https://img.shields.io/badge/API%20docs-Javadoc-4c9b3c.svg" alt="Javadoc" />
  </a>
</p>

---

## 目录

- [为什么选择它](#为什么选择它)
- [核心特性](#核心特性)
- [版本兼容](#版本兼容)
- [快速开始](#快速开始)
- [命名约定与切换规则](#命名约定与切换规则)
- [事务支持](#事务支持)
- [动态数据源 key](#动态数据源-key)
- [运行时管理能力](#运行时管理能力)
- [生态集成](#生态集成)
- [模块划分](#模块划分)
- [文档](#文档)
- [贡献与安全](#贡献与安全)

---

## 为什么选择它

给一个 Spring Boot 应用接入第二个数据库，通常意味着：手写第二个 `DataSource` Bean、第二个事务
管理器、第二套 MyBatis/JPA 装配，而且仍然不知道该怎么让某一次调用走到指定的库上。

**dynamic-datasource** 把这一切收敛成一个 starter：在 YAML 里声明数据源，在类或方法上标注
`@DS("slave")`，框架在方法调用时解析连接、调用结束后自动恢复，并且支持跨服务层的多层嵌套。
项目自 2018 年维护至今，同时面向三代 Spring Boot 发布产物，CI 在 JDK 17/21/25/27 矩阵（Boot 2
线通过 JDK 8 toolchain 编译与测试）以及 GraalVM Native Image 上持续验证。

> 它只做一件事——**决定一次调用打到哪个 `DataSource` 上**——并且刻意不限制你随后做什么。任意 CRUD、
> 任意 ORM、任意事务策略。

> 完整文档（所有配置项、连接池适配与集成方案）在 [秀策云文档站](https://doc.xiuceyun.cn)，
> 后续会逐页迁入 [Wiki](https://github.com/baomidou/dynamic-datasource/wiki)，详见[文档](#文档)。

## 核心特性

| 维度 | 能力 |
| --- | --- |
| **数据源分组** | 纯粹多库、读写分离、一主多从、混合模式，全靠命名即可表达。 |
| **动态路由** | `@DS` 可标注在方法/类/接口/抽象类上，就近生效；组名走负载均衡或随机策略。 |
| **事务支持** | `@DSTransactional` 本地多数据源事务（7 种传播行为），Seata `AT` / `XA` 与 Atomikos XA 分布式事务。 |
| **配置加密** | 内置 `ENC(...)` 敏感信息加密，算法与公钥可自定义；SpEL 类型访问默认关闭。 |
| **生命周期** | 支持无数据源启动、懒加载、运行期动态增删数据源、优雅关闭（等待活跃连接）。 |
| **可扩展** | 自定义注解（以 `@DS` 作为元注解）、自定义 `DsProcessor` 责任链、自定义 `DataSourceProvider`、可插拔 `DataSourceCreator`。 |
| **连接池** | Druid、HikariCP、BeeCP、DBCP2、C3P0、Oracle UCP、无池 basic 与 JNDI 的便捷集成。 |
| **原生镜像** | GraalVM Reachability Metadata 作为独立构件发布，`nativeTest` 是每次提交的准入门禁。 |

## 版本兼容

按你的 Spring Boot 版本选择对应的 starter（各产线的维护状态见 [SUPPORT.md](./SUPPORT.md)）：

| Spring Boot | JDK 要求 | Starter 构件 | 项目开始支持版本 |
| :---: | :---: | :--- | :---: |
| 1.5.x ~ 2.x.x | JDK 8+ | `dynamic-datasource-spring-boot-starter` | 1.0.0 |
| 3.x.x | JDK 17+ | `dynamic-datasource-spring-boot3-starter` | 4.0.0 |
| 4.x.x | JDK 17+ | `dynamic-datasource-spring-boot4-starter` | 4.5.0 |

> 连接池（Druid / HikariCP 等）与 JDBC 驱动 **不包含** 在本 starter 中，需项目自行引入并保持版本
> 匹配——这正是为了保证构件轻量，避免 shaded 式打包带来的依赖冲突。

## 快速开始

**1 · 引入依赖**（下例为 `4.5.0`，请替换为 [最新 released 版本](https://github.com/baomidou/dynamic-datasource/releases)）。

<details>
<summary><b>Maven</b></summary>

```xml
<!-- Spring Boot 2.x（含 1.5.x） -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- Spring Boot 3.x -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot3-starter</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- Spring Boot 4.x -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot4-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

</details>

<details>
<summary><b>Gradle</b></summary>

```groovy
// 按你的 Spring Boot 版本三选一
implementation 'com.baomidou:dynamic-datasource-spring-boot-starter:4.5.0'
implementation 'com.baomidou:dynamic-datasource-spring-boot3-starter:4.5.0'
implementation 'com.baomidou:dynamic-datasource-spring-boot4-starter:4.5.0'
```

</details>

**2 · 配置数据源。** `datasource:` 之下的每个 key 都是一个数据源名，第一个 `_` 之前的部分即组名。

```yaml
spring:
  datasource:
    dynamic:
      enabled: true              # 是否启用动态数据源，默认 true
      primary: master            # 默认数据源或数据源组，默认 master
      strict: false              # 严格模式：true 时未匹配到数据源直接抛异常
      grace-destroy: false       # 优雅关闭：true 时至多等待 10s 再强制关闭活跃连接
      datasource:
        master:
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver   # 3.2.0 起支持 SPI，可省略
        slave_1:
          url: jdbc:mysql://xx.xx.xx.xx:3307/dynamic
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave_2:
          url: ENC(xxxxx)        # 内置加密，详见文档站
          username: ENC(xxxxx)
          password: ENC(xxxxx)
          driver-class-name: com.mysql.cj.jdbc.Driver
```

以上配置得到一个 `master` 数据源，以及一个包含 `slave_1`、`slave_2` 的 `slave` 组。其他形态同样只是命名：

```yaml
# 一主多从                  # 纯粹多库                  # 混合配置
master:                    mysql:                     master:
slave_1:                   oracle:                    slave_1:
slave_2:                   sqlserver:                 slave_2:
                           h2:                        oracle_1:
                                                      oracle_2:
```

**3 · 用 `@DS` 切换数据源。** 可注解在方法或类上，同时存在时 **就近原则，方法上的注解优先**。

```java
@Service
@DS("slave")
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DS("master") // 指定到主库
    public List<Map<String, Object>> selectAll() {
        return jdbcTemplate.queryForList("select * from user");
    }

    @DS("slave_1") // 指定到具体某个库
    public List<Map<String, Object>> selectByCondition() {
        return jdbcTemplate.queryForList("select * from user where age > 10");
    }
}
```

| 注解 | 解析结果 |
| :--- | :--- |
| 不加注解 | 默认数据源（`primary`） |
| `@DS("slave")` | `slave` **组**，在组内按策略负载均衡 |
| `@DS("slave_1")` | `slave_1` **具体数据源** |
| `@Master` / `@Slave` | 内置快捷注解，分别等价于 `@DS("master")` / `@DS("slave")` |

只要一个注解自身被 `@DS` 元注解标注，它就能当作数据源切换注解使用——`@Master`、`@Slave` 就是这么来的，
你也可以据此定义属于自己业务语义的注解（例如内部标注 `@DS("oracle")` 的 `@OracleDs`）。

## 命名约定与切换规则

1. 所有数据源配置均位于 `spring.datasource.dynamic.datasource` 之下。
2. 第一个 `_` 之前的部分为 **组名**，同组的数据源会被归入同一个 `GroupDataSource`。
3. 切换目标既可以是组名也可以是具体数据源名；组由 `strategy` 解析（默认负载均衡，可选随机）。
4. 默认数据源名为 `master`，可通过 `spring.datasource.dynamic.primary` 修改。
5. 优先级由高到低：**代码中主动切换（`DynamicDataSourceContextHolder.push`）> 方法上的注解 > 类上的注解**，同时支持继承自接口与抽象类的 `@DS`。
6. 数据源以栈的形式压入/弹出，因此 `ServiceA >>> ServiceB >>> ServiceC` 跨库多层嵌套切换行为符合预期。

## 事务支持

**本地多数据源事务** —— `@DSTransactional` 在一个事务 id 下绑定各参与数据源的连接，到边界时统一提交或
整体回滚；嵌套事务则由 save point 支撑。最后一个库发生失败时，先前已写入的库也会一并回滚。

```java
@DSTransactional(propagation = DsPropagation.REQUIRED, rollbackFor = Exception.class)
@DS("master")
public void transfer() {
    userMapper.debit();     // master
    logMapper.write();      // log_1，调用链内部通过 @DS 切换到另一个库
}
```

传播行为可选：`REQUIRED`（默认）、`REQUIRES_NEW`、`NESTED`、`SUPPORTS`、`NOT_SUPPORTED`、`MANDATORY`、`NEVER`。
可通过 `@DsTxEventListener` 监听提交/回滚等事务事件，它是多数据源场景下 Spring `@TransactionalEventListener` 的对应物。

**分布式事务** —— 两种都是一等公民：

```yaml
# 1）Seata：为每个数据源包装代理，参入全局提交
spring:
  datasource:
    dynamic:
      seata: true            # 为数据源包装 Seata 代理
      seata-mode: AT         # AT（默认）或 XA
```

```yaml
# 2）Atomikos XA：driver-class-name 直接指向 XA 数据源
spring:
  datasource:
    dynamic:
      atomikos:              # 全局连接池参数，可在单数据源上覆盖
        max-pool-size: 10
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.MysqlXADataSource
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
```

已支持的 XA 资源：MySQL（5.x 与 8+）、Oracle、PostgreSQL、H2；MyBatis 环境下搭配 `AtomikosTransactionFactory` 使用。

## 动态数据源 key

`@DS` 的值不必是字面量。内置的 `DsProcessor` 责任链可以按请求解析，你也可以在链首加入自己的处理器：

```java
@DS("#header.ds")          // 从 HTTP 请求头取数据源 key
@DS("#session.dsName")     // 从 HTTP Session 取
@DS("#user.level")         // SpEL：使用方法参数
@DS("@dsLocator.for(#id)") // SpEL：引用 Spring Bean
```

> SpEL 的类型引用（`T(java.lang.Runtime)`）与构造表达式 **默认关闭**，以阻断表达式注入。确有需求时
> 再通过 `spring.datasource.dynamic.aop.allowed-spel-type-access: true` 打开。
> 相关 AOP 配置项：`aop.enabled`、`aop.order`（默认 `HIGHEST_PRECEDENCE`）、`aop.allowed-public-only`。

## 运行时管理能力

```java
@Autowired
private DynamicRoutingDataSource routingDataSource;
@Autowired
private DefaultDataSourceCreator dataSourceCreator;

public void openTenant(String tenantId) {
    DataSourceProperty property = new DataSourceProperty();
    property.setPoolName("tenant_" + tenantId);
    property.setUrl("jdbc:mysql://db.internal:3306/tenant_" + tenantId);
    property.setUsername("app");
    property.setPassword("secret");
    // property 会走 creator 责任链，变成一个初始化完成的真实连接池
    routingDataSource.addDataSource(property.getPoolName(), dataSourceCreator.createDataSource(property));
}

public void closeTenant(String tenantId) {
    routingDataSource.removeDataSource("tenant_" + tenantId); // 若开启优雅关闭，会先等待活跃连接
}
```

- `lazy: true`：数据源在首次使用时创建，而非启动期。
- 支持 **无数据源启动**；`AbstractJdbcDataSourceProvider` 可让数据源清单本身来自一个引导库。
- 每个数据源可独立执行初始化脚本（`init.schema` / `init.data`），各自的分隔符与出错策略可调，异构库群也能各自 bootstrap。
- `grace-destroy: true`：关闭时等待（至多 10s）活跃连接释放后再强制销毁。
- `ENC(...)` 密码加密，支持全局或单数据源 `public-key`，并以 `EncDataSourceInitEvent` 作为自定义算法（AES、SM4、KMS…）的扩展点。
- `p6spy: true` 为数据源套上 SQL 日志代理；`seata: false` 可让单个数据源跳过 Seata 包装。

## 生态集成

- **MyBatis / MyBatis-Plus**：可直接作为普通 `DataSource` 使用；`MasterSlaveAutoRoutingPlugin` 无需任何
  注解即可实现纯读写分离（`SELECT` 走 `slave`，其余走 `master`）。
- **Quartz**：把调度器的 `DataSource` 换成动态数据源，文档中的方案同时覆盖 JDBC JobStore。
- **ShardingSphere / ShardingJDBC、P6spy、JNDI**：提供集成方案，具体写在文档站（doc.xiuceyun.cn），相关内容正在逐页迁回 wiki。
- **GraalVM Native Image**：本项目、Druid 与 HikariCP 的 Reachability Metadata 位于
  `dynamic-datasource-native-metadata`，starter 用户通过官方元数据仓库自动获取。

## 模块划分

| 模块 | 职责 |
| --- | --- |
| `dynamic-datasource-creator` | 数据源创建：各连接池适配（Druid/HikariCP/BeeCP/DBCP2/C3P0/Oracle UCP/basic）、XA 资源、加解密工具 |
| `dynamic-datasource-spring` | 核心实现：`DynamicRoutingDataSource`、组/单项数据源、`@DS` AOP、上下文栈、事务引擎、路由策略、插件 |
| `dynamic-datasource-spring-boot-common` | 共享的 `DynamicDataSourceProperties` 与自动装配基础件 |
| `dynamic-datasource-spring-boot-starter` | Spring Boot 1.5.x ~ 2.x.x（`javax.*`）入口 |
| `dynamic-datasource-spring-boot3-starter` | Spring Boot 3.x（`jakarta.*`）入口 |
| `dynamic-datasource-spring-boot4-starter` | Spring Boot 4.x 入口 |
| `dynamic-datasource-native-metadata` | 以构件形式发布的 GraalVM Reachability Metadata |
| `dynamic-datasource-test-common` | 共享测试夹具，不发布 |

## 文档

> **遇到问题？先看 [Wiki FAQ](https://github.com/baomidou/dynamic-datasource/wiki)**：`@DS` 不生效、启动报错、事务内切换、
> 线程与异步、连接池参数不生效等历史高频问题均已整理为排查清单，多数问题可在此直接找到答案；
> 确认是框架缺陷或 FAQ 未覆盖的问题，再提 issue。

- **文档站 · 秀策云** — <https://doc.xiuceyun.cn>：当前最完整的配置项、集成方案与进阶用法均在此。
- **Wiki（中文）** — <https://github.com/baomidou/dynamic-datasource/wiki>
- **Wiki (English)** — <https://github.com/baomidou/dynamic-datasource/wiki/Home-English>
  wiki 是文档后续的沉淀方向，正在逐页补齐；在对应主题迁入 wiki 之前，仍以文档站为准。
- **FAQ 精选** — [@DS 不生效排查总表](https://github.com/baomidou/dynamic-datasource/wiki/DS-Not-Working-Checklist) ·
  [事务内不切换](https://github.com/baomidou/dynamic-datasource/wiki/DS-Switching-Inside-Transactions) ·
  [启动报错速查](https://github.com/baomidou/dynamic-datasource/wiki/Startup-Errors) ·
  [线程与异步](https://github.com/baomidou/dynamic-datasource/wiki/DS-And-Threads) ·
  [全部 FAQ](https://github.com/baomidou/dynamic-datasource/wiki)
- **示例工程** — <https://github.com/dynamic-datasource/dynamic-datasource-samples>
- **QQ 群**（中文交流）— [点击入群](https://shang.qq.com/wpa/qunwpa?idkey=ded31006508b57d2d732c81266dd2c26e33283f84464e2c294309d90b9674992)
- **旧版看云文档（仅作历史参考）** — <https://www.kancloud.cn/tracy5546/dynamic-datasource>

## AI 助手支持（Skill）

仓库内置开放格式的 Agent Skill（[`skills/dynamic-datasource`](./skills/dynamic-datasource/SKILL.md)）。把它装进你的项目后，AI 编程助手（Qoder / Claude Code / Cursor 等）在本项目上下文中可以直接回答：**基础安装与最小配置**，以及**最常见问题的排查**——"为什么数据源切换失败 / @DS 不生效"、启动报错、事务内切换、线程与异步、连接池参数、密码加密等。

安装（任选其一）：

- skills CLI：`npx skills add baomidou/dynamic-datasource`
- 手动复制：把 `skills/dynamic-datasource/` 目录复制进你项目的 `.qoder/skills/`（Qoder）或 `.claude/skills/`（Claude Code）等 AI 工具的 skills 目录
- 或者直接把 [`SKILL.md`](./skills/dynamic-datasource/SKILL.md) 的链接发给你的 AI 助手，让它依此上下文作答

## 贡献与安全

- 欢迎社区贡献：Gradle/toolchain 环境准备、`nativeTest` 门禁与发布流程见 [CONTRIBUTING.md](./CONTRIBUTING.md)。
- 发现安全漏洞请勿直接开公开 issue，请按 [SECURITY.md](./SECURITY.md) 处理。
- 本项目基于 [Apache License 2.0](./LICENSE) 发布，自 2018 年起由 [baomidou 组织](https://github.com/baomidou)维护，主仓库为
  [baomidou/dynamic-datasource](https://github.com/baomidou/dynamic-datasource)。

<p align="center">
  <a href="https://github.com/baomidou/dynamic-datasource/stargazers">
    <img src="https://img.shields.io/github/stars/baomidou/dynamic-datasource?style=social" alt="Stars" />
  </a>
</p>
