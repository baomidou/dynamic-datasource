# 1. FORK

Git Mirror 位于 https://github.com/baomidou/dynamic-datasource 。

# 2. RUN TEST

此项目使用 Gradle 9 构建，**Gradle 客户端与守护进程要求 JVM 17 或更高**（即使 Boot 2 线产物
面向 OpenJDK 8——编译/测试由各模块的 Java toolchain 决定，缺失的 toolchain 会通过 Foojay 自动供给）。
若本机默认 `JAVA_HOME` 指向 JDK 8，`./gradlew` 会直接报 `Gradle requires JVM 17 or later to run`，
需要先切换到 JDK 17+。

项目的输出产物对在 GraalVM Native Image 下的可用性，是通过原生镜像测试验证的。

当项目导入 IntelliJ IDEA 或 VSCode 时，IDE 对项目的语言级别应当设置为 8。
对于单独的 `com.baomidou:dynamic-datasource-spring-boot3-starter` 与 `com.baomidou:dynamic-datasource-spring-boot4-starter` 子模块，IDE 的语言级别应当设置为 17。

提交 PR 前，应在 OpenJDK 17+ 下执行 `./gradlew clean test` 以验证更改是否未破坏单元测试
（JDK 8 toolchain 下的 Boot 2 线测试会在构建中自动使用/下载 JDK 8）。若有需要请补充或更改单元测试。

假设贡献者处于新的 Ubuntu 22.04.3 LTS 实例下，其可通过如下 bash 命令来通过 SDKMAN! 管理 JDK 和工具链，并执行单元测试。

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.8-ms
sdk use java 17.0.8-ms

git clone git@github.com:baomidou/dynamic-datasource.git
cd ./dynamic-datasource/
./gradlew clean test
```

## 2.1. Execute NativeTest under GraalVM Native Image

`dynamic-datasource-spring-boot3-starter` 与 `dynamic-datasource-spring-boot4-starter` 使用 GraalVM Native Build Tools 的 Gradle Plugin
提供 `nativeTest`：先在 JVM 下运行单元测试（记录 `junit-platform-unique-ids*`），再把同一批测试编译为 GraalVM Native Image 执行。
请不要使用 `io.kotest:kotest-runner-junit5-jvm:5.5.4` 等在 `test listener` mode 下 failed to discover tests 的测试库。

GraalVM Reachability Metadata 位于共享子模块 `dynamic-datasource-native-metadata`：自身代码、Druid 与 HikariCP 各自独立的
`reachability-metadata.json`，每个条目均带 `condition.typeReached` 条件，无需手动配置。

假设贡献者处于新的 Ubuntu 26.04 实例下：

```bash
curl -sSL https://raw.githubusercontent.com/version-fox/vfox/main/install.sh | bash
echo 'eval "$(vfox activate bash)"' >> ~/.bashrc
source ~/.bashrc
vfox add java
vfox install java@25.3.4.1-graalce
vfox use --global java@25.3.4.1-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y

git clone git@github.com:baomidou/dynamic-datasource.git
cd ./dynamic-datasource/
./gradlew nativeTest --continue
```

贡献者在提交 PR 后，位于 Github Actions 的 CI 将进行此验证。如果 nativeTest 执行失败，请跳转到[本文的 2.2 一节](./CONTRIBUTING.md)。

当贡献者发现缺少与 `dynamic-datasource` 无关的第三方库的 GraalVM Reachability Metadata 时，应当在
https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue， 并提交包含依赖的第三方库缺失的 GraalVM Reachability 
Metadata 的 PR。

## 2.2 Generate or merge GraalVM Reachability Metadata for unit tests

如果 nativeTest 执行失败，应为单元测试生成初步的 GraalVM Reachability Metadata，并手动调整以修复 nativeTest。
如有需要，请使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 注解屏蔽部分单元测试在 GraalVM Native Image 下运行。
请不要为 SpEL 功能编写可能的 nativeTest，参考尚未关闭的 https://github.com/spring-projects/spring-framework/issues/29548 。

`generateMetadata` 任务会携带 GraalVM Tracing Agent 执行单元测试（`-Pagent`）并通过 `metadataCopy` 把元数据合并到
`dynamic-datasource-native-metadata` 子模块。Tracing Agent 的 Filter 链位于仓库根目录的 `native-image/` 下，
贡献者可能仍需手动调整具体的 JSON 条目与 Filter。

```bash
./gradlew generateMetadata -Pagent
```

测试专用的 GraalVM Reachability Metadata 放在各子模块的
`src/test/resources/META-INF/native-image/${project.name}-test-metadata/reachability-metadata.json` 下，自行替换 `${}` 内的子模块名。
请手动删除无任何具体条目的 JSON 文件。

# 3. PR

PR 应提交到位于 Github 的 Git Mirror，即 https://github.com/baomidou/dynamic-datasource 。
位于 Github Actions 的 CI 会在 JDK 8 toolchain 下编译并测试 Boot 2 线产物，并在 JDK 17/21/25 矩阵下
验证其余模块（运行器上的 Gradle 客户端本身需要 JVM 17+）。

PR 标题与 commit message 建议使用 conventional 前缀（`fix:` / `feat:` / `build:` / `docs:` /
`refactor:` / `ci:` / `chore:` / `test:`），一个 commit 只做一件事；发布说明（release notes）
正是由这些标题汇总生成的。请填写 `.github/pull_request_template.md` 中的影响面清单，
涉及 `@DS` 解析、数据源切换栈语义、事务模式的改动必须同时保留 v1/v3/v4 三套测试的有效性。

# 4. RELEASE

版本号唯一来源是根目录 `gradle.properties` 的 `APP_VERSION`（当前 `4.5.0`），
它由根 `build.gradle` 赋给所有模块的 `version`。

## 4.1 Tag 约定

仓库历史上同时存在 `V1.0.0`、`v4.3.1`、`v4.50` 等形态，而 GitHub Release 标题写的是 `v4.5.0`。
统一为**小写 `v` + 完整语义化版本**：

- 正确：`v4.5.0`、`v4.6.0`、`v4.5.1`（`v*` 才会触发发布流水线，大写 `V` 不匹配该模式）
- 避免：`v4.50`（看不出三段式，无法区分 patch/minor）、`V1.0.0`、不带 `v` 的裸版本号

MAJOR/MINOR/PATCH 的含义：MAJOR = 不兼容的公开 API 或配置项变更；MINOR = 向后兼容的新能力
（新增注解、新增连接池支持）；PATCH = 缺陷修复。Security fix 走 PATCH，并按 `SECURITY.md`
发布 GitHub Advisory。

## 4.2 发布步骤

```bash
# 1) 确认主干 CI 为绿，发布不可撤回
# 2) 修改 gradle.properties 的 APP_VERSION 并提交
git commit -am "build: release 4.6.0"
# 3) 打附注标签并推送标签，触发 release.yml
git tag -a v4.6.0 -m "v4.6.0"
git push origin v4.6.0
# 4) 流水线成功且构件已同步到 Maven Central 后，创建对应 Release
git checkout v4.6.0
gh release create v4.6.0 --generate-notes --latest
```

`release.yml` 会先执行 `./gradlew clean build`（含三套 starter 测试）作为发布门禁，再执行
`publishAllPublicationsToMavenCentralRepository`。凭据全部来自仓库 Secrets：
`MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` / `GPG_SECRET_KEY` / `GPG_PASSPHRASE`，
以 in-memory 方式签名。

请勿在本地执行 `publish*` 任务（`gradle.properties` 中的 `RELEASE_SIGNING_ENABLED` 与
`mavenCentral*` 属性由 CI 注入，本地没有可用凭据），也不要将密钥环、口令或私钥写入被跟踪
的文件——历史上 `gradle.properties` 曾以明文提交过 file-based 签名配置，该 key 仍存在于 git
历史中，**应当轮换/撤销并重新生成**，新 key 只通过 Secrets 提供给 CI。
