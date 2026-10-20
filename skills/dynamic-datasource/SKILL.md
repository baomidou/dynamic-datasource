---
name: dynamic-datasource
description: "dynamic-datasource（Baomidou 多数据源框架）使用与排障指南：基础安装、@DS/@DSTransactional 用法，以及最常见问题排查——数据源切换失败/@DS 不生效、启动报错、事务内不切换、线程与异步、连接池参数、密码加密。Use when the project depends on com.baomidou:dynamic-datasource-*-starter, or when the user asks about 数据源切换失败、@DS 不生效、多数据源配置不生效、@DSTransactional、dynamic datasource switching not working, or multi-datasource troubleshooting."
license: Apache-2.0
---

# dynamic-datasource 使用与排障

Spring Boot 多数据源框架：用 `@DS` 注解在运行时切换数据源，全部配置位于 `spring.datasource.dynamic.*` 之下。

**识别项目**：`pom.xml` / `build.gradle` 中存在 `com.baomidou:dynamic-datasource-spring-boot-starter`（Boot 1.5~2.x）、`dynamic-datasource-spring-boot3-starter`（Boot 3.x）或 `dynamic-datasource-spring-boot4-starter`（Boot 4.x）。

## 基础安装（速览）

1. 按 Spring Boot 大版本三选一引入 starter（上方坐标）；
2. 在 `spring.datasource.dynamic.datasource` 下配置各库（第一个 `_` 之前是组名：`slave_1` 属于 `slave` 组）；默认数据源键为 `master`；
3. 在 **Service 实现类的 public 方法**上标 `@DS`：`@DS("slave")` 走组（组内按策略负载均衡），`@DS("slave_1")` 走具体库；方法注解优先于类注解；
4. 启动日志出现 `initial loaded [N] datasource,primary datasource named [master]` 即装配成功。

> 完整安装、命名约定、优先级规则、SpEL 动态 key、运行时增删数据源 → [references/getting-started.md](references/getting-started.md)

## 排查工作流：用户问「数据源切换失败 / @DS 不生效」时

**先收集三样信息再下结论**（不要凭空猜）：

1. **版本**：Spring Boot / dynamic-datasource / MyBatis-Plus 各自版本；
2. **代码位置**：注解标在哪（类还是方法、实现类还是接口）、调用链怎么走（是否同类自调用）；
3. **日志**：让用户打开 `logging.level.com.baomidou.dynamic.datasource: debug`，看出现的是
   `switch to the datasource named [...]`（切换发生）还是 `switch to the primary datasource`（key 没压栈），或者两条都没有（注解根本没被拦截）。

**按命中率从高到低逐项排除**：

| # | 检查点 | 一句话判断 |
|---|---|---|
| 1 | 同类 `this.` 自调用、静态方法、非 Spring Bean | 没有经过代理，注解不可能生效 |
| 2 | 外层已有 Spring `@Transactional` | 事务先绑定了连接，事务内 `@DS` 无法换库 |
| 3 | MyBatis-Plus 姿势：Controller 直接 `service.list()` / `lambdaQuery().list()` | `IService` 默认方法与链式调用绕过了注解方法 |
| 4 | 目标 key 不存在（拼写、组名规则） | 非 strict 模式静默回落 primary；开 `spring.datasource.dynamic.strict: true` 暴露 |
| 5 | 跨线程：`@Async`、线程池、`parallelStream` | ThreadLocal 不跨线程，子线程走 primary |
| 6 | 手动 `push` 残留 | 未成对 `poll`/`clear` 会污染复用的线程（表现：偶发串库） |

常用自检一行代码：`DynamicDataSourceContextHolder.peek()`（返回 `null` 说明没切成功）。

> 完整排查表（含日志对照、断点验证、MyBatis-Plus 正误写法对照）→ [references/troubleshooting.md](references/troubleshooting.md)

## 常见问题路由

| 用户的问题 | 去哪看 |
|---|---|
| 启动失败：primary 检查 / driver / starter 版本不匹配 / druid 冲突 | [references/errors-and-config.md](references/errors-and-config.md) |
| 连接池参数不生效、druid 与框架的版本兼容、druid 监控页、Hikari 指标 | 同上 |
| 密码加密 `ENC(...)`、解密报错、与 jasypt 冲突 | 同上 |
| 事务里切库不生效、`@DSTransactional` 传播与回滚行为 | [references/transactions-and-threads.md](references/transactions-and-threads.md) |
| Atomikos XA 跨库事务：依赖版本、`type` 配置、JTA 装配、编程式 JTA、建表坑 | [references/atomikos-xa.md](references/atomikos-xa.md) |
| 异步/线程池里切换失效、并发串库 | 同上 |
| 手动切换（push/poll/clear）的正确用法 | 同上 |
| 基础安装、命名约定、`@DS` 语义、SpEL 动态 key、运行时增删数据源 | [references/getting-started.md](references/getting-started.md) |

## 回答问题的准则

- **排障类问题**：先收齐"版本 + 代码位置 + 日志"三样信息，对照排查表逐项排除；不要给"猜一个、改一个"式的修复建议。
- **涉及生产风险的建议要显式提示副作用**：如 `strict: true` 会让未匹配的 key 直接报错；`aop.allowed-spel-type-access: true` 会重新打开 SpEL 注入面（4.6 起默认关闭，CVE-2026-7045）。
- **版本敏感**：`@DSTransactional` 的传播/回滚契约以 4.6+ 为准；druid 与框架存在历史兼容窗口（≤3.6.1 配 druid 1.2.17+ 不兼容）。
- 官方资料（可引用给用户）：文档站 <https://doc.xiuceyun.cn> ；示例工程 <https://github.com/dynamic-datasource/dynamic-datasource-samples> ；仓库内参考 <https://github.com/baomidou/dynamic-datasource/tree/dev/skills/dynamic-datasource/references> 。
