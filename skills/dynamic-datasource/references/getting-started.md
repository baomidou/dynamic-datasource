# 基础安装与使用 | Getting Started

> 本文是 [SKILL.md](../SKILL.md) 的参考文档：安装、最小配置、命名约定、`@DS` 语义、动态 key、运行时增删数据源。

## 1. 引入依赖（按 Spring Boot 大版本三选一）

| Spring Boot | artifactId |
| --- | --- |
| 1.5.x / 2.x | `dynamic-datasource-spring-boot-starter` |
| 3.x（JDK 17+） | `dynamic-datasource-spring-boot3-starter` |
| 4.x | `dynamic-datasource-spring-boot4-starter` |

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot3-starter</artifactId>
    <version>4.6.0</version>  <!-- 以 Maven Central / GitHub Releases 最新版为准 -->
</dependency>
```

Gradle：`implementation 'com.baomidou:dynamic-datasource-spring-boot3-starter:4.6.0'`

## 2. 最小配置

```yaml
spring:
  datasource:
    dynamic:
      primary: master            # 默认数据源或数据源组
      strict: false              # true 时未匹配到数据源直接抛异常（推荐开发环境开启）
      datasource:
        master:
          url: jdbc:mysql://host1:3306/db
          username: root
          password: 123456
        slave_1:
          url: jdbc:mysql://host2:3306/db
          username: root
          password: 123456
        slave_2:
          url: jdbc:mysql://host3:3306/db
          username: root
          password: 123456
```

上面得到一个 `master` 数据源 + 一个包含 `slave_1`、`slave_2` 的 `slave` 组。

**命名约定**：

1. 所有数据源都在 `spring.datasource.dynamic.datasource` 之下，每个 key 是一个数据源名；
2. 第一个 `_` 之前的部分是**组名**，同组数据源归入一个 `GroupDataSource`；
3. 切换目标既可以是组名也可以是具体数据源名；组由 `strategy` 解析（默认 `LoadBalanceDynamicDataSourceStrategy` 轮询，可选随机或自定义）；
4. 默认数据源名是 `master`，可用 `spring.datasource.dynamic.primary` 修改。

## 3. `@DS` 语义与优先级

```java
@Service
@DS("slave")                      // 类级：本类所有方法默认走 slave 组
public class UserServiceImpl implements UserService {

    @DS("master")                 // 方法级优先于类级
    public List<User> selectAll() { ... }

    @DS("slave_1")                // 指定具体数据源，跳过组负载均衡
    public List<User> selectByCondition() { ... }
}
```

- **优先级**：代码中主动切换（`DynamicDataSourceContextHolder.push`）> 方法注解 > 类注解；接口与父类上的 `@DS` 可被继承；
- **解析结果**：`@DS("slave")` → 组（组内负载均衡）；`@DS("slave_1")` → 具体数据源；`@Master` / `@Slave` 是内置快捷注解；
- **注解位置**：推荐 Service 实现类方法。加在 Mapper 上 3.2.0+ 大部分场景可用，但只有确定该 Mapper 只属于一个数据源时才建议这么做；
- 只要注解自身被 `@DS` 元注解标注，就可以当数据源切换注解用（可自定义业务语义注解，如 `@OracleDs`）。

## 4. `@DS` 不生效？先看这条

`@DS` 是 Spring AOP 切面，只拦截**经过代理的、被注解的方法**。以下情况一定不生效：

- 同类内部 `this.method()` 自调用（不经过代理）；
- `static` 方法、自己 `new` 的对象、非 Spring Bean 的工具类；
- 外层已开启 Spring 事务（连接已绑定，无法换库）。

完整排查见 [troubleshooting.md](troubleshooting.md)。

## 5. 动态数据源 key（SpEL）

`@DS` 的值以 `#` 开头时交给 SpEL 处理器解析：

```java
@DS("#header.ds")          // HTTP 请求头（内置处理器）
@DS("#session.dsName")     // HTTP Session（内置处理器）
@DS("#user.level")         // 方法参数属性
@DS("#p0")                 // 按参数位置（参数名未保留时用）
@DS("@dsLocator.for(#id)") // 引用 Spring Bean
```

- 按参数名取值需要编译时保留参数名（`-parameters`，Spring Boot 插件默认开启），否则用 `#p0`；
- 解析失败/取不到值 → 回落 primary（非 strict）；
- 4.6 起 `T(...)` 类型引用与 `new` 构造表达式**默认禁用**（防注入，CVE-2026-7045）；确有需要才通过 `spring.datasource.dynamic.aop.allowed-spel-type-access: true` 打开，并知悉风险。

## 6. 运行时增删数据源

```java
@Autowired
private DynamicRoutingDataSource routingDataSource;
@Autowired
private DefaultDataSourceCreator dataSourceCreator;

// 添加（推荐经 creator 创建，自动应用全局配置/加密/初始化脚本）
DataSourceProperty property = new DataSourceProperty();
property.setPoolName("tenant_abc");
property.setUrl("jdbc:mysql://db.internal:3306/tenant_abc");
property.setUsername("app");
property.setPassword("secret");
routingDataSource.addDataSource(property.getPoolName(), dataSourceCreator.createDataSource(property));

// 删除（会真实关闭底层连接池；primary 不可删；同名的旧池被替换时会自动关闭）
routingDataSource.removeDataSource("tenant_abc");
```

- `grace-destroy: true`：关闭数据源/应用时等待活跃连接（至多约 10s）再销毁；
- **集群部署**：`addDataSource` 只作用于当前 JVM，每台实例都要执行（启动时统一加载 / MQ 广播 / 配置中心刷新）；
- 清单来自引导库：继承 `AbstractJdbcDataSourceProvider`；
- 判断数据源存在：`routingDataSource.getDataSources().containsKey(name)` 或 `getGroupDataSources().containsKey(group)`。

## 7. 快速自检清单

- 启动日志：`initial loaded [N] datasource,primary datasource named [master]`；
- 切换验证：业务代码里 `DynamicDataSourceContextHolder.peek()`（`null` = 没切成功）；
- 日志验证：开 `logging.level.com.baomidou.dynamic.datasource: debug` 看 `switch to the datasource named [...]`；
- 开发环境建议：`strict: true`，让写错的 key 立即报错而不是静默回落主库。

## 相关文档

- 文档站：[@DS 不生效排查总表](https://doc.xiuceyun.cn/docs/faq/not-working) ·
  [编程式切换](https://doc.xiuceyun.cn/docs/advanced/manual-switching) ·
  [动态增删数据源](https://doc.xiuceyun.cn/docs/advanced/add-remove-datasource)
- 示例工程：<https://github.com/dynamic-datasource/dynamic-datasource-samples>
