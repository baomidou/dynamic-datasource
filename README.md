<p align="center">

<img src="https://s1.ax1x.com/2018/07/31/PwPVAA.png" border="0" />

</p>

<p align="center">
	<strong>一个基于springboot的快速集成多数据源的启动器</strong>
</p>

<p align="center">
    <a href="https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter" target="_blank">
        <img src="https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter.svg?branch=master" >
    <a href="http://mvnrepository.com/artifact/com.baomidou/dynamic-datasource-spring-boot-starter" target="_blank">
        <img src="https://maven-badges.herokuapp.com/maven-central/com.baomidou/dynamic-datasource-spring-boot-starter/badge.svg" >
    </a>
    <a href="http://www.apache.org/licenses/LICENSE-2.0.html" target="_blank">
        <img src="http://img.shields.io/:license-apache-brightgreen.svg" >
    </a>
    <a>
        <img src="https://img.shields.io/badge/JDK-1.7+-green.svg" >
    </a>
    <a>
        <img src="https://img.shields.io/badge/springBoot-1.4+_1.5+_2.0+-green.svg" >
    </a>
</p>

#### [Github](https://github.com/baomidou/dynamic-datasource-spring-boot-starter) | [码云Gitee](https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter)

# 简介

dynamic-datasource-spring-boot-starter 是一个基于springboot的快速集成多数据源的启动器。

其支持 **Jdk 1.7+,    SpringBoot 1.4.x  1.5.x   2.0.x**。最新版为<img src="https://maven-badges.herokuapp.com/maven-central/com.baomidou/dynamic-datasource-spring-boot-starter/badge.svg" >

**示例项目** 可参考项目下的samples目录。

**示例项目** 可参考项目下的samples目录。

**示例项目** 可参考项目下的samples目录。

# 优势

网上关于动态数据源的切换的文档有很多，核心只有两种。1是构建多套环境，2是基于spring原生的 `AbstractRoutingDataSource` 切换。  

如果你的数据源较少，场景不复杂，选择以上任意一种都可以。如果你需要更多特性，请试着尝试本数据源。

1. 数据源分组，适用于多种场景 纯粹多库  读写分离  一主多从  混合模式。
2. 简单集成Druid数据源监控多数据源，简单集成Mybatis-Plus简化单表，简单集成P6sy格式化sql，简单集成Jndi数据源。
3. 自定义数据源来源。
4. 动态增减数据源。
5. 使用spel动态参数解析数据源，如从session，header和参数中获取数据源。（多租户架构神器）
6. 多层数据源嵌套切换。（一个业务ServiceA调用ServiceB，ServiceB调用ServiceC，每个Service都是不同的数据源）

# 劣势

不能使用多数据源事物（同一个数据源下能使用事物），网上其他方案也都不能提供。 

如果你需要使用到分布式事物，那么你的架构应该到了微服务化的时候了。

如果呼声强烈，项目达到800 star，作者考虑集成分布式事物。

PS: 如果您只是几个数据库但是有强烈的需求分布式事物，建议还是使用传统方式自己构建多套环境集成atomic这类，网上百度很多。

# 约定

1. 本框架只做 **切换数据源** 这件核心的事情，并**不限制你的具体操作**，切换了数据源可以做任何CRUD。
2. 配置文件所有以下划线 `_` 分割的数据源 **首部** 即为组的名称，相同组名称的数据源会放在一个组下。
3. 切换数据源即可是组名，也可是具体数据源名称，切换时默认采用负载均衡机制切换。
4. 默认的数据源名称为  **master** ，你可以通过spring.datasource.dynamic.primary修改。
5. 方法上的注解优先于类上注解。

# 建议

强烈建议在 **主从模式** 下遵循普遍的规则，以便他人能更轻易理解你的代码。

主数据库  **建议**   只执行 `INSERT`   `UPDATE`  `DELETE` 操作。

从数据库  **建议**   只执行 `SELECT` 操作。

# 使用方法

1. 引入dynamic-datasource-spring-boot-starter。

```xml
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
  <version>${version}</version>
</dependency>
```
2. 配置数据源。

```yaml
spring:
  datasource:
    dynamic:
      primary: master #设置默认的数据源或者数据源组,默认值即为master
      datasource:
        master:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://xx.xx.xx.xx:3306/dynamic
        slave_1:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://xx.xx.xx.xx:3307/dynamic
        slave_2:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://xx.xx.xx.xx:3308/dynamic
       #......省略
       #以上会配置一个默认库master，一个组slave下有两个子库slave_1,slave_2
```

```yaml
# 多主多从                      纯粹多库（记得设置primary）         混合配置
spring:                        spring:                           spring:
  datasource:                    datasource:                       datasource:
    dynamic:                       dynamic:                          dynamic:
      datasource:                    datasource:                       datasource:
        master_1:                       mysql:                           master:  
        master_2:                       oracle:                          slave_1:
        slave_1:                        sqlserver:                       slave_2:
        slave_2:                        postgresql:                      oracle_1:
        slave_3:                        h2:                              oracle_2:
```

3. 使用  **@DS**  切换数据源。

**@DS** 可以注解在方法上和类上，**同时存在方法注解优先于类上注解**。

注解在service实现或mapper接口方法上，但强烈不建议同时在service和mapper注解。 (可能会有问题)

|     注解      |                   结果                   |
| :-----------: | :--------------------------------------: |
|    没有@DS    |                默认数据源                |
| @DS("dsName") | dsName可以为组名也可以为具体某个库的名称 |

```java
@Service
@DS("slave")
public class UserServiceImpl implements UserService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<Map<String, Object>> selectAll() {
    return  jdbcTemplate.queryForList("select * from user");
  }
  
  @Override
  @DS("slave_1")
  public List<Map<String, Object>> selectByCondition() {
    return  jdbcTemplate.queryForList("select * from user where age >10");
  }
}
```
在mybatis环境下也可注解在mapper接口层。

```java
@DS("slave")
public interface UserMapper {

  @Insert("INSERT INTO user (name,age) values (#{name},#{age})")
  boolean addUser(@Param("name") String name, @Param("age") Integer age);

  @Update("UPDATE user set name=#{name}, age=#{age} where id =#{id}")
  boolean updateUser(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age);

  @Delete("DELETE from user where id =#{id}")
  boolean deleteUser(@Param("id") Integer id);

  @Select("SELECT * FROM user")
  @DS("slave_1")
  List<User> selectAll();
}
```

---

大部分用户看到这里就可以了，赶紧体验一下吧！  如果需要更多功能请继续往下看。

---

# 第三方集成

## 集成 Druid

springBoot2.x默认使用HikariCP，但在国内Druid的使用者非常庞大，此项目特地对其进行了适配，完成多数据源下使用Druid进行监控。

> 注意：主从可以使用不同的数据库连接池，如**master使用Druid监控，从库使用HikariCP**。 如果不配置连接池type类型，默认是Druid优先于HikariCP。

1. 项目引入`druid-spring-boot-starter`依赖。

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.10</version>
</dependency>
```

2. **排除**  原生Druid的快速配置类。

```java
@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
```

```yaml
#如果遇到DruidDataSourceAutoConfigure抛出no suitable driver表示注解排除没有生效尝试以下这种排除方法
spring:
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
```

> 为什么要排除DruidDataSourceAutoConfigure ？   
>
> DruidDataSourceAutoConfigure会注入一个DataSourceWrapper，其会在原生的spring.datasource下找url,username,password等。而我们动态数据源的配置路径是变化的。

3. 其他属性依旧如原生`druid-spring-boot-starter`的配置。

```yaml
spring:
  datasource:
    druid:
      stat-view-servlet:
        loginUsername: admin
        loginPassword: 123456
    dynamic:
      druid: # 全局druid参数，绝大部分值和默认保持一致。(现已支持的参数如下,不清楚含义不要乱设置)
        initial-size:
        max-active:
        min-idle:
        max-wait:
        time-between-eviction-runs-millis:
        time-between-log-stats-millis:
        stat-sqlmax-size:
        min-evictable-idle-time-millis:
        max-evictable-idle-time-millis:
        test-while-idle:
        test-on-borrow:
        test-on-return:
        validation-query:
        validation-query-timeout:
        use-global-datasource-stat:
        async-init:
        clear-filters-enable:
        reset-stat-enable:
        not-full-timeout-retry-count:
        max-wait-thread-count:
        fail-fast:
        phyTimeout-millis:
        keep-alive:
        pool-prepared-statements:
        init-variants:
        init-global-variants:
        use-unfair-lock:
        kill-when-socket-read-timeout:
        connection-properties:
        max-pool-prepared-statement-per-connection-size:
        init-connection-sqls:
        share-prepared-statements:
        connection-errorretry-attempts:
        break-after-acquire-failure:
        filters: stat,wall # 注意这个值和druid原生不一致，默认启动了stat,wall
      datasource:
        master:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3306/dynamic?characterEncoding=utf8&useSSL=false
          druid: # 以下参数针对每个库可以重新设置druid参数
            initial-size:
            validation-query: select 1 FROM DUAL #比如oracle就需要重新设置这个
            public-key: #（非全局参数）设置即表示启用加密,底层会自动帮你配置相关的连接参数和filter。
#           ......


# 生成 publickey 和密码 
# java -cp druid-1.1.10.jar com.alibaba.druid.filter.config.ConfigTools youpassword
```

如上即可配置访问用户和密码，访问 http://localhost:8080/druid/index.html 查看druid监控。

## 集成 MybatisPlus

只要进入mybatisPlus相关jar包，项目自动集成。 兼容mybatisPlus 2.x和3.x的版本。

只要注解在mybatisPlus的mapper或serviceImpl上即可完成mp内置方法切换。

```java
// 注解在类上统一切换数据源，如果想某个方法特殊处理，请自己用一个方法包裹然后注解在该方法上。
@DS("slave")
public interface UserMapper extends BaseMapper<User> {
}

@Service
@DS("slave")//再次建议不要同时注解在service和mapper上。
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
```

## 集成 P6sy

p6sy大部分人最常用的功能就是格式化你的sql语句。

```mysql
# 如在使用mybatis的过程中，原生输出的语句是带?号的。在需要复制到其他地方执行看效果的时候很不方便。
select * from user where age>?
# 在使用了p6sy后，其会帮你格式化成真正的执行语句。
select * from user where age>6
```

1. 引入相关jar包。

```xml
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.8.0</version>
</dependency>
```

2. 启用配置。

```yaml
spring:
  datasource:
    dynamic:
      p6sy: true # 默认false,建议线上关闭。
```

3. 引入相关配置文件。

在classPath下创建spy.properties

```properties
# 一个最简单配置,定义slf4j日志输出。 更多参数请自行了解。
appender=com.p6spy.engine.spy.appender.Slf4JLogger
```

## 集成 Jndi数据源

在某些场景我们的数据源来源于jndi，开启使用十分方便。

```yaml
spring:
  datasource:
    dynamic:
      master:
        jndi_name: xxx #只要配置即表示启用。
```

# 高级

## 自定义数据源来源

数据源来源的默认实现是`YmlDynamicDataSourceProvider`，其从yaml或properties中读取信息并解析出所有数据源信息。

```java
public interface DynamicDataSourceProvider {

    /**
     * 加载所有数据源
     *
     * @return 所有数据源，key为数据源名称
     */
    Map<String, DataSource> loadDataSources();

}
```

可以参考  `AbstractJdbcDataSourceProvider`  （仅供参考，不能直接使用）来实现从JDBC数据库中获取数据库连接信息。

## 动态增减数据源

```java
public class DemoController {

    @Autowired //在需要的地方注入，然后调用相关方法增减数据源
    private DynamicRoutingDataSource dynamicRoutingDataSource;
    
   //public synchronized void addDataSource(String ds, DataSource dataSource)
    
   //public synchronized void removeDataSource(String ds)
    
   // 另外这个类也有相关方法获取所有数据源的信息
}
```

## 解析自己的数据源

知道了如何动态增减数据源，但不知道如何解析自己数据源，来动态创建一个DataSource。

`com.baomidou.dynamic.datasource.DynamicDataSourceCreator`   是一个数据源创建BEAN。其对spring1.x和2.x做了一些适配。

熟悉spring的朋友也可以直接调用原生的DataSourceBuilder来创建简单数据源。

```java
//核心源码简述
public class DynamicDataSourceCreator {

//最外层创建数据源方法，一般直接调用这个就可以
public DataSource createDataSource(DataSourceProperty dataSourceProperty)
  
//项目只对druid和hikari做了特殊处理，支持一些参数和配置，其他的类型只能调用这个
public DataSource createBasicDataSource(DataSourceProperty dataSourceProperty)

//创建jndi数据源，只要配置参数设置了jndiName就会创建，优先级最高
public DataSource createJNDIDataSource(String jndiName)

//创建druid数据源,如果未指定type,druid的优先于hikari
public DataSource createDruidDataSource(DataSourceProperty dataSourceProperty)

//创建hikari数据源
public DataSource createHikariDataSource(DataSourceProperty dataSourceProperty)
    
}
```

## 动态参数解析数据源(spel)

所有以  `#`   开头的参数都会使用spel从参数中获取数据源。

```java
@DS("#session.tenantName")//从session获取
public List selectSpelBySession() {
	return userMapper.selectUsers();
}

@DS("#header.tenantName")//从header获取
public List selectSpelByHeader() {
	return userMapper.selectUsers();
}

@DS("#tenantName")//使用spel从参数获取
public List selectSpelByKey(String tenantName) {
	return userMapper.selectUsers();
}

@DS("#user.tenantName")//使用spel从复杂参数获取
public List selecSpelByTenant(User user) {
	return userMapper.selectUsers();
}
```
如果你还想对spel解析的参数进行进一步处理，请注入`DynamicDataSourceSpelResolver`。

默认的`DefaultDynamicDataSourceSpelResolver` 没有对解析到的参数进行处理直接返回。

# 苞米豆开源项目

<https://gitee.com/baomidou/mybatis-plus> mybatis单表快速开发，3.0已支持lamdba写法 。

<https://gitee.com/baomidou/kisso> 基于cookie的SSO中间件。

<https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter> 多数据源，动态数据源，主从分离 快速启动器。

<https://gitee.com/baomidou/kaptcha-spring-boot-starter> 谷歌验证码 快速启动器。

<https://gitee.com/baomidou/lock4j-spring-boot-starter> 支持RedisTemplate、Redisson、Zookeeper 简单可靠的分布式锁 快速启动器。

# 常见问题

1. 多个库的事物如何处理？

**不能 不能 不能**，一个业务操作涉及多个库不要加事物。

2. 是否支持JPA？

不完全支持，受限于JPA底层，你只能在一个controller下切换第一个库，第二个库不能切换。（如有解决办法请联系作者）
