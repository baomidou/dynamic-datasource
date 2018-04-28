# dynamic-datasource-spring-boot-starter
[![Build Status](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter.svg?branch=master)](https://www.travis-ci.org/baomidou/dynamic-datasource-spring-boot-starter)
[![Coverage Status](https://coveralls.io/repos/github/jsonzou/jmockdata/badge.svg)](https://coveralls.io/github/jsonzou/jmockdata)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.github.jsonzou/jmockdata/badge.svg)](http://mvnrepository.com/artifact/com.github.jsonzou/jmockdata)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

dynamic-datasource-spring-boot-starter是一个基于aop切换的简单多数据快速启动器。

示例项目 [dynamic-datasource-example](https://gitee.com/baomidou/dynamic-datasource-example)

# 使用方法

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
        url: jdbc:mysql://47.100.20.186:3307/springboot?characterEncoding=utf8&useSSL=false
      slaves:
        one:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3308/springboot?characterEncoding=utf8&useSSL=false
        two:
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://47.100.20.186:3309/springboot?characterEncoding=utf8&useSSL=false
```

3. 在实现方法上加 @DS 注解完成数据源的切换。

@DS 只可以注解在方法上,可以注解在service实现或mapper接口方法上.
未指定@DS或@DS中的数据库ID未找到都将走主库。
指定了@DS但未设置数据库ID讲按策略选择从数据库，默认的策略是负载均衡策略。

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