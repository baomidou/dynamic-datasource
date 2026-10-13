## 变更类型 / What kind of change does this PR introduce?

- [ ] Bugfix
- [ ] Feature
- [ ] Code style (formatting, no behaviour change)
- [ ] Refactor
- [ ] Build / CI
- [ ] Doc
- [ ] Other, please describe:

## 描述 / The description of the PR

说明**为什么**需要这个改动，而不只是改了什么；关联对应的 issue（`Fixes #123`）。

## 影响面 / Impact on the core contracts

本框架的核心契约很窄，改动通常会波及其中之一，请勾选并说明：

- [ ] `@DS` 解析 / `DynamicDataSourceContextHolder` 的切换与栈语义（含多层嵌套、同类内部调用）
- [ ] 数据源创建与连接池参数（creator、Druid/HikariCP/beecp/c3p0/dbcp2/UCP）
- [ ] 事务语义（`@DSTransactional`、savepoint、XA/Atomikos、Seata）
- [ ] 分组与负载均衡（`GroupDataSource`、strategy）
- [ ] `ENC(...)` 加解密 / 配置属性（新增或改名配置项）
- [ ] 构建产物（模块划分、POM 依赖范围 api/compileOnly、JDK 基线）
- [ ] 以上均不涉及

新增/改名配置项时，请同步文档 https://doc.xiuceyun.cn 与 `DynamicDataSourceProperties` 的 Javadoc。

## 自检清单 / Checklist

- [ ] 本地在 **JDK 17+** 下 `./gradlew clean test` 通过（Gradle 客户端要求 17+；Boot 2 线模块由 JDK 8 toolchain 编译测试）
- [ ] 涉及 Boot 3/4 特有 API 时，对应 starter 模块也构建通过：`./gradlew build`
- [ ] 为行为变更补充/调整了单元测试
- [ ] 新文件带有 license header（`Copyright © 2018 organization baomidou`）
- [ ] 没有引入新的三方依赖；若必须引入，已在 PR 描述中说明理由与替代方案对比
- [ ] 考虑了 GraalVM Native Image 影响（反射/代理注册），必要时说明为何 nativeTest 可能失败
- [ ] commit message 采用 conventional 前缀（`fix:` / `feat:` / `build:` / `docs:` …），一个 commit 做一件事

## 其他信息 / Other information
