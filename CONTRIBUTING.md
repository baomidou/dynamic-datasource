# FORK

Git Mirror 位于 https://github.com/baomidou/dynamic-datasource 。

# RUN TEST

此项目在 OpenJDK 17 下完成构建，输出产物指向 OpenJDK 7。

当项目导入 IntelliJ IDEA 或 VSCode 时，IDE 对项目的语言级别应当设置为 7。
对于单独的 `com.baomidou:dynamic-datasource-spring-boot3-starter` 子模块，IDE 的语言级别应当设置为 17。

提交 PR 前，应在 OpenJDK 17 下执行 `./mvnw -T1C -B clean test` 以验证更改是否未破坏单元测试。若有需要请补充或更改单元测试。

# PR

PR 应提交到位于 Github 的 Git Mirror，即 https://github.com/baomidou/dynamic-datasource 。
位于 Github Actions 的 CI 将在 OpenJDK 8 和 OpenJDK 17 下对 PR 对应分支执行对应的单元测试。
