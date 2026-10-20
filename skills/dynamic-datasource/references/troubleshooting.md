# 数据源切换失败排查 | Troubleshooting: Switching Not Working

> 本文是 [SKILL.md](../SKILL.md) 的参考文档：用户说「数据源切换失败 / @DS 不生效 / 一直走主库 / 切了没效果」时按本文逐项排查。
> 基于 150+ 历史 issue 的根因分布整理。

## 第 0 步：先看日志，再看代码

让用户打开 debug 日志：

```yaml
logging:
  level:
    com.baomidou.dynamic.datasource: debug
```

| 日志 | 含义 | 下一步 |
| --- | --- | --- |
| `switch to the datasource named [slave_1]` | key 解析正确，切换动作发生了 | 检查"事务已绑定连接""缓存/SQL 执行路径"两条 |
| `switch to the primary datasource` | **没有任何有效 key**，落回主库 | 检查"注解没被拦截"和"key 不存在" |
| 两条都没有 | 注解所在的方法**根本没进切面** | 检查 AOP 边界（自调用 / 非 public / 非 Bean / MP 姿势） |

一行断点验证：在业务方法内 `String key = DynamicDataSourceContextHolder.peek();` —— `null` = 没切成功。

## 排查清单（按命中率排序）

### 1. 调用没经过 Spring 代理（最高频）

以下调用**不经过代理**，`@DS` 形同虚设：

- 同类内部 `this.method()` 自调用；
- `static` 方法、静态工具类；
- 自己 `new` 出来的对象、没有被 Spring 管理的类。

修复：

```java
// ❌ 自调用
public void a() { this.b(); }       // b() 上的 @DS 不生效

// ✅ 跨 Bean 调用
@Autowired private SelfService self;
public void a() { self.b(); }        // 走代理

// ✅ 或自注入代理（同上）；工具类改为注入代理 Bean，不要用静态方法
```

### 2. 外层已有 Spring 事务

Spring `@Transactional` 在开启时就绑定了连接（通常是 primary）；事务中途 `@DS` 换 key 无法更换已绑定的连接。

**规则：`@DS` 必须发生在事务开启之前。**

- 想在同一事务里换库：做不到（一个 Spring 事务对一个 DataSource 只绑一条连接），改用 `@DSTransactional`；
- 典型场景（`@Transactional` 方法内调 `@DS` mapper、Spring Batch 的 Step 先开事务、Quartz/ShedLock 框架先开事务）详见
  Wiki：[@DS 在事务内不切换数据源](https://doc.xiuceyun.cn/docs/tx/basics)。

### 3. MyBatis-Plus 调用姿势

`@DS` 只认"**执行入口落在被注解的代理方法上**"。以下绕过：

```java
// ❌ Controller 直接调 IService 默认方法 / 链式 wrapper
userService.list();
userService.getById(1L);
userService.lambdaQuery().eq(...).list();

// ✅ 自定义业务方法 + 注解，内部自由使用 MP API
@DS("slave")
public List<User> selectSlaveUsers() {
    return baseMapper.selectList(null);     // OK
    // 或 return this.lambdaQuery().list(); // OK
}
```

- `saveBatch` / `updateBatchById` 的默认实现同样存在绕过，建议自定义方法 + 循环 `baseMapper` 调用；
- ActiveRecord（`Model` 的静态方法）**不支持** `@DS`，改用 Service + Mapper；
- `databaseId`（多厂商方言）不会随 `@DS` 变化，异构多库建议用不同 Mapper。

### 4. 目标 key 不存在

- 检查拼写、大小写与**组名规则**（第一个 `_` 之前是组名：`slave_1` 属于 `slave` 组）；
- 非严格模式下 key 不存在会**静默回落 primary**——这是"注解写了却一直走主库"最隐蔽的原因；
- 让用户开 `spring.datasource.dynamic.strict: true`，未匹配立即抛 `CannotFindDataSourceException`。

### 5. 跨线程 / 异步 / 并发

- `@Async`、线程池、`CompletableFuture`、`parallelStream`：ThreadLocal 不跨线程，子线程走 primary；
- 正确姿势：**子线程内**调用带 `@DS` 的代理方法，或任务内手动 `push`/`poll` 包裹；
- "偶发串库"根因多是手动 `push` 后异常路径漏了 `poll`/`clear`，污染复用线程。

详见 [transactions-and-threads.md](transactions-and-threads.md)。

### 6. 其他时机问题

- 注解标在**测试类本身**（而非 Bean 方法）上不生效；
- `@PostConstruct` / 应用启动早期阶段调用可能拿不到切换效果，移到请求路径执行；
- 非 public 方法默认不拦截（`aop.allowed-public-only=true`）；
- 手动 `push` 的优先级**高于**注解：检查是否被残留 push 覆盖。

## 症状 → 最可能原因速查

| 症状 | 最可能原因 |
| --- | --- |
| 一直走 primary，日志无 switch to the datasource | 注解没被拦截（自调用 / 非 public / 非 Bean / MP 姿势） |
| 同类内方法调用没切换 | `this.` 自调用 |
| 事务方法内切换无效 | 事务已绑定连接 |
| Controller 链式查询没切换 | MyBatis-Plus 姿势 |
| 偶发串库 | push 残留 / 线程复用 |
| 切了但报某表不存在 | key 不在（回落 primary），开 strict 验证 |
| 指定了错误 key 却不报错 | 非 strict 模式静默回落，这是设计行为 |

## 回答此类问题的话术建议

1. 先要三样信息：**版本（SB/dd/mp）+ 注解位置与调用链 + debug 日志**；
2. 对照上面 6 项逐条排除，不要直接建议"改配置试试"；
3. 定位后给出最小修复（自注入 / 调整注释放置 / 换 `@DSTransactional` / 子线程内切），并提醒开发环境开 `strict: true` 防止再次踩坑。

## 相关文档

- Wiki：[@DS 不生效排查总表](https://doc.xiuceyun.cn/docs/faq/not-working)（本文的完整版，带更多 issue 案例）
- Wiki：[@DS 注解位置与 MyBatis-Plus 实践](https://doc.xiuceyun.cn/docs/faq/annotation-placement)
