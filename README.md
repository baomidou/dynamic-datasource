<p align="center">
  <img src="https://images.gitee.com/uploads/images/2019/0626/231046_f44892b9_709883.png" alt="dynamic-datasource" width="150" />
</p>

<h1 align="center">Dynamic Datasource</h1>

<p align="center">
  <strong>Multi-datasource routing for Spring Boot — switch with a single annotation.</strong><br/>
  Read/write splitting · one master / many slaves · polyglot databases · dynamic routing · distributed &amp; local transactions
</p>

<p align="center">
  <b>English</b> · <a href="./README_zh_CN.md">简体中文</a>
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

## Table of Contents

- [Why this project](#why-this-project)
- [Out of scope](#out-of-scope)
- [Highlights](#highlights)
- [Compatibility](#compatibility)
- [Quick start](#quick-start)
- [Naming conventions & routing rules](#naming-conventions--routing-rules)
- [Transactions](#transactions)
- [Dynamic datasource keys](#dynamic-datasource-keys)
- [Runtime management](#runtime-management)
- [Ecosystem integrations](#ecosystem-integrations)
- [Module layout](#module-layout)
- [Documentation](#documentation)
- [Contributing, Security and License](#contributing-security-and-license)

---

## Why this project

Adding a second database to a Spring Boot application usually means hand-writing a second
`DataSource` bean, a second `TransactionManager`, a second set of MyBatis/JPA wiring — and then
still not knowing how to route a call to one of them.

**dynamic-datasource** replaces all of that with a single starter: declare your datasources in
YAML, annotate a class or method with `@DS("slave")`, and the framework resolves the connection at
invocation time, restores it when the call returns, and nests correctly across service layers. It
has been maintained since 2018, ships artifacts for three Spring Boot generations, and is validated
by CI on a JDK 17/21/25/27 matrix (the Boot 2 line is compiled and tested through a JDK 8 toolchain)
plus GraalVM Native Image.

> It does exactly one thing — **choosing which `DataSource` a call runs against** — and deliberately
> does not constrain what you do afterwards. Any CRUD, any ORM, any transaction strategy.

> The full reference — every configuration key, connection pool and integration recipe — lives on
> [doc.xiuceyun.cn](https://doc.xiuceyun.cn) (Chinese); the [wiki](https://github.com/baomidou/dynamic-datasource/wiki/Home-English)
> is taking over page by page. See [Documentation](#documentation).

## Out of scope

dynamic-datasource chooses which `DataSource` a call uses. It deliberately does not:

- Bundle or implement a connection pool. Bring your own pool and JDBC driver; this project provides integrations.
- Enhance ORM behavior or provide a general SQL-rewriting or sharding engine. The MyBatis plugin only selects a datasource for read/write routing; use ShardingSphere for sharding.
- Implement a distributed-transaction kernel. Seata and Atomikos are external integrations.
- Ship a full JPMS `module-info`; the stable module-naming contract is the `Automatic-Module-Name` manifest attribute.
- Propagate transactions across threads. Routing and transaction context are ThreadLocal-based by design.

## Highlights

| Area | What you get |
| --- | --- |
| **Grouping** | Pure multi-DB, read/write splitting, one-master-many-slaves, or a mix of all three, expressed by naming alone. |
| **Routing** | `@DS` on method / class / interface / abstract class, nearest declaration wins; group names resolve through a load-balanced or random strategy. |
| **Transactions** | Local multi-datasource transactions via `@DSTransactional` (7 propagation levels), plus Seata `AT` / `XA` and Atomikos XA for truly distributed commits. |
| **Security** | `ENC(...)` encrypted credentials out of the box, customisable algorithm and public key; SpEL type access restricted by default. |
| **Lifecycle** | Start with zero datasources, lazy-initialise on first use, add/remove datasources at runtime, graceful shutdown that waits for in-flight connections. |
| **Extensibility** | Custom annotations that meta-annotate `@DS`, custom `DsProcessor` chain, custom `DataSourceProvider`, pluggable `DataSourceCreator`. |
| **Connectivity** | Curated integration for Druid, HikariCP, BeeCP, DBCP2, C3P0, Oracle UCP, unpooled basic datasources and JNDI lookups. |
| **Native** | GraalVM reachability metadata published as an artifact; `nativeTest` gates every commit in CI. |

## Compatibility

Pick the starter that matches your Spring Boot line (the maintenance status of each line is defined in [SUPPORT.md](./SUPPORT.md)):

| Spring Boot | JDK | Starter artifact | Available since |
| :---: | :---: | :--- | :---: |
| 1.5.x ~ 2.x.x | JDK 8+ | `dynamic-datasource-spring-boot-starter` | 1.0.0 |
| 3.x.x | JDK 17+ | `dynamic-datasource-spring-boot3-starter` | 4.0.0 |
| 4.x.x | JDK 17+ | `dynamic-datasource-spring-boot4-starter` | 4.5.0 |

> Connection pools (Druid / HikariCP / …) and JDBC drivers are **not** bundled. Bring your own and
> keep the versions aligned with your Spring Boot line — this is what keeps the starter lightweight
> and avoids the classpath conflicts that a shaded starter would cause.

## Quick start

**1 · Add the dependency** (shown for `4.5.0`; replace with the [latest release](https://github.com/baomidou/dynamic-datasource/releases)).

<details>
<summary><b>Maven</b></summary>

```xml
<!-- Spring Boot 2.x (and 1.5.x) -->
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
// choose exactly one, matching your Spring Boot line
implementation 'com.baomidou:dynamic-datasource-spring-boot-starter:4.5.0'
implementation 'com.baomidou:dynamic-datasource-spring-boot3-starter:4.5.0'
implementation 'com.baomidou:dynamic-datasource-spring-boot4-starter:4.5.0'
```

</details>

**2 · Declare your datasources.** Everything after the `datasource:` key is a datasource name; the
part before the first `_` becomes its group.

```yaml
spring:
  datasource:
    dynamic:
      enabled: true              # master switch, default true
      primary: master            # default datasource or group, default "master"
      strict: false              # true = throw when the key does not resolve
      grace-destroy: false       # true = wait up to 10s for active connections on shutdown
      datasource:
        master:
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver   # optional since 3.2.0 (SPI based)
        slave_1:
          url: jdbc:mysql://xx.xx.xx.xx:3307/dynamic
          username: root
          password: 123456
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave_2:
          url: ENC(xxxxx)        # built-in encryption, see the documentation
          username: ENC(xxxxx)
          password: ENC(xxxxx)
          driver-class-name: com.mysql.cj.jdbc.Driver
```

The snippet above yields one `master` datasource plus a `slave` group containing `slave_1` and
`slave_2`. Other shapes are equally declarative:

```yaml
# one master, many slaves        # pure multi-database          # mixed
master:                            mysql:                        master:
slave_1:                           oracle:                       slave_1:
slave_2:                           sqlserver:                    slave_2:
                                   h2:                           oracle_1:
                                                                 oracle_2:
```

**3 · Route with `@DS`.** Applicable to methods and types; when both are present the method wins.

```java
@Service
@DS("slave")
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DS("master") // pinned to the master datasource
    public List<Map<String, Object>> selectAll() {
        return jdbcTemplate.queryForList("select * from user");
    }

    @DS("slave_1") // pinned to one concrete datasource
    public List<Map<String, Object>> selectByCondition() {
        return jdbcTemplate.queryForList("select * from user where age > 10");
    }
}
```

| Annotation | Resolved datasource |
| :--- | :--- |
| none | the `primary` datasource |
| `@DS("slave")` | the `slave` **group**, balanced across its members |
| `@DS("slave_1")` | the `slave_1` **datasource** |
| `@Master` / `@Slave` | shortcuts shipped for the `master` / `slave` names |

Any annotation that is itself meta-annotated with `@DS` works as a datasource switch — that is exactly
how `@Master` and `@Slave` are shipped, and it lets you define a domain-specific one such as
`@OracleDs` declared as `@DS("oracle")`.

## Naming conventions & routing rules

1. All datasource configuration keys are read under `spring.datasource.dynamic.datasource`.
2. The segment before the first `_` is the **group name**; names sharing a group are pooled into one `GroupDataSource`.
3. You may switch to a group name or to a concrete datasource name; groups are resolved by `strategy` (load balance by default, `RandomDynamicDataSourceStrategy` optional).
4. The default datasource is `master`, overridable via `spring.datasource.dynamic.primary`.
5. Resolution priority, nearest wins: **programmatic push (`DynamicDataSourceContextHolder.push`) > method annotation > class annotation**, and `@DS` inherited from interfaces or abstract classes is honoured.
6. The datasource stack is pushed and popped per invocation, so `ServiceA >>> ServiceB >>> ServiceC` nesting across different datasources behaves as expected.

## Transactions

**Local, no external coordinator** — `@DSTransactional` binds one connection per participating
datasource under a transaction id, then commits or rolls all of them back together at the boundary;
nested transactions are handled through save points. A failure in the last datasource undoes what the
earlier ones already wrote.

```java
@DSTransactional(propagation = DsPropagation.REQUIRED, rollbackFor = Exception.class)
@DS("master")
public void transfer() {
    userMapper.debit();     // master
    logMapper.write();      // log_1, switched inside the call chain via @DS
}
```

Propagation levels: `REQUIRED` (default), `REQUIRES_NEW`, `NESTED`, `SUPPORTS`, `NOT_SUPPORTED`, `MANDATORY`, `NEVER`.
Transaction events can be observed with `@DsTxEventListener` (commit / rollback / after-completion), the
multi-datasource counterpart of Spring's `@TransactionalEventListener`.

**Distributed** — two first-class options:

```yaml
# 1) Seata: proxy each datasource for a global commit
spring:
  datasource:
    dynamic:
      seata: true            # wrap datasources for Seata
      seata-mode: AT         # AT (default) or XA
```

```yaml
# 2) Atomikos XA: point driver-class-name at an XA datasource
spring:
  datasource:
    dynamic:
      atomikos:              # global pool tuning; per-datasource overrides apply on top
        max-pool-size: 10
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.MysqlXADataSource
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
```

Supported XA resources: MySQL (5.x and 8+), Oracle, PostgreSQL, H2; on the MyBatis side pair them with
`AtomikosTransactionFactory`.

## Dynamic datasource keys

The `@DS` value does not have to be a literal. The built-in `DsProcessor` chain resolves it per
request, and you can prepend your own processor:

```java
@DS("#header.ds")          // read the datasource key from an HTTP header
@DS("#session.dsName")     // ... or from the HTTP session
@DS("#user.level")         // SpEL against the method arguments
@DS("@dsLocator.for(#id)") // SpEL with a bean reference
```

> SpEL type references (`T(java.lang.Runtime)`) and constructor expressions are **disabled by
> default** to block expression injection. Re-enable them only when genuinely required, via
> `spring.datasource.dynamic.aop.allowed-spel-type-access: true`.

Related AOP knobs: `aop.enabled`, `aop.order` (default `HIGHEST_PRECEDENCE`), `aop.allowed-public-only`.

## Runtime management

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
    // the property goes through the creator chain and becomes a real, initialised pool
    routingDataSource.addDataSource(property.getPoolName(), dataSourceCreator.createDataSource(property));
}

public void closeTenant(String tenantId) {
    routingDataSource.removeDataSource("tenant_" + tenantId); // destroyed, gracefully if configured
}
```

- `lazy: true` — a datasource is created on first access instead of at startup.
- Starting with **no** datasource is supported; `AbstractJdbcDataSourceProvider` lets you load the
  catalogue itself from a bootstrap database.
- Per-datasource initialisation scripts (`init.schema` / `init.data`), each with its own separator and
  error policy, so a group of heterogeneous databases can bootstrap independently.
- `grace-destroy: true` — on shutdown, wait (at most 10s) for active connections before forcing close.
- `ENC(...)` credentials with a global or per-datasource `public-key`, and `EncDataSourceInitEvent`
  as the hook for your own algorithm (AES, SM4, KMS…).
- `p6spy: true` wraps datasources for SQL logging; `seata: false` opts a single datasource out of proxying.

## Ecosystem integrations

- **MyBatis / MyBatis-Plus** — usable as a plain `DataSource`; `MasterSlaveAutoRoutingPlugin` gives
  pure read/write splitting with no annotation at all (`SELECT` → `slave`, everything else → `master`).
- **Quartz** — hand the scheduler the dynamic `DataSource`; the documented recipe covers the JDBC job store.
- **ShardingSphere / ShardingJDBC, P6spy, JNDI** — integration recipes are on the documentation site;
  they are being migrated into the wiki step by step.
- **GraalVM Native Image** — reachability metadata for this project, Druid and HikariCP ships in
  `dynamic-datasource-native-metadata`; starter users pick it up through the native metadata repository.

## Module layout

| Module | Responsibility |
| --- | --- |
| `dynamic-datasource-creator` | Datasource creation: pool adapters (Druid/HikariCP/BeeCP/DBCP2/C3P0/Oracle UCP/basic), XA resources, crypto helpers |
| `dynamic-datasource-spring` | Core: `DynamicRoutingDataSource`, group/item datasources, `@DS` AOP, holder stack, transaction engine, strategies, plugins |
| `dynamic-datasource-spring-boot-common` | Shared `DynamicDataSourceProperties` and auto-configuration building blocks |
| `dynamic-datasource-spring-boot-starter` | Spring Boot 1.5.x ~ 2.x.x (`javax.*`) entry point |
| `dynamic-datasource-spring-boot3-starter` | Spring Boot 3.x (`jakarta.*`) entry point |
| `dynamic-datasource-spring-boot4-starter` | Spring Boot 4.x entry point |
| `dynamic-datasource-native-metadata` | GraalVM reachability metadata shipped as an artifact |
| `dynamic-datasource-test-common` | Shared test fixtures, not published |

## Documentation

> **Facing a problem? Check the [Wiki FAQ](https://github.com/baomidou/dynamic-datasource/wiki) first.**
> The most frequently reported issues — `@DS` not switching, startup errors, switching inside transactions,
> threads/async, pool parameters not taking effect — are distilled into troubleshooting checklists; most
> questions are answered there. File an issue only when it is a genuine bug or not covered by the FAQ.

- **Documentation** — <https://doc.xiuceyun.cn>: the complete guide — tutorials, configuration keys,
  integrations and advanced usage, with a Chinese/English switcher and versioned docs (the former
  paid content is now free for everyone).
- **Wiki (English)** — <https://github.com/baomidou/dynamic-datasource/wiki/Home-English>
- **Wiki (简体中文)** — <https://github.com/baomidou/dynamic-datasource/wiki>
  The Wiki focuses on FAQ & troubleshooting checklists distilled from hundreds of historical issues;
  treat the documentation site as the complete reference.
- **FAQ picks** — [@DS not working checklist](https://github.com/baomidou/dynamic-datasource/wiki/DS-Not-Working-Checklist) ·
  [switching inside transactions](https://github.com/baomidou/dynamic-datasource/wiki/DS-Switching-Inside-Transactions) ·
  [startup errors](https://github.com/baomidou/dynamic-datasource/wiki/Startup-Errors) ·
  [threads & async](https://github.com/baomidou/dynamic-datasource/wiki/DS-And-Threads) ·
  [all FAQs](https://github.com/baomidou/dynamic-datasource/wiki)
- **Samples** — <https://github.com/dynamic-datasource/dynamic-datasource-samples>
- **QQ group** (Chinese-speaking community) — [join](https://shang.qq.com/wpa/qunwpa?idkey=ded31006508b57d2d732c81266dd2c26e33283f84464e2c294309d90b9674992)
- **Legacy docs (Kancloud, historical reference only)** — <https://www.kancloud.cn/tracy5546/dynamic-datasource>

## AI assistant support (Skill)

This repository ships an Agent Skill in the open `SKILL.md` format ([`skills/dynamic-datasource`](./skills/dynamic-datasource/SKILL.md)). Once installed into your project, AI coding assistants (Qoder / Claude Code / Cursor, …) can answer, in your project's context: **basic installation & minimal configuration**, and **troubleshooting for the most common problems** — "why is datasource switching failing / `@DS` not working", startup errors, switching inside transactions, threads/async, pool parameters, password encryption.

Install (any one):

- skills CLI: `npx skills add baomidou/dynamic-datasource`
- Manual: copy the `skills/dynamic-datasource/` directory into your project's `.qoder/skills/` (Qoder) or `.claude/skills/` (Claude Code) directory
- Or simply paste the [`SKILL.md`](./skills/dynamic-datasource/SKILL.md) link to your AI assistant and let it answer from this context

## Contributing, Security and License

- Contributions are welcome — read [CONTRIBUTING.md](./CONTRIBUTING.md) for the Gradle/toolchain
  setup, the `nativeTest` gate and the release procedure.
- Please do **not** open a public issue for a vulnerability; follow [SECURITY.md](./SECURITY.md).
- Released under the [Apache License 2.0](./LICENSE). Maintained since 2018 by the
  [baomidou organisation](https://github.com/baomidou); the canonical repository is
  [baomidou/dynamic-datasource](https://github.com/baomidou/dynamic-datasource).

<p align="center">
  <a href="https://github.com/baomidou/dynamic-datasource/stargazers">
    <img src="https://img.shields.io/github/stars/baomidou/dynamic-datasource?style=social" alt="Stars" />
  </a>
</p>
