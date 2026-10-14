# 启动报错、连接池与加密 | Errors, Pools & Encryption

> 本文是 [SKILL.md](../SKILL.md) 的参考文档：启动失败、连接池参数、密码加密类问题的排查。
> 排查心法：先看完整堆栈**最底部的第一个 `Caused by:`**。

## 一、启动报错速查

### 1. `Failed to determine a suitable driver class` / `'url' attribute is not specified`

Spring Boot 认为你要走"默认单数据源"装配，但没找到可用 url。逐项检查：

1. 数据源是否真的在 `spring.datasource.dynamic.datasource` 之下（漏层级 / 拼错前缀会导致清单为空）；
2. `spring.datasource.dynamic.enabled` 是否被设成了 `false`；
3. 是否同时引入了 `druid-spring-boot-starter`（它的 auto-configure 会抢先建默认数据源）——排除它：

```yaml
spring:
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
```

4. 多模块项目：yml 是否真的被打包 / profile 是否激活（"本地好好的，打包就报"的典型）。

### 2. `Please check the setting of primary`（旧版本）/ `CannotFindDataSourceException`（新版本）

`primary` 指向的数据源/组不存在。检查拼写、组名规则（`slave_1` 属于 `slave` 组）、profile 是否覆盖丢失。

### 3. `could not find a datasource named xxx`

`strict: true` 下的**正确报错**——切换的 key 不存在。改 key 或修正拼写。

### 4. `enabled P6SPY / ALIBABA SEATA, however without dependency`

开了开关没加依赖（`p6spy: true` / `seata: true`）。补依赖或关闭开关。

### 5. `NoClassDefFoundError: io/seata/...` / `ArrayStoreException: TypeNotPresentExceptionProxy`

- 前者：Seata 类被引用但依赖缺失（同第 4 条）；
- 后者：**Spring Boot 大版本用错了 starter 产物**（Boot 3 项目用了 Boot 2 的 starter 或反之）。按 `README` 的版本表三选一：
  Boot 1.5/2.x → `-starter`；Boot 3.x（JDK 17+）→ `-spring-boot3-starter`；Boot 4.x → `-spring-boot4-starter`。

### 6. `Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required`

MyBatis(-Plus) 装配问题在接入多数据源后暴露：确认 mybatis(-plus) starter 与 Boot 大版本匹配、容器中只有一个 `SqlSessionFactory`。

### 7. 某库连不上导致整个应用起不来

- `lazy: true`：数据源延迟到首次使用才创建；
- 池层面放宽：Hikari `initialization-fail-timeout: -1`；Druid `fail-fast: false` + `initial-size: 0`；
- 注意 `lazy` 只是推迟错误，生产上应保证配置正确。

### 8. 启动日志对照

| 日志 | 含义 |
| --- | --- |
| `initial loaded [N] datasource,primary datasource named [master]` | 正常 |
| `initial loaded [0] datasource,Please add your primary datasource...` | 清单为空（回第 1 条） |
| `start closing .... all closed success,bye` | 正常关闭流程，不是错误 |

## 二、连接池

### 支持范围与优先级

HikariCP（默认）> Druid > BeeCP > DBCP2 > C3P0 > OracleUCP > Atomikos > JNDI；单库可用 `type` 强制指定。

### 配置层级（"参数不生效"的第一大来源）

```yaml
spring:
  datasource:
    dynamic:
      druid:            # ① 全局默认
        max-wait: 3000
      hikari:
        maximum-pool-size: 20
      datasource:
        master:
          druid:        # ② 单数据源覆盖全局
            max-wait: 5000
```

- ❌ `spring.datasource.druid.*` / `spring.datasource.hikari.*`（不属于本框架，不会被读取）；
- ✅ `spring.datasource.dynamic.druid.*`、`spring.datasource.dynamic.datasource.<name>.druid.*`。

### druid 兼容窗口（历史反复爆发的坑）

| dynamic-datasource | druid | 结果 |
| --- | --- | --- |
| ≤ 3.6.1 | 1.2.17+ | ❌ 启动失败，升级框架到 4.x 解决 |
| 4.x | 1.2.x 较新版 | ✅ |
| 4.2.x | 1.2.22 | ⚠️ 排除 `DruidDataSourceAutoConfigure`（见上） |

### 参数陷阱

- Druid `max-wait` 默认 **-1（无限等待）**：连接耗尽时卡死而非快速失败，生产建议显式设置；
- Druid `socket-timeout` / `connect-timeout` 自 1.2.12 起**默认 10s**：慢 SQL 被 10s 掐断时先看它；
- Druid `wall` 参数（`multi-statement-allow` 等）要配在 `wall` 子块里，且 `filters` 包含 `wall`；
- Hikari `connection-timeout` 默认 30s：出现 `Connection is not available, request timed out after 30001ms` 先查连接泄漏/池上限。

### 监控

- Hikari 指标（actuator/prometheus）：4.x 内置绑定，`spring.datasource.dynamic.hikari-metrics.enabled` 默认 true；
- Druid 监控页（stat-view-servlet）：框架不会自动注册，需手动注册 `StatViewServlet` 或保留 druid starter 的监控配置。

## 三、密码加密（ENC）

### 用法

```java
// 1) 用框架工具生成密文（RSA 自动分组，务必用它而不是第三方工具）
System.out.println("ENC(" + CryptoUtils.encrypt("my-db-password") + ")");
```

```yaml
# 2) url / username / password 均可使用；public-key 可全局或单库配置
spring:
  datasource:
    dynamic:
      datasource:
        master:
          password: ENC(密文)
```

- 自定义算法（AES/SM4/KMS）：注册自定义 `DataSourceInitEvent` Bean，或覆盖 `EncDataSourceInitEvent`。

### 常见报错

| 报错 | 原因 / 解决 |
| --- | --- |
| `Data must not be longer than 53 bytes` | 默认 RSA 密钥单块 53 字节上限；用 `CryptoUtils.encrypt(...)`（内置分组）生成 |
| `could not decrypt an ENC(...) property, check the public-key configuration`（4.6+） | 公钥配错/密文不完整；老版本会把密文当明文（表现为莫名 `Access denied`），升级可获准确报错 |
| 与 jasypt 冲突 | jasypt 默认语法也是 `ENC(...)`，改其前后缀（`jasypt.encryptor.property.prefix`）或二选一 |

## 相关文档

- Wiki：[启动报错速查](https://github.com/baomidou/dynamic-datasource/wiki/Startup-Errors) ·
  [连接池常见坑](https://github.com/baomidou/dynamic-datasource/wiki/Connection-Pool-Gotchas) ·
  [密码加密](https://github.com/baomidou/dynamic-datasource/wiki/Password-Encryption)
