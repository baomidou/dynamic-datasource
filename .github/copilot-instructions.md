# Project instructions

> **Read [`AGENTS.md`](../../AGENTS.md) first.** It is the canonical rules file for AI agents and
> human contributors; this file mirrors it for GitHub Copilot. Its §0 is non-negotiable:
> **never commit or push directly to `dev` or `main`** — create a `<type>/<slug>` branch off `dev`,
> push it, and open a PR with `--base dev`. Do not merge it yourself.

## What this is

`dynamic-datasource` — a Spring Boot starter that routes JDBC access across multiple
datasources at runtime (`@DS`, datasource groups, `@DSTransactional`). It is a **library**
published to Maven Central, not an application: there is no `main()`, no server to run.

Seven Gradle modules (Groovy DSL, Gradle 9.8 via the wrapper):

- `dynamic-datasource-creator` – connection-pool/datasource creation (Druid, HikariCP, beecp, c3p0, dbcp2, Oracle UCP, Atomikos XA)
- `dynamic-datasource-spring` – the core: `DynamicRoutingDataSource`, `@DS` AOP, transaction support
- `dynamic-datasource-spring-boot-common` – shared autoconfigure/properties for the Boot 2/3/4 starters
- `dynamic-datasource-spring-boot-starter` – Boot **1.5.x ~ 2.x** entry point (JDK 8 baseline)
- `dynamic-datasource-spring-boot3-starter` – Boot **3.x** entry point (JDK 17+)
- `dynamic-datasource-spring-boot4-starter` – Boot **4.x** entry point (JDK 17+)
- `dynamic-datasource-test-common` – services/entities/SQL scripts shared by the starters' tests

The three starters are thin: they differ mainly in Spring Boot auto-configuration wiring
(`AutoConfiguration.imports` vs `spring.factories`) and the Boot generation they compile
against. Behaviour changes almost always belong in `-spring` / `-creator`, and then need to
be exercised in **all three** starter test suites (`v1` / `v3` / `v4` packages).

## Build and test

The Gradle **client requires JVM 17+**; module bytecode is produced by Java toolchains
(8 by default, 17 for the Boot 3/4 starters) which Gradle provisions automatically.

```bash
./gradlew build                                              # everything
./gradlew :dynamic-datasource-spring:test                    # one module
./gradlew :dynamic-datasource-spring-boot3-starter:test --tests "*DsTransactionalTest*"
```

Tests use H2 in-memory databases plus the SQL scripts in
`dynamic-datasource-test-common/src/main/resources/db/`; no external database or network
access is required. Do not add tests that need a live MySQL/Postgres.

> 本地环境备注 / local environment note: the maintainer's shell defines `jdk17` as a shortcut
> to switch the current JDK to 17 (`jdk8`, `jdk21` … same pattern) before running Gradle.

### Running nativeTest locally (required when you touch reflection)

CI's `test-graalvm-ce-ci` runs `nativeTest` for `spring-boot3-starter` / `spring-boot4-starter`;
the GraalVM version lives in the `version` matrix of `ci.yml` (currently `25.3.4.1`,
distribution `graalvm-community`). You can reproduce it locally with
[sdkman](https://sdkman.io):

```bash
sdk install java 25.4.4.1+1-graalce        # CE versions carry a build number; `25.4.4.1-graalce` alone fails
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.4.4.1+1-graalce/Home"   # macOS layout: real home is Home/
export PATH="$JAVA_HOME/bin:$PATH"
java -version && native-image --version     # both must print
./gradlew :dynamic-datasource-spring-boot4-starter:nativeTest
./gradlew :dynamic-datasource-spring-boot3-starter:nativeTest
```

Each leg takes 3–5 minutes (native-image compilation dominates).

Two gotchas:

- **`sdk use` may not switch on macOS**: `current` can keep pointing at the old JDK. Set
  `JAVA_HOME` to `<version>/Home` directly instead of retrying `sdk use`.
- **Install may hang at `Cleaning up residual files...`**: a `safe-delete`-style hook that blocks
  bulk deletion stops sdkman from clearing `~/.sdkman/tmp/Java-<version>`, and nothing lands in
  `candidates/`. The archive is already unpacked — finish it by hand:

  ```bash
  mv ~/.sdkman/tmp/Java-25.4.4.1+1-graalce ~/.sdkman/candidates/java/25.4.4.1+1-graalce
  ```

**When it is mandatory**: anything relying on reflection (`Introspector`, `Class.getMethods()`,
dynamic proxies) needs entries in `reachability-metadata.json` under
`dynamic-datasource-native-metadata`. Unregistered reflection usually does **not** throw — it
silently degrades (e.g. an empty property list), so green JVM tests prove nothing here. State in
the PR whether you ran `nativeTest`.

Never run `publish*`/`sign*` tasks locally. Signing and Central credentials come from CI
secrets (in-memory GPG key); do not put credentials or key rings into tracked files.

## Branching and pull requests

- Development happens on **`dev`**; **`main`** is the stable release line (default branch,
  protected against force pushes and deletion, admins included). **Target pull requests at
  `dev`** — never at `main`; `main` only receives release merges from `dev`, and PRs opened
  against `main` by mistake get an automated reminder.
- CI, CodeQL and the dependency scan run on both `main` and `dev`; javadoc and Scorecard run
  on `main` only.
- Releases: the maintainer merges `dev` into `main` (merge commit), bumps `APP_VERSION`,
  tags `vX.Y.Z` (annotated) and pushes the tag — CI publishes to Maven Central.
  Full procedure and release-notes rules: CONTRIBUTING §4.
- Commit style: conventional-commit prefixes, one logical change per commit — release notes
  are assembled from these titles.

## Conventions

- **Versions**: all dependency versions live in `gradle/libs.versions.toml`. Add or update a
  version/catalog entry there; do not hardcode `"group:artifact:1.2.3"` in a `build.gradle`.
  Exception: the `mavenBom` strings in the starters, which need a resolved `String`.
- **Dependency scope**: optional pools/pools-specific creators are `compileOnly` (the user's
  application supplies them); `org.slf4j:slf4j-api` is `api`. Keep published POM scopes
  deliberate — they are the consumer-visible contract.
- **Language level**: write code that compiles under the Java 8 toolchain for the core and
  Boot 2 modules (no `var`, records, switch expressions, text blocks). The Boot 3/4 starter
  modules may use Java 17 constructs.
- **License header**: every Java file starts with the `Copyright © 2018 organization baomidou`
  header (Apache 2.0); copy it from a neighbouring file.
- Lombok is `compileOnly` + `annotationProcessor` and is used for boilerplate in
  properties/config classes; plain code is fine elsewhere.
- Comments and Javadoc are sparse and in Chinese in much of the core; match the style of the
  file you are editing rather than adding narration.

## Before finishing a change

- Run `./gradlew build` (covers all modules and the three test suites).
- A change to `@DS` resolution, `DynamicDataSourceContextHolder` stack semantics, transaction
  modes, or datasource add/remove behaviour must keep the corresponding `v1`/`v3`/`v4` tests
  honest — extend them rather than only adding a new one.
- New/renamed configuration properties belong in `DynamicDataSourceProperties` and must be
  documented at https://doc.xiuceyun.cn (separate repository).
- Security findings are **not** public issues: see `SECURITY.md`.
