# dynamic-datasource-spring-boot-starter
[![Build Status](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter.svg?branch=master)](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.baomidou/dynamic-datasource-spring-boot-starter/badge.svg)](http://mvnrepository.com/artifact/com.baomidou/dynamic-datasource-spring-boot-starter)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

#### [Github](https://github.com/baomidou/dynamic-datasource-spring-boot-starter) | [Gitee](https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter)

## [English introduction](README.md) | [中文说明](README_zh.md)

## Introduction

dynamic-datasource-spring-boot-starter based on springBoot2.0.

It can be used for Separation of reading and writing.

The master database to `INSERT`   `UPDATE`  `DELETE` .

The slave database to `SELECT` .

If you project is complex,I will propose you use [sharding-jdbc ](https://github.com/shardingjdbc/sharding-jdbc).

## Demo 

[dynamic-datasource-example](https://gitee.com/baomidou/dynamic-datasource-example) a simple dynamic project that u can direct run。

## How to use

1. Import dynamic-datasource-spring-boot-starter。

```xml
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
  <version>${version}</version>
</dependency>
```
2. Config your master-slave info in application.yml。

spring.datasource.dynamic.master （config the unique master database）

spring.datasource.dynamic.slave （config every slave database）

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

3. Use annotation  `@DS`   to switch database.

>  `@DS`   can be annotation on serviceImpl method or mybatis mapper method.

| annotation  |                            result                            |
| :---------: | :----------------------------------------------------------: |
| without @DS |                            master                            |
| @DS("one")  | If exist a database named 'one' then switch to one,otherwise to master |
|     @DS     | depensOn DynamicDataSourceStrategy to determine a slave database,the default strategy is LoadBalance. |

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
In mybatis environment it can also annotation on mapper method.

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

## Custom config

1. Config DynamicDataSourceProvider .

The default DynamicDataSourceProvider is `YmlDynamicDataSourceProvider`，It read database info from `application.yaml` or `application.properties` .

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

2. Config DynamicDataSourceStrategy.

The default DynamicDataSourceStrategy is `LoadBalanceDynamicDataSourceStrategy` . 

The starter alse provide `RandomDynamicDataSourceStrategy`.

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

Demo to use `RandomDynamicDataSourceStrategy` instead of `LoadBalanceDynamicDataSourceStrategy` .

```java
@Configuration
public class DynamicConfiguration {
 
  @Bean
  public DynamicDataSourceStrategy dynamicDataSourceStrategy() {
    return new RandomDynamicDataSourceStrategy();
  }

}
```

