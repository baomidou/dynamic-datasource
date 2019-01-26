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

网上关于动态数据源的切换的文档有很多，核心只有两种。

1. 构建多套环境，优势是方便控制也容易集成一些简单的分布式事务，缺点是非动态同时代码量较多,配置难度大。
2. 基于spring提供原生的 `AbstractRoutingDataSource` ，参考一些文档自己实现切换。

如果你的数据源较少，场景不复杂，选择以上任意一种都可以。如果你需要更多特性，请尝试本动态数据源。

1. 数据源分组，适用于多种场景 纯粹多库  读写分离  一主多从  混合模式。
2. 简单集成Druid数据源监控多数据源，简单集成Mybatis-Plus简化单表，简单集成P6sy格式化sql，简单集成Jndi数据源。
3. 简化Druid和HikariCp配置，提供全局参数配置。
4. 提供自定义数据源来源(默认使用yml或properties配置)。
5. 项目启动后能动态增减数据源。
6. 使用spel动态参数解析数据源，如从session，header和参数中获取数据源。（多租户架构神器）
7. 多层数据源嵌套切换。（一个业务ServiceA调用ServiceB，ServiceB调用ServiceC，每个Service都是不同的数据源）
8. 使用正则匹配或spel表达式来切换数据源（实验性功能）。

# 劣势

不能使用多数据源事务（同一个数据源下能使用事务），网上其他方案也都不能提供。 

如果你需要使用到分布式事务，那么你的架构应该到了微服务化的时候了。

如果呼声强烈，项目达到800 star，作者考虑集成多数据源事务。(单机架构下多数据源事物)

PS: 如果您只是几个数据库但是有强烈的需求分布式事务，建议还是使用传统方式自己构建多套环境集成atomic这类，网上百度很多。

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
# 多主多从                      纯粹多库（记得设置primary）                   混合配置
spring:                               spring:                               spring:
  datasource:                           datasource:                           datasource:
    dynamic:                              dynamic:                              dynamic:
      datasource:                           datasource:                           datasource:
        master_1:                             mysql:                                master:
        master_2:                             oracle:                               slave_1:
        slave_1:                              sqlserver:                            slave_2:
        slave_2:                              postgresql:                           oracle_1:
        slave_3:                              h2:                                   oracle_2:
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
在mybatis环境下也可注解在mapper接口层。(若非必要强烈不建议在mapper上切换，不符合抽象原则。应由service来切换，在mapper上切换遇到的问题一律拒绝)

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


####                                                                                       赶紧集成体验一下吧！ 如果需要更多功能请继续往下看！

---



- #### Druid集成，MybatisPlus集成，动态增减数据源等等更多更细致的文档在这里     [点击查看](https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter/wikis/pages)

- #### 项目Javadoc一览                  [点击查看](https://apidoc.gitee.com/baomidou/dynamic-datasource-spring-boot-starter/)