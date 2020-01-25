# 简介

此工程为多数据源集成druid+seata+mybatisPlus的版本。

seata 官方Github地址: https://github.com/seata/seata

seata 官方Github文档: https://seata.io/zh-cn/docs/overview/what-is-seata.html

seata 官方Github示例地址: https://github.com/seata/seata-samples

题外话：一般需要分布式事务的场景大多数都是微服务化，个人并不建议在单体项目引入多数据源+分布式事务，有能力尽早拆开，可为过度方案。

# 案例

## 介绍

此工程示例为模拟用户下单，扣商品库存，扣用户余额，初步可分为订单服务+商品服务+用户服务。

参考复制seata-samples下的相关工程  感谢HelloWoodes。

## 环境准备

为了快速演示相关环境都采用docker部署，生产上线请参考seata官方文档使用。

1. 准备seata-server。

```shell
docker run --name seata-server -p 8091:8091 -d seataio/seata-server
```

2. 准备mysql数据库，账户root密码123456。

```shell
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7
```

3. 创建相关数据库。

创建    `seata-order`  `seata-product`  `seata-account`  模拟连接不同的数据库。

```mysql
CREATE DATABASE IF NOT EXIST seata-order;
CREATE DATABASE IF NOT EXIST seata-product;
CREATE DATABASE IF NOT EXIST seata-account;
```

4. 准备相关数据库脚本。

每个数据库下简历相关的表，seata需要undo_log来监测和回滚。

相关的脚本不用自行准备，本工程已在resources/db下面准备好，另外配合多数据源的自动执行脚本功能，应用启动后会自动执行。

## 工程准备

1. 搭建一个基础的springboot maven项目。

https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter/tree/master/samples 

此工程下的`dynamic-seata-sample` 已所有都配置完毕，在上面的环境准备中如已都配置完毕即可直接执行测试。

2. 引入相关依赖，seata+druid+mybatisPlus+dynamic-datasource+mysql+lombok。

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-alibaba-seata</artifactId>
      <exclusions>
        <exclusion>
          <groupId>io.seata</groupId>
          <artifactId>seata-all</artifactId>
        </exclusion>
      </exclusions>
      <version>2.1.1.RELEASE</version>
    </dependency>
    <dependency>
      <groupId>io.seata</groupId>
      <artifactId>seata-all</artifactId>
      <version>1.0.0</version>
    </dependency>
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid-spring-boot-starter</artifactId>
    </dependency>
   <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-boot-starter</artifactId>
      <version>3.3.0</version>
    </dependency>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
      <version>2.5.8</version>
    </dependency>
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>provided</scope>
    </dependency>
```

3.  编写相关yaml配置。

```yaml
spring:
  application:
    name: dynamic
  datasource:
    dynamic:
      primary: order #主库设置成order库
      strict: true   #开启严格模式  非必须
      seata: true    #手动开启seata
      datasource:    #每个数据源连接自己对应的数据库
        order:
          username: root
          password: 123456
          url: jdbc:mysql://localhost:3306/seata_order?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
          driver-class-name: com.mysql.cj.jdbc.Driver
          schema: db/schema-order.sql #自动执行对应的数据库sql建表
        account:
          username: root
          password: 123456
          url: jdbc:mysql://localhost:3306/seata_account?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
          driver-class-name: com.mysql.cj.jdbc.Driver
          schema: db/schema-account.sql
        product:
          username: root
          password: 123456
          url: jdbc:mysql://localhost:3306/seata_product?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
          driver-class-name: com.mysql.cj.jdbc.Driver
          schema: db/schema-product.sql

  cloud:
    alibaba:
      seata:
        tx-service-group: my_test_tx_group  #必须，和file.conf里配置对应上,详情自行了解seata。

```

4. 配置seata启动需要的registry.conf和file.conf。

相关概念请自行去seata官网学习,大致就是seata需要的注册和配置。

这两个配置文件都放在resources下面。

**registry.conf**

```shell
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "file"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  eureka {
    serviceUrl = "http://localhost:1001/eureka"
    application = "default"
    weight = "1"
  }
  redis {
    serverAddr = "localhost:6379"
    db = "0"
  }
  zk {
    cluster = "default"
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  consul {
    cluster = "default"
    serverAddr = "127.0.0.1:8500"
  }
  etcd3 {
    cluster = "default"
    serverAddr = "http://localhost:2379"
  }
  sofa {
    serverAddr = "127.0.0.1:9603"
    application = "default"
    region = "DEFAULT_ZONE"
    datacenter = "DefaultDataCenter"
    cluster = "default"
    group = "SEATA_GROUP"
    addressWaitTime = "3000"
  }
  file {
    name = "file.conf"
  }
}

config {
  # file、nacos 、apollo、zk
  type = "file"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  apollo {
    app.id = "seata-server"
    apollo.meta = "http://192.168.1.204:8801"
  }
  zk {
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  file {
    name = "file.conf"
  }
}
```

**file.conf**

```shell
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  # the client batch send request enable
  enable-client-batch-send-request = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}
service {
  #transaction service group mapping
  vgroup_mapping.my_test_tx_group = "default"
  #only support when registry.type=file, please don't set multiple addresses
  default.grouplist = "127.0.0.1:8091"
  #degrade, current not support
  enableDegrade = false
  #disable seata
  disableGlobalTransaction = false
}

client {
  rm {
    async.commit.buffer.limit = 10000
    lock {
      retry.internal = 10
      retry.times = 30
      retry.policy.branch-rollback-on-conflict = true
    }
    report.retry.count = 5
    table.meta.check.enable = false
    report.success.enable = true
  }
  tm {
    commit.retry.count = 5
    rollback.retry.count = 5
  }
  undo {
    data.validation = true
    log.serialization = "jackson"
    log.table = "undo_log"
  }
  log {
    exceptionRate = 100
  }
  support {
    # auto proxy the DataSource bean
    spring.datasource.autoproxy = false
  }
}
```

## 代码编写

参考工程下面的代码完成controller,service,dao,entity,dto等。

订单服务

```java
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

  @Resource
  private OrderDao orderDao;
  @Autowired
  private AccountService accountService;
  @Autowired
  private ProductService productService;
  
  @DS("order")//每一层都需要使用多数据源注解切换所选择的数据库
  @Override
  @Transactional
  @GlobalTransactional //重点 第一个开启事务的需要添加seata全局事务注解
  public void placeOrder(PlaceOrderRequest request) {
    log.info("=============ORDER START=================");
    Long userId = request.getUserId();
    Long productId = request.getProductId();
    Integer amount = request.getAmount();
    log.info("收到下单请求,用户:{}, 商品:{},数量:{}", userId, productId, amount);

    log.info("当前 XID: {}", RootContext.getXID());

    Order order = Order.builder()
        .userId(userId)
        .productId(productId)
        .status(OrderStatus.INIT)
        .amount(amount)
        .build();

    orderDao.insert(order);
    log.info("订单一阶段生成，等待扣库存付款中");
    // 扣减库存并计算总价
    Double totalPrice = productService.reduceStock(productId, amount);
    // 扣减余额
    accountService.reduceBalance(userId, totalPrice);

    order.setStatus(OrderStatus.SUCCESS);
    order.setTotalPrice(totalPrice);
    orderDao.updateById(order);
    log.info("订单已成功下单");
    log.info("=============ORDER END=================");
  }
}
```

商品服务

```java
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

  @Resource
  private ProductDao productDao;

  /**
   * 事务传播特性设置为 REQUIRES_NEW 开启新的事务  重要！！！！一定要使用REQUIRES_NEW
   */
  @DS("product")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public Double reduceStock(Long productId, Integer amount) {
    log.info("=============PRODUCT START=================");
    log.info("当前 XID: {}", RootContext.getXID());

    // 检查库存
    Product product = productDao.selectById(productId);
    Integer stock = product.getStock();
    log.info("商品编号为 {} 的库存为{},订单商品数量为{}", productId, stock, amount);

    if (stock < amount) {
      log.warn("商品编号为{} 库存不足，当前库存:{}", productId, stock);
      throw new RuntimeException("库存不足");
    }
    log.info("开始扣减商品编号为 {} 库存,单价商品价格为{}", productId, product.getPrice());
    // 扣减库存
    int currentStock = stock - amount;
    product.setStock(currentStock);
    productDao.updateById(product);
    double totalPrice = product.getPrice() * amount;
    log.info("扣减商品编号为 {} 库存成功,扣减后库存为{}, {} 件商品总价为 {} ", productId, currentStock, amount, totalPrice);
    log.info("=============PRODUCT END=================");
    return totalPrice;
  }
}
```

用户服务

```java
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

  @Resource
  private AccountDao accountDao;

  /**
   * 事务传播特性设置为 REQUIRES_NEW 开启新的事务    重要！！！！一定要使用REQUIRES_NEW
   */
  @DS("account")
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reduceBalance(Long userId, Double price) {
    log.info("=============ACCOUNT START=================");
    log.info("当前 XID: {}", RootContext.getXID());

    Account account = accountDao.selectById(userId);
    Double balance = account.getBalance();
    log.info("下单用户{}余额为 {},商品总价为{}", userId, balance, price);

    if (balance < price) {
      log.warn("用户 {} 余额不足，当前余额:{}", userId, balance);
      throw new RuntimeException("余额不足");
    }
    log.info("开始扣减用户 {} 余额", userId);
    double currentBalance = account.getBalance() - price;
    account.setBalance(currentBalance);
    accountDao.updateById(account);
    log.info("扣减用户 {} 余额成功,扣减后用户账户余额为{}", userId, currentBalance);
    log.info("=============ACCOUNT END=================");
  }

}
```

# 测试

在schema自动执行的脚本里，默认设置了商品价格为10，商品总数量为20，用户余额为50。

启动项目后通过命令行执行。

1. 模拟正常下单，买一个商品。

```shell
curl -X POST \
  http://localhost:8080/order/placeOrder \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "productId": 1,
    "amount": 1
}'
```

2. 模拟库存不足，事务回滚。

```shell
curl -X POST \
  http://localhost:8080/order/placeOrder \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "productId": 1,
    "amount": 22
}'
```

3. 模拟用户余额不足，事务回滚。

```shell
curl -X POST \
  http://localhost:8080/order/placeOrder \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "productId": 1,
    "amount": 6
}'
```

注意观察运行日志，至此分布式事务集成案例全流程完毕。