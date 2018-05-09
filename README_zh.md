# dynamic-datasource-spring-boot-starter
[![Build Status](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter.svg?branch=master)](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.baomidou/dynamic-datasource-spring-boot-starter/badge.svg)](http://mvnrepository.com/artifact/com.baomidou/dynamic-datasource-spring-boot-starter)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

#### [Github](https://github.com/baomidou/dynamic-datasource-spring-boot-starter) | [Gitee](https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter)

## [English introduction](README.md) | [中文说明](README_zh.md)

## 简介

dynamic-datasource-spring-boot-starter 基于 springBoot2.0.

它适用于读写分离，一主多从的环境。

主数据库使用 `INSERT`   `UPDATE`  `DELETE` 操作。

从数据库使用 `SELECT` 操作。

如果你的项目比较复杂，建议使用 [sharding-jdbc ](https://github.com/shardingjdbc/sharding-jdbc)。

[点击加入QQ群【苞米豆开源交流】](https://jq.qq.com/?_wv=1027&k=5tFhLhS)

## 示例

[dynamic-datasource-example](https://gitee.com/baomidou/dynamic-datasource-example) 一个简单能直接运行的项目。

# 使用方法

1. 引入dynamic-datasource-spring-boot-starter。

```xml
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
  <version>${version}</version>
</dependency>
```
2. 配置主从数据源。

spring.datasource.dynamic.master 配置唯一主数据源（写库）

spring.datasource.dynamic.slave 配置每一个从数据源（读库）

```yaml
spring:
  datasource:
    dynamic:
      master:
        username: root
        password: 123456
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://47.100.20.186:3307/dynamic?characterEncoding=utf8&useSSL=false
      slave:
        one:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3308/dynamic?characterEncoding=utf8&useSSL=false
        two:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3309/dynamic?characterEncoding=utf8&useSSL=false
```

3. 切换数据源。

使用 **@DS**  注解切换数据源。

> 可以注解在方法上,可以注解在service实现或mapper接口方法上。

|     注解     |                             结果                             |
| :----------: | :----------------------------------------------------------: |
|   没有@DS    |                             主库                             |
| @DS("slave") |                存在slave指定slave，不存在主库                |
|     @DS      | 根据DynamicDataSourceStrategy策略，选择一个从库。默认负载均衡策略。 |

```java
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @DS("one")
  public List<Map<String, Object>> selectAll() {
    return  jdbcTemplate.queryForList("select * from user");
  }
  
  @Override
  @DS
  public List<Map<String, Object>> selectByCondition() {
    return  jdbcTemplate.queryForList("select * from user where age >10");
  }

}
```
在mybatis环境下也可注解在mapper接口层。

```java
public interface UserMapper {

  @Insert("INSERT INTO user (name,age) values (#{name},#{age})")
  boolean addUser(@Param("name") String name, @Param("age") Integer age);

  @Update("UPDATE user set name=#{name}, age=#{age} where id =#{id}")
  boolean updateUser(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age);

  @Delete("DELETE from user where id =#{id}")
  boolean deleteUser(@Param("id") Integer id);

  @Select("SELECT * FROM user")
  @DS
  List<User> selectAll();

}
```

# 集成druid

springBoot2.x默认使用HikariCP，但在国内Druid的使用者非常庞大，此项目特地对其进行了适配，完成多数据源下使用Druid进行监控。

> 注意：主从可以使用不同的数据库连接池，如master使用Druid监控，从库使用HikariCP。 如果不配置连接池type类型，默认是Druid优先于HikariCP。

1. 项目引入`druid-spring-boot-starter`依赖。

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.9</version>
</dependency>
```

2. 排除原生Druid的快速配置类。

```java
@SpringBootApplication(exclude = DruidDataSourceAutoConfigure.class)
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
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
      master:
        username: root
        password: 123456
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://47.100.20.186:3307/dynamic?characterEncoding=utf8&useSSL=false
        druid:
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

数据源来源的默认实现是YmlDynamicDataSourceProvider，其从yaml或properties中读取信息并解析出主从信息。

场景：有些人想把从库信息配置到主库的某个表中，如有个表名slave_datasource，用户可以自己去实现以下接口并注入。

```java
public interface DynamicDataSourceProvider {

  /**
   * load master
   *
   * @return masterDataSource
   */
  DataSource loadMasterDataSource();

  /**
   * load slaves
   *
   * @return slaveDataSource
   */
  Map<String, DataSource> loadSlaveDataSource();

}
```

2. 自定义从库选择策略。

默认的策略是负载均衡的策略，LoadBalanceDynamicDataSourceStrategy。

内部也提供了一个随机策略，RandomDynamicDataSourceStrategy。

```java
public interface DynamicDataSourceStrategy {

  /**
   * determine a slaveId
   *
   * @param slaveDataSourceLookupKeys slaveKeys
   * @return slaveId
   */
  String determineSlaveDataSource(String[] slaveDataSourceLookupKeys);

}
```

用户可以自己重写策略并注入，这里注入自带的随机策略。

```java
  @Bean
  public DynamicDataSourceStrategy dynamicDataSourceStrategy() {
    return new RandomDynamicDataSourceStrategy();
  }
```
