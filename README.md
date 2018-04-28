# dynamic-datasource-spring-boot-starter
[![Build Status](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter.svg?branch=master)](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter)
[![Coverage Status](https://coveralls.io/repos/github/jsonzou/jmockdata/badge.svg)](https://coveralls.io/github/jsonzou/jmockdata)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.jsonzou/jmockdata/badge.svg)](http://mvnrepository.com/artifact/com.github.jsonzou/jmockdata)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

dynamic-datasource-spring-boot-starter是一个基于aop切换的简单多数据快速启动器。

示例项目 [dynamic-datasource-example](https://gitee.com/baomidou/dynamic-datasource-example)

# 使用方法

1. 引入dynamic-datasource-spring-boot-starter。

```xml
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```
2. 配置主从数据源。

spring.datasource.dynamic.master 配置唯一主数据源（写库）

spring.datasource.dynamic.slaves 配置每一个从数据源（读库）

```yaml
spring:
  datasource:
    dynamic:
      master:
        username: root
        password: 123456
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://47.100.20.186:3307/dynamic?characterEncoding=utf8&useSSL=false
      slaves:
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

# 自定义

1. 自定义数据源来源。

数据源来源的默认实现是YmlDynamicDataSourceProvider，其从yaml或properties中读取信息并解析出主从信息。

场景：有些人想把从库信息配置到主库的某个表中，如有个表名slave_datasource。现在需要用户自己去实现以下接口并注入。

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

默认的策略是负载均衡的策略，LoadBalanceDynamicDataSourceStrategy。 也提供了一个随机策略，RandomDynamicDataSourceStrategy。

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

重写策略并注入。

```java
  @Bean
  public DynamicDataSourceStrategy dynamicDataSourceStrategy() {
    return new RandomDynamicDataSourceStrategy();
  }
```

