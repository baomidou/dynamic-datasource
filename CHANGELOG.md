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
