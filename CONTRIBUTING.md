# 1. FORK

Git Mirror 位于 https://github.com/baomidou/dynamic-datasource 。

# 2. RUN TEST

此项目在 OpenJDK 17 下完成构建，输出产物指向 OpenJDK 8。

当项目导入 IntelliJ IDEA 或 VSCode 时，IDE 对项目的语言级别应当设置为 8。
对于单独的 `com.baomidou:dynamic-datasource-spring-boot3-starter` 子模块，IDE 的语言级别应当设置为 17。

提交 PR 前，应在 OpenJDK 17 下执行 `./mvnw -T1C -B clean test` 以验证更改是否未破坏单元测试。若有需要请补充或更改单元测试。

假设贡献者处于新的 Ubuntu 22.04.3 LTS 实例下，其可通过如下 bash 命令来通过 SDKMAN! 管理 JDK 和工具链，并执行单元测试。

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
通过在 JVM 下运行单元测试，为单元测试打上 `junit-platform-unique-ids*`，此后构建为 GraalVM Native Image 进行 nativeTest 来测试在 
GraalVM Native Image 下的单元测试覆盖率。请不要使用 `io.kotest:kotest-runner-junit5-jvm:5.5.4` 等在 `test listener` mode 下 
failed to discover tests 的测试库。

项目定义了 `nativeTestInSpringBoot` 的 Maven Profile 用于为 `dynamic-datasource-spring-boot3-starter` 模块执行 nativeTest。

假设贡献者处于新的 Ubuntu 22.04.3 LTS 实例下，其可通过如下 bash 命令通过 SDKMAN! 管理 JDK 和工具链，
并为 `dynamic-datasource-spring-boot3-starter` 子模块执行 nativeTest。

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-graalce
sdk use java 21-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y

git clone git@github.com:baomidou/dynamic-datasource.git
cd ./dynamic-datasource/
./mvnw -PnativeTestInSpringBoot -T1C -e clean test
```

贡献者在提交 PR 后，位于 Github Actions 的 CI 将进行此验证。如果 nativeTest 执行失败，请跳转到[本文的 2.2 一节](./CONTRIBUTING.md)。

当贡献者发现缺少与 `dynamic-datasource` 无关的第三方库的 GraalVM Reachability Metadata 时，应当在
https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue， 并提交包含依赖的第三方库缺失的 GraalVM Reachability 
Metadata 的 PR。

## 2.2 Generate or merge GraalVM Reachability Metadata for unit tests

如果 nativeTest 执行失败， 应为单元测试生成初步的 GraalVM Reachability Metadata，并手动调整以修复 nativeTest。
如有需要，请使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 注解或 `org.graalvm.nativeimage.imagecode` 的 
System Property 屏蔽部分单元测试在 GraalVM Native Image 下运行。

请不要为 SpEL 功能编写可能的 nativeTest，参考尚未关闭的 https://github.com/spring-projects/spring-framework/issues/29548 。

项目定义了 `generateMetadata` 的 Maven Profile 用于在普通 JVM 下携带 GraalVM Tracing Agent 执行单元测试，并在特定目录下生成或合并
已有的 GraalVM Reachability Metadata 文件。可通过如下 bash 命令简单处理此流程。贡献者仍可能需要手动调整具体的 JSON 条目，并在适当的时候
调整 Maven Profile 和 GraalVM Tracing Agent 的 Filter 链。

以下命令仅为 `dynamic-datasource-spring-boot3-starter` 生成 Conditioanl 形态的 GraalVM Reachability Metadata 的一个举例。

对于测试类和测试文件独立使用的 GraalVM Reachability Metadata，贡献者应该放置到相关子模块对应的 
`${project.basedir}/src/test/resources/META-INF/native-image/${project.artifactId}-test-metadata/` 文件夹下。`${}` 内为 
POM 4.0 的常规系统变量，自行替换。

```bash
git clone git@github.com:baomidou/dynamic-datasource.git
cd ./dynamic-datasource/
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
./mvnw -PnativeTestInSpringBoot -T1C -e clean test
```

请手动删除无任何具体条目的 JSON 文件。

# 3. PR

PR 应提交到位于 Github 的 Git Mirror，即 https://github.com/baomidou/dynamic-datasource 。
位于 Github Actions 的 CI 将在 OpenJDK 8+ 下对 PR 对应分支执行对应的单元测试。
