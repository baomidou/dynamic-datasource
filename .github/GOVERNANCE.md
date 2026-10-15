# Repository Governance Baseline

> 本文件是 `baomidou/dynamic-datasource` 的**治理台账**（governance baseline），面向维护者本人：
> 记录"这个仓库到底配了什么、为什么这么配、什么时候配的"，以及那些**只存在于 GitHub 网页设置里、
> 从代码仓库看不出来**的配置。
>
> 它不重复 `README.md` / `CONTRIBUTING.md` / `AGENTS.md` 的正文内容，而是它们的**索引与补充**。
> 正文改了这里可能过期；两端都改不到时，以本文件的"分区责任人"清单为准去定位。
>
> 最后更新：2026-10-15（对应 `APP_VERSION=4.7.0` 在途、`v4.6.0` 已发布）

---

## 1. 全景速览

| 领域 | 现状 | 落地位置 |
| :--- | :--- | :--- |
| 分支模型 | `dev`（开发）+ `main`（发布线），双向分支保护 | 见 §3 |
| CI 门禁 | 12 个 workflow，覆盖构建/测试/native/安全/文档/仓库自动化 | 见 §5 |
| 供应链安全 | SBOM + OSV + CodeQL + zizmor + Scorecard + Dependabot 六层 | 见 §6 |
| 发布 | tag 触发，含构建门禁、SBOM、Sigstore 证明、发布后外仓验收 | 见 §7 |
| 社区治理 | Issue Forms 分流、PR 影响面清单、自动打标、stale、首次欢迎 | 见 §8 |
| 文档体系 | 双语 README + Wiki FAQ + 文档站 + Javadoc Pages + Agent Skill | 见 §9 |
| AI 协作契约 | `AGENTS.md`（canonical）+ Copilot 镜像 | 见 §4 |

---

## 2. 治理演进时间线

这一节回答"这些东西是什么时候来的"。仓库自 2018 年起维护，但**系统化治理集中在四个窗口**。

### 2.1 2018–2024：功能驱动期

| 时间 | 事件 | 说明 |
| :--- | :--- | :--- |
| 2018-11 | 首个 release (`2.4.0`) | 单模块 Maven、单一 Boot 2 支持 |
| 2020-05 | 首个 Issue 模板 | `8800108` |
| 2020-12 | `codeql-analysis.yml` | `7873987`，2022-12 被 `codeql.yml` 取代 |
| 2021-01 | `maven.yml`、FUNDING.yml | 2023-09 删除 FUNDING，2022-12 删除 maven.yml |
| 2023-01 | `ci.yml` 落地 + test-common 模块 | `5a4207d`，首次有系统单元测试的组织方式 |
| 2023-08/09 | Boot 3 starter 测试补齐、`nativeTest` 入 CI | `18b5312` / `2aed8df` / `9e02908`，**native 支持自此成为一等公民** |
| 2024-06 | `v4.3.1` 最后发布 | 之后进入约 19 个月的沉寂期 |

### 2.2 2025-12：构建系统换代

| commit | 变更 |
| :--- | :--- |
| `e022e69` | **Maven → Gradle**（#751），七模块拆分定型 |
| `98ed59f` | `release.yml` 首版（Gradle 版发布流水线） |
| `c9b0bc8` / `ae817e1` / `60b99fa` | CONTRIBUTING、agent 指令、构建说明跟进 Gradle |

### 2.3 2026-10-12 ~ 10-13：工程质量与安全基座

| commit | 变更 | 性质 |
| :--- | :--- | :--- |
| `e37e86a` | JDK 25 / Gradle 9.8 / Wrapper Neo（#786） | 工具链前瞻 |
| `e0cfedf` | 依赖版本迁移到 `libs.versions.toml` | 单一版本源 |
| `caceeda` | `SECURITY.md` + 私密漏洞上报通道 | 安全 |
| `81ef873` | 重写 Issue Forms（替代失效的旧模板） | 社区 |
| `ad8db2b` | 最小权限 token、`workflow_dispatch`、PR dependency review | 安全 |
| `eaa179f` | **发布门禁从 `build -x test` 改为完整 `clean build`** | 关键修正 |
| `bba0c97` | Dependabot（gradle + github-actions） | 供应链 |
| `0903d78` / `799b009` / `5c09554` | CycloneDX SBOM → 随 release 归档 → 周调度 OSV 扫描 | 供应链 |
| `04619ef` | JaCoCo 聚合报告（`jacocoRootReport`） | 质量 |
| `929acf9` | SBOM 收敛到 `runtimeClasspath`（**降噪关键**） | 供应链 |
| `6c37ab1` | `osv-scanner.toml` 记录已接受的 Boot 2 EOL 漏洞 | 供应链 |
| `41ddcc8` | `SUPPORT.md` 多产线维护策略 | 治理 |
| `dcb30fa` / `188c910` | Release notes 融合流程；4.5.0 遗留问题披露 | 发布 |
| `8ba95c5` / `5e1a111` | nativeTest 按 starter 拆腿、`-Ob`、缓存 wrapper/JDK | CI 性能 |
| `0cfbf54` | README 重写为英文主版 + 简体中文对照 | 文档 |

同期还有一批**正确性修复**（`da4fd2a` 吞异常、`4e043b3` TOCTOU、`131ee7b` 连接泄漏、`f786518` Seata checker 从未注册等），说明该轮是"先把工程质量补实，再谈流程"。

### 2.4 2026-10-14 ~ 10-15：仓库级治理块

这是**最后一跃**，两天内补齐了"仓库作为一个产品"的部分：

| commit | 变更 |
| :--- | :--- |
| `de2cf23` / `08808c6` | `CODEOWNERS`（含 `tracy5546 → huayanYu` 改名说明） |
| `a9a0ff1` | Scorecard + CodeQL 重做 + **zizmor**（扫 workflow 自身） |
| `6f599b8` | stale + labeler + first-interaction |
| `eae3d45` | Sigstore attestation + Develocity build scan |
| `69b51e5` | Javadoc → GitHub Pages + lychee 周检死链 + Discussions 分流 |
| `4e27f27` / `d73a854` | action 全量 pin SHA + main/dev 分支模型落地 |
| `4622cc6` | `pr-target-check.yml`（误投 main 自动提醒） |
| `34d0b63` | Agent Skill（`skills/dynamic-datasource`） |
| `e303ed0` / `9feea1c` | README `Out of scope` 章节（双语） |
| `e83d9bb` | **`AGENTS.md` 固化分支与 PR 流程** |
| `ac2e958` → `1468df5` | code scanning 告警清理，沉淀为 `AGENTS.md` §6/§7 两条教训 |
| `252196e` / `3274b25` | 发布后 `repository_dispatch` 通知 samples 仓验收 |

> **结论**：有效治理历史实际集中在 2026-10-12 ~ 10-15 这四天（§2.3 + §2.4）。在此之前 CI 虽有，
> 但没有 SBOM、没有可信的发布门禁、没有 Issue 分流、也没有对 AI agent 的行为约束。
> 本文件本身即是该轮治理的产物。

---

## 3. 分支模型与权限

### 3.1 分支职责

| 分支 | 定位 | 保护要点 |
| :--- | :--- | :--- |
| `main` | 稳定发布线，default branch | 禁止 force push / 删除；`enforce_admins: true`；**无** required review |
| `dev` | 日常开发线 | 禁止 force push / 删除；`required_approving_review_count: 0`；`enforce_admins: true` |

**为什么 `required_approving_review_count` 是 0**：单人维护，设为 ≥1 会让维护者自己也无法合并。
此设定被明确写进 `AGENTS.md` §0，禁止 AI/贡献者"出于最佳实践"调高。

### 3.2 变更抵达 `main` 的唯一路径

```
feature 分支 → PR(--base dev) → CI 全绿 → 维护者合并 → 维护者 dev→main merge → tag v* → release.yml
```

三层防线防止误投 `main`：
1. `.github/pull_request_template.md` 顶部双语红字提示；
2. `.github/workflows/pr-target-check.yml` 自动评论（使用 `pull_request_target`，已加 zizmor ignore 注释）；
3. AGENTS.md §0 对 AI agent 的硬性禁止。

### 3.3 代码之外：GitHub 仓库设置

> **这一节无法从 clone 出的代码中看出，改仓库或复刻设置时需对照。**

| 项 | 当前值 |
| :--- | :--- |
| Default branch | `main` |
| Visibility | public |
| Issues | 启用 |
| Discussions | **启用**（承担全部使用咨询） |
| Wiki | 独立 git 仓库（未被包含在 clone 中） |
| Dependabot security updates | enabled |
| Secret scanning | enabled |
| Secret scanning push protection | enabled |
| Secret scanning (non-provider patterns) | disabled |
| Secret scanning validity checks | disabled |
| Private vulnerability reporting | 启用（`SECURITY.md` 首选入口） |

### 3.4 Secrets 清单

| Secret | 用途 | 使用位置 |
| :--- | :--- | :--- |
| `MAVEN_CENTRAL_USERNAME` / `_PASSWORD` | Central Portal 凭据 | `release.yml` publish |
| `GPG_SECRET_KEY` / `GPG_PASSPHRASE` | in-memory 签名 | `release.yml` publish |
| `SAMPLES_DISPATCH_TOKEN` | 跨仓 PAT，触发 samples 验收 | `release.yml` notify-samples |

`GITHUB_TOKEN` 保持默认权限，通过 workflow 级 `permissions` 显式收敛，不在仓库级抬高。

### 3.5 Labels

`bug` `ci` `core` `CVE` `dependencies` `documentation` `duplicate` `enhancement`
`good first issue` `java` `stale` `starters` `tests` `todo`

其中 `core` / `starters` / `tests` / `documentation` / `ci` 由 `.github/labeler.yml` 按路径自动施加；
`dependencies` 是 Dependabot 与 stale 豁免的识别标签。

---

## 4. AI 协作者契约

| 文件 | 角色 |
| :--- | :--- |
| [`AGENTS.md`](../AGENTS.md) | **canonical source**，给 Codex / Claude Code / Cursor / Windsurf 等 |
| [`.github/copilot-instructions.md`](copilot-instructions.md) | 镜像，仅因 Copilot 固定读取此路径而存在 |

两者必须保持同步（已在两份文件中互相声明）。`AGENTS.md` 的 §0 为不可协商条款，
§6 / §7 是从 PR #800 实际事故中沉淀的两条教训：

- **§6 负向测试不得复现弱点模式** —— 断言 `IllegalArgumentException` 的坏值必须运行时派生，
  否则静态扫描会持续命中那条"没人真执行"的字面量；
- **§7 抑制注释是假设，CI 才是判决** —— `// codeql[...]` / `# zizmor: ignore[...]` 可能静默失效，
  必须以 `gh pr checks` 的实际运行结果为准。

---

## 5. CI / 自动化全景

共 12 个 workflow。全部 action **pin 到 commit SHA 并附版本号注释**；全部 checkout 使用
`persist-credentials: false`；全部 job 设置了 `timeout-minutes`。

### 5.1 构建与测试

| Workflow | 触发 | 内容 |
| :--- | :--- | :--- |
| `ci.yml` | push/PR(main,dev)、dispatch | ① JDK **17/21/25/27** 矩阵 `clean test`；② GraalVM **25.3.4.1** 下按 starter 拆腿跑 `nativeTest`（Boot3/Boot4）；③ 仅 JDK 17 腿采集 `jacocoRootReport` 并上传 artifact（保留 14 天）；④ PR 专属 `dependency-review`（`fail-on-severity: high`） |

设计要点：
- nativeTest 按 starter 拆两腿并行而非串行（`-Ob` 快速构建，每腿约 5 分钟）；
- 覆盖率只在一条腿采集（各腿测试相同，重复无信息），依赖 Gradle up-to-date 避免重跑；
- wrapper 发行版与 Foojay 供给的 JDK 单独缓存，`~/.gradle/wrapper/dists` + `~/.gradle/jdks`。

### 5.2 发布

见 §7。

### 5.3 仓库自动化

| Workflow | 触发 | 内容 |
| :--- | :--- | :--- |
| `labeler.yml` | PR | 按路径打 `core`/`starters`/`tests`/`documentation`/`ci` |
| `stale.yml` | 每日 01:40 UTC | 60 天标记、14 天关闭；豁免 `enhancement,pinned`（issue）/ `dependencies`（PR） |
| `first-interaction.yml` | issue opened / PR opened | 双语欢迎；排除 `dependabot[bot]` |
| `pr-target-check.yml` | PR opened/edited/reopened | 目标为 `main` 且作者非维护者时自动提示改到 `dev` |

> `stale` / `first-interaction` 用 `pull_request_target` 是必需用法（fork PR 的 `pull_request`
> 事件无写权限），两者均不检出代码，已加 zizmor ignore 注释。注意 zizmor 的 ignore 必须紧贴
> `pull_request_target:` 行上方，贴在 `on:` 上方**不报错但永不生效**。

### 5.4 文档

| Workflow | 触发 | 内容 |
| :--- | :--- | :--- |
| `javadoc.yml` | push main（`*/src/main/java/**` 变化）、dispatch | 生成各模块 Javadoc，拼装索引页发布到 GitHub Pages |
| `lychee.yml` | 周一 02:20 UTC、dispatch | 校验根目录 `*.md` 外链，`fail: true` |

---

## 6. 安全与供应链分层

六个层次各有分工，**不重叠也互不替代**：

| 层 | 工具 | 扫描对象 | 频率 | 是否门禁 |
| :--- | :--- | :--- | :--- | :--- |
| 依赖增量 | `dependency-review`（`ci.yml`） | PR 引入的新依赖 | 每次 PR | ✅ high 即失败 |
| 依赖全量 | `dependency-scan.yml` → osv-scanner | **SBOM**（非源码） | 周五 19:00 UTC | ❌ `fail-on-vuln: false` |
| 自身代码 | `codeql.yml` | Java 源码 | push/PR + 周六 02:15 UTC | ✅ PR status check |
| CI 自身 | `zizmor.yml` | `.github/workflows/**` | workflow 变更时 | ✅ SARIF 入 Code Scanning |
| 仓库治理 | `scorecard.yml` | 分支保护/权限/pinning/危险模式 | 周六 01:30 UTC + push main | 评分制 |
| 版本更新 | Dependabot | gradle + github-actions | 周六 03:00 (Asia/Shanghai) | PR 形式 |

### 6.1 关键设计决策

**① SBOM 必须收敛作用域**（最容易被忽略、收益最大的一条）

`build.gradle` 中 `includeConfigs = ['runtimeClasspath']`。默认全作用域会把 `compileOnly`
的集成库（seata/druid/hikari/c3p0 等）连同传递闭包写进 BOM——**实测 317 条告警约 85% 来自此**，
而这些构件根本不会传给使用者。`compileClasspath` 同理排除。

原则：**降噪靠精确描述范围，不靠关闭扫描或批量静默告警。**

**② 为什么不直接让 osv-scanner 扫源码**

本仓库是 Gradle 项目、无 lockfile，`osv-scanner` 递扫源码读不出依赖版本，会变成"零发现的假绿"。
因此 `dependency-scan.yml` 先跑 `cyclonedxBom` 生成 SBOM，再以 `--sbom=bom.json` 模式扫描。

**③ 已接受风险的登记制度**（`osv-scanner.toml`）

逐条 CVE + reason，且顶部写明维护约定：
- 每条新出现的 Boot 2 线 CVE 需评审后登记；
- **存在同线修复版本时优先升级而非忽略**；
- **Boot 3/4 线不在清单内**，命中即升级 patch。

**④ Dependabot 的克制配置**

`cooldown.default-days: 7`（避开投毒/撤回发布）、minor+patch 合并为单个 PR、
`target-branch: dev`、`open-pull-requests-limit: 5`、
忽略 `org.springframework.boot:*` 的 semver-prerelease。

> 注意：wrapper 是 **source-built 的 Gradle Wrapper Neo 分支**，`gradle-wrapper.jar` 按需生成，
> 其 `distributionSha256Sum` 需手工重算 —— **wrapper 升级 PR 不可自动合并**。

### 6.2 Workflow 安全基线

- 顶层 `permissions` 一律最小（`contents: read`），写权限下放到具体 job；
  `release.yml` 的做法就很典型：`id-token`/`attestations` 只给 build job，留在顶层会连带授予
  notify-samples job，触发 zizmor 的 excessive-permissions；
- 所有外部可控输入（tag 名、PR 号）经 **env 传参**，不在 `run:` 中直接 `${{ }}` 插值，消除模板注入面；
- 发布 job 显式 `cache-disabled: true`：低频操作不怕慢，直接消除 cache-poisoning 面。

---

## 7. 发布流程

### 7.1 版本号与 tag

- 唯一来源：根 [`gradle.properties`](../gradle.properties) 的 `APP_VERSION`（当前 **4.7.0**，在途）
- 由根 `build.gradle` 的 `allprojects` 赋给全部模块
- Tag 规范（见 `CONTRIBUTING.md` §4.1）：**小写 `v` + 三段式**。`v4.5.0` ✅ / `v4.50`、`V1.0.0` ❌
  - 历史遗留 `v4.50` 即是不规范样例，`release.yml` 的 `tags: [v*]` 不匹配大写 `V`
- MAJOR/MINOR/PATCH 语义明文：**存在配置项变更 = MAJOR**，新增能力 = MINOR，缺陷修复 = PATCH
  - Security fix 走 PATCH + GitHub Advisory

### 7.2 release.yml 流水线

tag `v*` → **build job**：

1. `./gradlew clean build` —— **发布门禁**（历史上曾是 `build -x test`，已修正）
2. `publishAllPublicationsToMavenCentralRepository --no-configuration-cache`
   （原因：`io.spring.dependency-management` 的 POM `withXml()` 非缓存安全）
3. `cyclonedxBom` 生成 SBOM 并上传 artifact
4. Sigstore attestation：对 7 个已发布 jar 做 provenance，对 SBOM 做 sbom attestation
5. Develocity build scan（公开，便于追溯构建性能问题）

→ **notify-samples job**：`repository_dispatch` 通知
`dynamic-datasource/dynamic-datasource-samples` 对着**刚发布到 Central 的构件**跑全量集成测试。

> `continue-on-error: true` 的理由写得很直白：发布不可逆，此时构件已经出去了，
> 把这个 job 的失败算进整个 run 是误导维护者；它的失败只应告警。
> 且使用独立 PAT（`SAMPLES_DISPATCH_TOKEN`）与发布凭据隔离。

> **注意：`release.yml` 不负责创建 GitHub Release。** 它只完成"发布到 Maven Central +
> 上传 SBOM artifact + 签发 attestation"；GitHub Release（含 Release Notes 与 SBOM 附件）
> 必须由维护者按 `CONTRIBUTING.md` §4.2 手工创建。这是现状而非疏忽，
> 因为 Release Notes 需要人工融合（见 §7.3），不适合全自动。

### 7.3 Release Notes 规范

**不允许裸用 `gh release create --generate-notes`**，必须走 4a → 4b 融合流程：

```
① gh api .../releases/generate-notes 取回自动稿作为底稿
② 人工分类亮点（升级必读在最前）
③ What's Changed（每个 PR/作者一行）
④「特别感谢」显式点名本版社区 PR 作者
```

理由记录在 `CONTRIBUTING.md` §4.2：自动稿只有 ②，会冲淡升级注意并把贡献者名字埋进长列表，
而**社区贡献者必须被显式感谢**。

### 7.4 发布后必修事项

- 在 GitHub Release 附上 SBOM artifact（`gh release create ... sbom.json#...`）；
- `CONTRIBUTING.md` §4.3「必须向使用方交代的遗留问题」是待办清单，**每条发布后应核销或补充**。

---

## 8. 社区治理

### 8.1 Issue 入口

| 文件 | 作用 |
| :--- | :--- |
| `ISSUE_TEMPLATE/config.yml` | `blank_issues_enabled: false`；三个 contact_links 把**使用咨询 → Discussions**、**文档 → 文档站**、**漏洞 → 私密上报**分流出去 |
| `ISSUE_TEMPLATE/bug_report.yml` | Issue Form，强制 starter 模块 / ds 版本 / Boot 版本 / JDK 版本 / 复现步骤；preflight 含**"已对配置脱敏"**勾选 |

`bug_report.yml` 里的两处措辞值得保留：
- 粘贴异常栈时"不要按日志级别过滤后只贴其中一段——同一毫秒的 ERROR 行往往正是关键信息"；
- dropdown 第一项就说明"选错 starter 模块是最常见的'看起来像 bug'的问题"。

### 8.2 PR 入口

`pull_request_template.md` 的核心是 **影响面清单**——把"本框架的核心契约很窄"编码成勾选框：

`@DS` 解析与栈语义 / 数据源创建与连接池 / 事务语义 / 分组与负载均衡 / ENC 加解密与配置项 / 构建产物。

配套硬要求（见 `AGENTS.md` §5）：改核心语义时必须**保持 v1/v3/v4 三套测试的有效性**，
扩展现有用例而非只新增一个。

### 8.3 策略与边界

| 文件 | 作用 |
| :--- | :--- |
| [`SUPPORT.md`](../SUPPORT.md) | 三条产线的维护状态（冻结/维护/主线）+ 四条变更准入原则 + 支持渠道 |
| [`SECURITY.md`](../SECURITY.md) | 双语；私密上报通道、响应目标、Scope notes（何种报告会被 close）、已加固项 |
| [`CODE_OF_CONDUCT.md`](../CODE_OF_CONDUCT.md) | Contributor Covenant |

`SUPPORT.md` 中最锋利的一条：**不接受"配置项存在但行为不一致"的半支持状态**。
另一条很实用：产线冻结不会删除任何已发布构件。

---

## 9. 文档与知识分发体系

五个层次，**职责不重叠**：

| 层 | 位置 | 承担 |
| :--- | :--- | :--- |
| 入口 | `README.md`（英，canonical）+ `README_zh_CN.md`（中，章节镜像） | 是什么、选哪个 starter、Out of scope、文档导航 |
| 参考 | <https://doc.xiuceyun.cn> | 全部配置项、连接池、集成方案（中文，版本化） |
| 排障 | GitHub Wiki（独立仓库） | FAQ / checklist：@DS 不生效、事务内切换、线程异步、启动报错 |
| API | <https://baomidou.github.io/dynamic-datasource/> | 按模块 Javadoc，由 `javadoc.yml` 自动发布 |
| AI | [`skills/dynamic-datasource/SKILL.md`](../skills/dynamic-datasource/SKILL.md) | 开放 SKILL 格式，面向 AI 助手的安装+排障工作流 |

README 的 **`Out of scope`** 章节值得单独强调：它明确写出项目**不**做什么（不捆绑连接池、
不做 SQL 改写/分片、不实现分布式事务内核、不完套 `module-info`、不做跨线程事务传播），
这条章节同时被 `SUPPORT.md` 反向引用作为新特性提案的第一道过滤条件——**它是预防性 issue 消减**。

> 双语 README 需保持章节对齐（当前中文版标题数量与顺序一致）。改动一侧时应同步另一侧，
> 历史上有过 `9feea1c` 这种"把 out-of-scope 补进中文版"的追平提交。

---

## 10. 构建工程约束索引

散落在 `build.gradle` / `gradle/libs.versions.toml` / `CONTRIBUTING.md` §5 中的硬约束，
统一索引如下（**每条都附了违反后的真实报错信号**，这是它们最有价值的部分）：

| # | 约束 | 违反后的信号 |
| :-- | :--- | :--- |
| 1 | 发布任务必须 `--no-configuration-cache` | `this.delegate is null` |
| 2 | JDK 基线只用 `toolchain`，不混用 `sourceCompatibility` | 依赖解析降级 JVM 8，Boot3/4 报 `only compatible with JVM 17+` |
| 3 | Gradle 客户端需 JVM 17+，无法用 `gradle-daemon-jvm.properties` 绕过 | 守护进程启动前即失败 |
| 4 | `subprojects {}` 内访问不到版本目录 `libs` | 隐式父属性弃用告警 / `No such property` |
| 5 | `mavenBom` 只接受 `String`，需 `${libs.versions.x.get()}` 插值 | 类型错误 |
| 6 | sources/javadoc jar 由发布插件自动提供，不可手写 | `multiple artifacts with identical extension and classifier` |
| 7 | Lombok ≥ 1.18.42（Boot 2.7.18 BOM 只管理 1.18.30） | JDK 25 下 `TypeTag::UNKNOWN` |
| 8 | 不启用 `mavenLocal()`；镜像仅加速、保留 Central 兜底 | 构建不可复现 |
| 9 | JaCoCo 版本写在 `build.gradle`（TOML 在 root 阶段不可读），根与子模块必须同版本 | exec 文件解析失败 |
| 10 | `jacocoRootReport` 不得合并三个 starter（同名类冲突） | `IllegalStateException: Can't add different class with same name` |
| 11 | 仓库声明在 `allprojects` 而非 `subprojects`（根任务需解析 `jacocoAnt`） | `no repositories are defined` |

**防御性设计样例**（值得在新项目复刻）：

- `Automatic-Module-Name` 按 `project.name` .substring 推导，**改名会显式抛异常**而非静默生成错误模块名；
- `coverageModules` 列表缺失时抛 `GradleException`，防止模块"默默从分母里消失"；
- `jacocoRootReport` / SBOM 均**不接入 `build`/`check`**，日常构建不付这份开销，由 CI 显式调用。

---

## 11. 文件资产速查表

> 记不清"某个事情在哪个文件里配的"时，查这张表。

| 我要改…… | 去这里 |
| :--- | :--- |
| 依赖 / 插件版本 | [`gradle/libs.versions.toml`](../gradle/libs.versions.toml)（JaCoCo 例外，在根 `build.gradle`） |
| 仓库、UTF-8、JPMS 模块名、SBOM 范围、覆盖率聚合 | [`build.gradle`](../build.gradle) |
| 模块清单 / toolchain resolver | [`settings.gradle`](../settings.gradle) |
| 版本号 / POM 元数据 / Gradle 优化开关 | [`gradle.properties`](../gradle.properties) |
| 贡献流程、构建约束、发版步骤 | [`CONTRIBUTING.md`](../CONTRIBUTING.md) |
| AI agent 行为规则 | [`AGENTS.md`](../AGENTS.md) + [`.github/copilot-instructions.md`](copilot-instructions.md) |
| 漏洞响应政策 | [`SECURITY.md`](../SECURITY.md) |
| 多产线支持策略 | [`SUPPORT.md`](../SUPPORT.md) |
| 已接受 CVE 清单 | [`osv-scanner.toml`](../osv-scanner.toml) |
| Issue 入口与分流 | `ISSUE_TEMPLATE/config.yml`、`bug_report.yml` |
| PR 自检项 | [`.github/pull_request_template.md`](pull_request_template.md) |
| 自动打标规则 | `.github/labeler.yml` |
| CODEOWNERS | `.github/CODEOWNERS` |
| CI / 发布 / 扫描 | `.github/workflows/*.yml`（12 个，见 §5） |
| AI 助手知识包 | `skills/dynamic-datasource/SKILL.md` + `references/` |
| GraalVM 元数据与过滤链 | `native-image/*.json`、`dynamic-datasource-native-metadata/` |

---

## 12. 已知缺口与待办

记录当前尚未做或不完善的部分，避免误以为治理已经完备：

| 项 | 说明 |
| :--- | :--- |
| `dependency-scan` 未成为门禁 | 现在 `fail-on-vuln: false`，先把存量告警留在 Code Scanning 页面；待**存量清零后再改为 true** |
| Scorecard 分数 | 有徽章与 SARIF，但未针对低分项做系统性整改（如 `signed-releases`、`fuzzing`、`dangerous-workflow` 细节） |
| 覆盖率无外部徽章 | `jacocoRootReport` 只上传 artifact，未接 Codecov 等；是刻意选择（避免外部服务依赖与 token） |
| Dependabot alerts 策略未文档化 | `dependabot.yml` 只覆盖版本更新；alerts 触发的安全更新走仓库设置，处置流程未落文档；grouped PR 仍需人工回归 nativeTest |
| Secret scanning 增强项未开 | `non_provider_patterns` 与 `validity_checks` 均为 disabled |
| 历史 tag 不合规 | `v4.50` 等旧 tag 违反当前 tag 规范，属不可变历史，仅文档约束新增 |
| Wiki 不在 CI 内 | Wiki 是独立仓库，lychee 只作为外链校验其 URL，**内容不在 CI 保护范围内** |

---

## 附录 A：把这套治理套到新项目的检查清单

本文件描述的每一层都可独立迁移。按 ROI 排序：

**第一梯队（与语言/构建工具无关，直接可用）**
1. `AGENTS.md` + copilot 镜像 + 同步声明，尤其 §0 的"技术强制而非约定"写法
2. `CONTRIBUTING.md` 的"构建约束清单"格式：**每条必附违反后的真实报错信号**
3. Issue Form + `config.yml` 的 Discussions 分流
4. `pull_request_template.md` 的**影响面清单**（把核心契约枚举成勾选框）
5. `SUPPORT.md` 的多产线/多代际维护状态表

**第二梯队（概念通用，工具需替换）**
6. SBOM → OSV 的两段式依赖扫描（关键是**先有 SBOM**）
7. SBOM 作用域收敛到"真正传给用户的作用域"，以此降噪而非关停扫描
8. `osv-scanner.toml` 式的 suppression 清单：每条 CVE 带 reason + "优先升级而非忽略"的维护约定
9. workflow 安全基线四件套：最小 `permissions` + 全 SHA pin + `persist-credentials: false` + env 传参防注入
10. 发布门禁 = 完整构建测试（不要 `-x test`）

**第三梯队（本项目特有）**
11. `skills/` Agent Skill 格式的知识分发
12. zizmor 静态扫描 workflow 自身
13. Sigstore provenance + SBOM attestation
14. 发布后跨仓 `repository_dispatch` 触发下游仓库用**已发布构件**验收

**迁移时要诚实回答的问题**
- 核心契约是什么？能否枚举成 PR 模板上的 5–7 个勾选框？
- 有没有一个位于**发布必经之路**上的待办清单（类比 `CONTRIBUTING.md` §4.3）？
- 覆盖率统计是否会因为"测试在 starter 模块、被测代码在核心模块"而失真？（本仓库因此单做 `jacocoRootReport`）
- 有哪些地方需要**反向约束 AI 的"好心"**？（例如本仓库禁止把 required review 数量调高）
