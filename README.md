dynamic-datasource-spring-boot-starter是一个基于aop切换的简单多数据快速启动器。

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

3. 在实现方法上加 @DynamicDataSource 注解完成数据源的切换。

```java
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  @DynamicDataSource("one")
  public List<Map<String, Object>> selectAll() {
    return  jdbcTemplate.queryForList("select * from user");
  }

}
```