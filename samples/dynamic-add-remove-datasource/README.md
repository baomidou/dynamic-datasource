1. 启动项目.

2. 访问localhost:8080/doc

3.调用now接口查看当前数据源

4. 调用添加接口添加数据源

最小参数为一下内容

```json
{
    "pollName": "11",
    "username": "sa",
    "password": "",
    "driverClassName":"org.h2.Driver",
    "url": "jdbc:h2:mem:test"
}
```

5.重新查看所有数据源
