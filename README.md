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
<p align="center">
	QQ群:<a href="https://jq.qq.com/?_wv=1027&k=5tFhLhS" target="_blank">710314529</a>
</p>

#### [Github](https://github.com/baomidou/dynamic-datasource-spring-boot-starter) | [Gitee](https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter)

# 简介

dynamic-datasource-spring-boot-starter 是一个基于springboot的快速集成多数据源的启动器。

其支持 **Jdk 1.7+,    SpringBoot 1.4.x  1.5.x   2.0.x**。

从 **2.0.0** 开始它适用于多种场景，常见的场景如下。

- 纯粹多库,各个库甚至可以是不同的数据库。
- 读写分离，一主多从，多主多从。
- 混合模式，既有主从也有单库。

本框架只做 **切换数据源** 这件核心的事情，并不限制您的具体操作。

强烈建议在 **主从模式** 下遵循普遍的规则，以便他人能更轻易理解您的代码。

主数据库  **建议**   只执行 `INSERT`   `UPDATE`  `DELETE` 操作。

从数据库  **建议**   只执行 `SELECT` 操作。

另外如果您的项目比较复杂，强烈建议您使用 [sharding-jdbc ](https://github.com/shardingjdbc/sharding-jdbc)。

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

从 **2.0.0** 开始所有数据源的 **配置同级** ，不再有默认的主从限制，您可以给您的数据源起任何合适的名字。

**约定大于配置**：

* 所有以下划线 `_` 分割的数据源 **首部** 即为组的名称，相同组名称的数据源会放在一个组下。
* 默认的数据源名称为  **master** ，您可以通过spring.datasource.dynamic.primary来修改。

下面给出最常见的几种配置方案：

* 一主多从方案：

```yaml
spring:
  datasource:
    dynamic:
      primary: master #设置默认的数据源或者数据源组,默认值即为master,如果你主从默认下主库的名称就是master可不定义此项。
      datasource:
        master:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3306/dynamic?characterEncoding=utf8&useSSL=false
        slave_1:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3307/dynamic?characterEncoding=utf8&useSSL=false
        slave_2:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3308/dynamic?characterEncoding=utf8&useSSL=false
       #......省略
       #以上会配置一个默认库master，一个组slave下有两个子库slave_1,slave_2
```

* 多主多从方案：(谨慎使用，您清楚的知道多个主库间需要自己做同步)

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master_1:
        master_2:
        slave_1:
        slave_2:
        slave_3:
```

* 纯粹多库方案：

```yaml
spring:
  datasource:
    dynamic:
      primary: mysql #记得设置一个默认数据源
      datasource:
        mysql:
        oracle:
        sqlserver: 
        h2:
```

* 混合方案：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
        slave_1:
        slave_2: 
        oracle_1:
        oracle_2:
        sqlserver:
```

3. 使用  **@DS**  切换数据源。

**@DS** 可以注解在方法上和类上，**同时存在方法注解优先于类上注解**,强烈建议注解在service实现或mapper接口方法上。

注意从2.0.0 **不再支持@DS空注解** ，您 **必须** 指明你所需要的数据库 **组名** 或者 **具体某个数据库名称** 。

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

# 集成druid

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
      datasource:
        master:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3306/dynamic?characterEncoding=utf8&useSSL=false
          druid: #以下均为默认值
            initial-size: 3
            max-active: 8
            min-idle: 2
            max-wait: -1
            min-evictable-idle-time-millis: 30000
            max-evictable-idle-time-millis: 30000
            time-between-eviction-runs-millis: 0
            validation-query: select 1
            validation-query-timeout: -1
            test-on-borrow: false
            test-on-return: false
            test-while-idle: true
            pool-prepared-statements: true
            max-open-prepared-statements: 100
            filters: stat,wall
            share-prepared-statements: true
```

如上即可配置访问用户和密码，访问 http://localhost:8080/druid/index.html 查看druid监控。



# 自定义

1. 自定义数据源来源。

数据源来源的默认实现是YmlDynamicDataSourceProvider，其从yaml或properties中读取信息并解析出所有数据源信息。

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

# 苞米豆开源项目

<https://gitee.com/baomidou/mybatis-plus> mybatis单表快速开发，3.0已支持lamdba写法 。

https://gitee.com/baomidou/kisso 基于cookie的SSO中间件。

<https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter> 多数据源，动态数据源，主从分离 快速启动器。

<https://gitee.com/baomidou/kaptcha-spring-boot-starter> 谷歌验证码 快速启动器。

[https://gitee.com/baomidou/lock4j](https://gitee.com/baomidou/redisplus)[-spring-boot-starter](https://gitee.com/baomidou/kaptcha-spring-boot-starter) 支持RedisTemplate、Redisson、Zookeeper 简单可靠的分布式锁 快速启动器。