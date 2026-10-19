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

GraalVM Reachability Metadata 位于共享子模块 `dynamic-datasource-native-metadata`：自身代码与 Druid 各自独立的
`reachability-metadata.json`，每个条目均带 `condition.typeReached` 条件，无需手动配置。
自身代码对 HikariCP 的反射按发起调用的子模块存放，即 `com.baomidou/dynamic-datasource-creator` 与
`com.baomidou/dynamic-datasource-spring`。HikariCP 自身所需的元数据由
https://github.com/oracle/graalvm-reachability-metadata 提供，两个 starter 的 `nativeTest` 均启用了该仓库。

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

## 4.0 分支与贡献流程

| 分支 | 定位 | 规则 |
| --- | --- | --- |
| `main` | 稳定发布线（默认分支） | 只接收来自 `dev` 的发版合并；禁止 force push / 删除（管理员同样受限）；发布 tag 打在这里 |
| `dev` | 日常开发线 | **所有 PR 的目标分支**；CI / CodeQL / 依赖扫描在这里运行 |

流程：分支/fork → **PR 目标选 `dev`**（误选 `main` 会被自动提醒）→ CI 全绿 + 自检清单 →
合并进 `dev` → 维护者在发布时把 `dev` 合并进 `main`（merge commit）并打 tag。

- 为什么这样分：`main` 上的任何提交都可能被外部直接引用（源码链接、发布比对），必须始终处于可发布状态。
- 紧急修复：同样先进 `dev`；随下一次发布一起合并，不单独向 `main` 直推。

版本号唯一来源是根目录 `gradle.properties` 的 `APP_VERSION`（当前 `4.6.0`），
它由根 `build.gradle` 赋给所有模块的 `version`。

`release.yml` 末尾的 `notify-samples` job 会在发布成功后 `repository_dispatch` 通知
[`dynamic-datasource/dynamic-datasource-samples`](https://github.com/dynamic-datasource/dynamic-datasource-samples)
对着刚发布的构件跑全量集成测试，需要仓库 Secret `SAMPLES_DISPATCH_TOKEN`（对该仓库有写权限的 PAT）。
该 job 标了 `continue-on-error`：发布不可逆且已完成，它失败只会告警、不会把发布标红，
但 samples 会退化到靠每日定时任务发现新版本。

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
#    （附 SBOM；Release Note 需交代的事项见 4.3）
git checkout v4.6.0
./gradlew cyclonedxBom
# 4a) 取 GitHub 自动生成稿作为底稿（含全部 PR 与作者）
#     previous_tag_name 必须为真实存在的上一版 tag（用于圈定 PR 区间，替换成实际值）
gh api repos/{owner}/{repo}/releases/generate-notes \
  -f tag_name=v4.6.0 -f previous_tag_name=v4.50 --jq .body > notes.md
# 4b) 融合为最终 notes 后再建 release（一个贡献者都不能丢）：
#     人工分类亮点（升级必读在最前，含 4.3 要交代的遗留问题）→ 自动稿 What's Changed → 特别感谢
gh release create v4.6.0 --latest --notes-file notes.md \
  build/reports/cyclonedx/bom.json#"CycloneDX SBOM (published artifacts)"
git checkout main   # 别把工作区留在 detached HEAD
```

> **Release notes 目标结构**：① 人工分类亮点（升级注意在最前）② 自动稿的 "What's Changed"（每个 PR/作者一行）③「特别感谢」点名本版社区 PR 作者。
> **不要直接裸用 `gh release create --generate-notes`**：它只有 ②，会冲淡升级注意、把贡献者的名字埋进长列表——社区 PR 作者必须被显式感谢（鼓励持续提交），所以按 4a → 4b 的“取回自动稿再融合”流程走。

`cyclonedxBom` 生成的是**聚合 SBOM**（`build/reports/cyclonedx/bom.json`），只包含实际会传递给
使用方的作用域（`runtimeClasspath`/`compileClasspath`）与已发布构件；`dynamic-datasource-test-common`
不发布，因此已被排除在其 `cyclonedxDirectBom` 之外。供 `dependency-scan.yml` 做漏洞扫描用的就是这份清单，
所以不要手改它的内容来"降噪"——需要排除某个模块时，去禁用它的 direct BOM 任务。

`release.yml` 会先执行 `./gradlew clean build`（含三套 starter 测试）作为发布门禁，再执行
`publishAllPublicationsToMavenCentralRepository`。凭据全部来自仓库 Secrets：
`MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` / `GPG_SECRET_KEY` / `GPG_PASSPHRASE`，
以 in-memory 方式签名。

请勿在本地执行 `publish*` 任务（`gradle.properties` 中的 `RELEASE_SIGNING_ENABLED` 与
`mavenCentral*` 属性由 CI 注入，本地没有可用凭据），也不要将密钥环、口令或私钥写入被跟踪
的文件——历史上 `gradle.properties` 曾以明文提交过 file-based 签名配置，该 key 仍存在于 git
历史中，**应当轮换/撤销并重新生成**，新 key 只通过 Secrets 提供给 CI。

另：本地一旦跑过 `publishToMavenLocal`，`~/.m2` 里同 GAV 的线上构件会被覆盖（版本号未变时
尤其隐蔽）。审计"已发布 POM/依赖树"必须用干净仓库：`mvn -Dmaven.repo.local=/tmp/freshm2 ...`。

## 4.3 发布说明：必须向使用方交代的遗留问题
> **这一节是待办清单。下面每条在对应 Release 贴出后即可删除；新增遗留问题时也按同样格式补一条。**
> 之所以写在 CONTRIBUTING 而不是 issue：发布动作本身就是照着 4.2 走的，提醒放在必经之路上才不会漏。

（当前无遗留项。）

# 5. 构建约束（改构建脚本前必读）

以下每条都是实际调试付出过代价的硬约束，违反后的失败信号并不直观：

1. **发布任务必须 `--no-configuration-cache`**。`io.spring.dependency-management` 在生成 POM 时
   回调 `withXml()`，该动作不是缓存安全的（缓存复用后报 `this.delegate is null`）。因此
   `release.yml` 的 publish 步骤显式关掉了缓存；测试/构建任务仍使用缓存。
2. **JDK 基线只能走 toolchain，不能与 `sourceCompatibility` 混用**。一旦 root 的 `subprojects`
   里残留 `sourceCompatibility = 1.8`，Gradle 9 会按 "JVM 8 兼容库" 的元数据约束去解析依赖，
   Boot 3/4 模块直接报 `spring-boot-starter-web:x is only compatible with JVM 17+`。各模块统一
   用 `java { toolchain { ... } }`（默认 8，boot3/boot4 为 17）。
3. **Gradle 客户端自身需要 JVM 17+，且无法用 `gradle-daemon-jvm.properties` 绕过**：该检查发生在
   守护进程启动之前。本机默认 `JAVA_HOME` 为 JDK 8 时需先切到 17+。
4. **`subprojects {}` 块里访问不到版本目录 `libs`**：root 脚本执行时子项目尚未评估，强行使用会得
   到隐式父属性查找弃用警告或 `No such property`。公共依赖（`slf4j-api`、Lombok）因此写在各子
   模块自己的 `build.gradle` 中。同理，版本目录别名不能数字开头（`druid-boot3-starter` 不合法，
   实际生成的访问器保留 camelCase），且访问器名与 `[versions]` 键一致（`libs.versions.springBoot2`，
   写成 `libs.versions.spring.boot2` 会报错）。
5. **`mavenBom` 只接受 `String`**，不能直接传版本目录 Provider，需要 `"...:${libs.versions.x.get()}"` 插值。
6. **sources/javadoc jar 由发布插件自动提供**（`com.vanniktech.maven.publish` 默认
   `withSourcesJar()`/`withJavadocJar()`），手写同名任务会触发
   `multiple artifacts with the identical extension and classifier ('jar', 'javadoc')`。
7. **Lombok 版本不得低于 1.18.42**，否则在 JDK 25 矩阵下编译报 `TypeTag::UNKNOWN`。
8. 不启用 `mavenLocal()`：本地仓库构件不可复现。镜像仓库仅作加速，必须保留 Maven Central 兜底。
9. `doc/` 下原有的 Maven→Gradle 迁移记录已删除（内容被后续重构取代），上述约束即为其存活部分。
10. **Starter 同名类审计与 JaCoCo bundle 限制**。共享配置已优先移入
   `dynamic-datasource-spring-boot-common`；以下是三个 starter 中本次审计的同名类及处置：

   | 类 | 分类 | 处置与原因 |
   | --- | --- | --- |
   | `DynamicDataSourceAopConfiguration` | Advisor 配置相同；处理器链按 servlet 命名空间不同 | advisor/属性配置移入 common；Boot 2、Boot 3/4 分别在 starter 主自动配置中创建 `javax` / `jakarta` 处理器链 |
   | `DynamicDataSourcePoolMetricsAutoConfiguration` | Boot 2/3 仅有 `instanceof` 语法差异；Boot 4 的 Boot API 包名和 nullness API 不同 | Boot 2/3 实现与 Hikari binder 移入 common；Boot 4 使用独立的 `DynamicDataSourcePoolMetricsBoot4AutoConfiguration` |
   | `DsJakartaHeaderProcessor`, `DsJakartaSessionProcessor` | Boot 3/4 源码相同，但依赖 Jakarta servlet 与对应 Spring 请求属性 API | 保留在 Boot 3/4 starter，避免 common 对 Boot 2 `javax.servlet` 基线产生链接依赖 |
   | `DynamicDataSourceAutoConfiguration` | Boot 4 JDBC auto-configuration 包名不同；Druid 导入和顺序也不同 | 各 starter 保留各自入口配置 |
   | `DruidDynamicDataSourceConfiguration` | Boot 2/3 的 Druid starter 包名不同；Boot 4 无此配置类 | 保留 Boot 2/3 版本 |

   JaCoCo 仍不能把三个 starter 放进同一个 bundle：剩余的
   `DynamicDataSourceAutoConfiguration`、Boot 2/3 `DruidDynamicDataSourceConfiguration` 与
   Boot 3/4 `DsJakarta*Processor` 具有重复全限定类名，一起分析会报
   `IllegalStateException: Can't add different class with same name`，外层错误通常只有
   `Error while creating report`。Gradle 9.8 的 `JacocoReport` 没有多 bundle
   (`additionalGroupData`) 入口，所以 `jacocoRootReport` 只统计 `coverageModules` 中无重名的库模块；
   向其中添加模块前，先与现有 class 输入核对全限定类名。
11. **仓库列表声明在 `allprojects` 而不是 `subprojects`**：根任务需要解析 `jacocoAnt`
    （`org.jacoco:org.jacoco.ant`），只在 `subprojects` 里声明会让根项目在写配置缓存时报
    `Cannot resolve external dependency ... because no repositories are defined`。同理 JaCoCo 版本
    写成 root 脚本的局部变量 `jacocoToolVersion`（约束 4：`subprojects {}` 里拿不到版本目录），
    根与子模块共用它，agent 与报告工具版本必须一致，否则 exec 文件解析失败。
