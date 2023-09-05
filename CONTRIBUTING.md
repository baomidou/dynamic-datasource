# 1. FORK

Git Mirror 位于 https://github.com/baomidou/dynamic-datasource 。

# 2. RUN TEST

此项目在 OpenJDK 17 下完成构建，输出产物指向 OpenJDK 8。

当项目导入 IntelliJ IDEA 或 VSCode 时，IDE 对项目的语言级别应当设置为 8。
对于单独的 `com.baomidou:dynamic-datasource-spring-boot3-starter` 子模块，IDE 的语言级别应当设置为 17。

提交 PR 前，应在 OpenJDK 17 下执行 `./mvnw -T1C -B clean test` 以验证更改是否未破坏单元测试。若有需要请补充或更改单元测试。

假设贡献者处于新的 Ubuntu 22.04.3 LTS 实例下，其可通过如下 bash 命令来执行单元测试。

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.8-ms
sdk use java 17.0.8-ms

git clone git@github.com:baomidou/dynamic-datasource.git
cd ./dynamic-datasource/
./mvnw -T1C -B clean test
```

## 2.1. Execute NativeTest under GraalVM Native Image

项目的输出产物对在 GraalVM Native Image 下的可用性的验证，是通过 GraalVM Native Build Tools 的 Maven Plugin 子项目来完成的。
通过在 JVM 下运行单元测试，为单元测试打上 unique Id，此后构建为 GraalVM Native Image 进行 nativeTest 来测试在 GraalVM Native Image 
下的单元测试覆盖率。

项目定义了 `generateMetadata` 的 Maven Profile 用于在普通 JVM 下携带 GraalVM Tracing Agent 执行单元测试，并在特定目录下生成或合并
已有的 GraalVM Reachability Metadata 文件。

项目定义了 `nativeTestInSpringBoot` 的 Maven Profile 用于为 `dynamic-datasource-spring-boot3-starter` 模块执行 nativeTest。

假设贡献者处于新的 Ubuntu 22.04.3 LTS 实例下，其可通过如下 bash 命令为 `dynamic-datasource-spring-boot3-starter` 子模块执行 nativeTest。

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.8-graalce
sdk use java 17.0.8-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y

git clone git@github.com:baomidou/dynamic-datasource.git
cd ./dynamic-datasource/
./mvnw -T1C -B -PgenerateMetadata -DskipNativeTests clean test
./mvnw -am -pl dynamic-datasource-spring-boot3-starter -PnativeTestInSpringBoot -T1C -B clean test
```

贡献者在提交 PR 后，位于 Github Actions 的 CI 将进行此验证。相关 CI 的失败行为是现阶段是可接受的。

请不要为 SPEL 功能编写可能的 nativeTest，参考 https://github.com/spring-projects/spring-framework/issues/29548 。如有需要，
请使用 `org.graalvm.nativeimage.imagecode` 的 System Property 屏蔽相关测试在 GraalVM Native Image 下运行。

当贡献者发现缺少与 `dynamic-datasource` 无关的第三方库的 GraalVM Reachability Metadata 时，应当在
https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue， 并提交包含依赖的第三方库缺失的 GraalVM Reachability 
Metadata 的 PR。

# 3. PR

PR 应提交到位于 Github 的 Git Mirror，即 https://github.com/baomidou/dynamic-datasource 。
位于 Github Actions 的 CI 将在 OpenJDK 8 和 OpenJDK 17 下对 PR 对应分支执行对应的单元测试。
