# v2.5.0

- 修复数据源启动说明错误。
- 修复负载均衡策略切换隐藏bug。
- 底层更换为ArrayDeque。
- 重构动态处理器。

# v2.4.2

- 引入了实验性的功能： 根据正则或spel来自动匹配数据源。

# v2.4.1

- 修复了上个版本hikari不兼容1.5.x的BUG。
- 提供了hikari全局属性的配置。

# v2.4.0
- 重构了druid配置。
- 支持了更多druid参数配置，支持了加密。
- 完善了文档。

# v2.3.6 
- 修复上个版本的druid参数设置bug。
- 对外开放获取所有数据源和所有组数据源方法。

# v2.3.5 druid参数设置有bug
- 支持p6sy。(格式化sql利器)
- 支持jndi数据源。
- 支持druid更多参数。
- 支持hikari参数设置。
- 切换数据源工具类只有在clear的时候才移除当前数据源名称。
- 启动带上数据源名称。
- 添加了更多的测试代码。

# v2.3.4
- 底层细节优化。
- 重构多级数据源切换。
- 示例项目重构。

# v2.3.3 问题版本-不能使用
- 支持嵌套下多级的数据源切换(service1 mysql调用service2 oracle)。

https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter/issues/IO33C

- 修复spel对request和session的支持。

# v2.3.2 问题版本-不能使用
- 修复在不需要session的场景中自动注入session。

# v2.3.1 问题版本-不能使用
- 修复2.3.0中使用spel session 和header的取值错误。

# v.2.3.0

- 重构创建数据源类。废弃DataSourceFactory，改为Bean的DynamicDataSourceCreator。
- 自动适配mybatisPlus，移除参数的mp-enabled。
- 新特性支持spel参数获取数据源。

# v2.2.3

- 支持druid参数全局配置。
- 对外暴露动态添加删除数据源的方法。
- 增加在组内数据源为空时使用默认数据源。
- 去除启动时校验组内只有单个数据源。

# v2.2.2 问题版本-不能使用

- 修复上个版本mp3适配失败的Bug。

# v2.2.1

- 适配mybatis2.x版本。

# v2.2.0

- 修复从默认数据源获取数据不能是组数据源的bug。
- 摒弃spring原生动态数据源抽象，重新实现。
- 去除底层方法缓存。
- 提供从JDBC中获取数据源的抽象。
- 部分代码重新划分包。
- license的组织名更新成为baomidou。

# v2.1.1

- 切面顺序调整为最高。
- 底层切换数据源逻辑优化。

# v2.1.0

- 修复了底层一个逻辑bug。
- 提供了对mp的原生支持。
- 底层代码进行了细微的性能优化。

# v2.0.2

- 修复springboot2.0以上版本不能设置HikariDataSource。
- 底层代码的整理。
- Druid数据源初始大小改为3。

# v2.0.1

- 修复一个方法缓存的bug，会引起同名方法的注解失效。
- 底层代码的重命名和部分格式的调整。

# v2.0.0

- Breaking change：数据源配置同级，不再默认主从，支持多种方案。
- Breaking change：使用约定大于配置，开启 多组模式 新启程。
- Breaking change：注解包路径变更，与苞米豆其他项目保持一致写法。
- Breaking change：不再支持@DS空注解。
- Breaking change：不再支持强制主库force-master配置。
- Breaking change：数据源选择策略现在如需更改需要设置配置文件的dynamicDataSourceStrategyClass。
- Druid数据源默认validation-query 为select 1 。
- 全部源码改为中文。
- 增加了部分启动时和运行中的日志。

# v1.4.0

- 支持了在类上注解，如果方法上同时有注解则方法注解优先。
- 支持了遇到事物强制主库，并且是默认行为，可在配置更改foeceMaster。
- 最低支持jdk1.7，springboot1.4.x。
- 重构aop，解决了部分springboot版本引入插件无效的问题。

# v1.3.0

- 对Druid的paCache属性提供支持。
- 还原上一版本切面的配置方式。
- 其他一些细节的优化。

# v1.2.0

- 对Druid提供更完善的支持。
- 更改了默认的注解切面的注入方式。
- 抽象了切面选择数据源接口，方便以后支持spel语法等。

# v1.1.0

- 支持Druid数据源 (support DruidDataSource)。
