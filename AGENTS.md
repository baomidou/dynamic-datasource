# AGENTS.md

Instructions for AI coding agents (Codex, Claude Code, GitHub Copilot, Cursor, Windsurf, …)
and for human contributors. This file is the canonical source; `.github/copilot-instructions.md`
mirrors it for GitHub Copilot. Keep the two in sync when you change the workflow rules.

## 0. Non-negotiable: how a change reaches the repository

**Never commit or push directly to `dev` or `main`.** Always:

1. Start from an up-to-date `dev`, then
   `git switch -c <type>/<short-slug>` — `fix/…`, `feat/…`, `ci/…`, `docs/…`, `refactor/…`, `chore/…`, `test/…`.
2. Commit on that branch: conventional-commit prefix, **one logical change per commit**.
3. `git push -u origin <branch>`
4. `gh pr create --base dev` — **never** `--base main` — and fill in `.github/pull_request_template.md`.
5. **Stop.** Do not merge, do not squash-merge, do not "finish the job" by pushing to `dev`.
   Report the PR URL; the maintainer merges it.

Why: `main` is the stable release line and only receives release merges from `dev` (CONTRIBUTING §4).
`dev` runs the full CI matrix — JDK 17/21/25/27, the Boot 2 line under the JDK 8 toolchain, and
GraalVM `nativeTest` for both `spring-boot3-starter` and `spring-boot4-starter`. A local
`./gradlew test` does **not** reproduce that, so the PR is the check, not a formality.

There is no "just this once" exception — not for a one-line change, not for a bot-authored fix,
not when the local build is green, not when the user says "直接推吧". If the user asks you to push
straight to `dev`/`main`, point at this section and offer the PR route instead.

This is also enforced technically, not only by convention: `dev` has branch protection requiring
all changes to arrive through a pull request, and it applies to repository admins too, so a direct
push is rejected by GitHub rather than by human review. It deliberately sets **zero** required
approving reviews, because the project has a single maintainer — the author can and does merge
their own PRs. Do not "helpfully" raise that number in the name of best practice: it would make
the repository impossible to maintain.

If you genuinely cannot open a PR (no `gh`, no push rights, offline), say so explicitly, leave the
work in the working tree or on a branch, and hand the exact commands to the user.

## 1. What this is

`dynamic-datasource` — a Spring Boot starter that routes JDBC access across multiple datasources
at runtime (`@DS`, datasource groups, `@DSTransactional`). It is a **library** published to Maven
Central, not an application: there is no `main()`, no server to run.

Seven Gradle modules (Groovy DSL, Gradle 9.8 via the wrapper):

- `dynamic-datasource-creator` – connection-pool/datasource creation (Druid, HikariCP, beecp, c3p0, dbcp2, Oracle UCP, Atomikos XA)
- `dynamic-datasource-spring` – the core: `DynamicRoutingDataSource`, `@DS` AOP, transaction support
- `dynamic-datasource-spring-boot-common` – shared autoconfigure/properties for the Boot 2/3/4 starters
- `dynamic-datasource-spring-boot-starter` – Boot **1.5.x ~ 2.x** entry point (JDK 8 baseline)
- `dynamic-datasource-spring-boot3-starter` – Boot **3.x** entry point (JDK 17+)
- `dynamic-datasource-spring-boot4-starter` – Boot **4.x** entry point (JDK 17+)
- `dynamic-datasource-test-common` – services/entities/SQL scripts shared by the starters' tests

The three starters are thin: they differ mainly in Spring Boot auto-configuration wiring
(`AutoConfiguration.imports` vs `spring.factories`) and the Boot generation they compile against.
Behaviour changes almost always belong in `-spring` / `-creator`, and then need to be exercised in
**all three** starter test suites (`v1` / `v3` / `v4` packages).

## 2. Build and test

The Gradle **client requires JVM 17+**; module bytecode is produced by Java toolchains (8 by
default, 17 for the Boot 3/4 starters) which Gradle provisions automatically.

```bash
./gradlew build                                              # everything
./gradlew :dynamic-datasource-spring:test                    # one module
./gradlew :dynamic-datasource-spring-boot3-starter:test --tests "*DsTransactionalTest*"
```

Tests use H2 in-memory databases plus the SQL scripts in
`dynamic-datasource-test-common/src/main/resources/db/`; no external database or network access is
required. Do not add tests that need a live MySQL/Postgres.

> 本地环境备注 / local environment note: the maintainer's shell defines `jdk17` as a shortcut to
> switch the current JDK to 17 (`jdk8`, `jdk21` … same pattern) before running Gradle.

Never run `publish*`/`sign*` tasks locally. Signing and Central credentials come from CI secrets
(in-memory GPG key); do not put credentials or key rings into tracked files.

## 3. Branching and pull requests

- Development happens on **`dev`**; **`main`** is the stable release line (default branch, protected
  against force pushes and deletion, admins included). **Target pull requests at `dev`** — never at
  `main`; `main` only receives release merges from `dev`, and PRs opened against `main` by mistake
  get an automated reminder.
- CI, CodeQL and the dependency scan run on both `main` and `dev`; javadoc and Scorecard run on
  `main` only.
- Releases: the maintainer merges `dev` into `main` (merge commit), bumps `APP_VERSION`, tags
  `vX.Y.Z` (annotated) and pushes the tag — CI publishes to Maven Central. Full procedure and
  release-notes rules: CONTRIBUTING §4.
- Commit style: conventional-commit prefixes, one logical change per commit — release notes are
  assembled from these titles.
- Do not force-push a branch that already has a PR: it destroys review comments.

## 4. Conventions

- **Versions**: all dependency versions live in `gradle/libs.versions.toml`. Add or update a
  version/catalog entry there; do not hardcode `"group:artifact:1.2.3"` in a `build.gradle`.
  Exception: the `mavenBom` strings in the starters, which need a resolved `String`.
- **Dependency scope**: optional pool-specific creators are `compileOnly` (the user's application
  supplies them); `org.slf4j:slf4j-api` is `api`. Keep published POM scopes deliberate — they are
  the consumer-visible contract.
- **Language level**: code in the core and Boot 2 modules must compile under the Java 8 toolchain
  (no `var`, records, switch expressions, text blocks). The Boot 3/4 starter modules may use Java
  17 constructs.
- **License header**: every Java file starts with the `Copyright © 2018 organization baomidou`
  header (Apache 2.0); copy it from a neighbouring file.
- Lombok is `compileOnly` + `annotationProcessor` and is used for boilerplate in properties/config
  classes; plain code is fine elsewhere.
- Comments and Javadoc are sparse and in Chinese in much of the core; match the style of the file
  you are editing rather than adding narration.

## 5. Before finishing a change

- Run `./gradlew build` (covers all modules and the three test suites). If you cannot — offline,
  wrong JDK, no GraalVM — state exactly what you did and did not run in the PR description.
- A change to `@DS` resolution, `DynamicDataSourceContextHolder` stack semantics, transaction
  modes, or datasource add/remove behaviour must keep the corresponding `v1`/`v3`/`v4` tests
  honest — extend them rather than only adding a new one.
- New/renamed configuration properties belong in `DynamicDataSourceProperties` and must be
  documented at https://doc.xiuceyun.cn (separate repository).
- Security findings are **not** public issues: see `SECURITY.md`. Code scanning alerts are triaged
  in the Security tab; fix them in code or dismiss them with a written justification — do not
  silence a scanner blanket-wide to make a number go down.

## 6. Negative tests must not re-create the weak pattern

A test asserting "the guard rejects X" still writes the offending literal into the source tree,
and **static analysers do not care about your intent**. Real damage that has already happened here
(see PR #800):

- `assertThatThrownBy(() -> genKeyPairBytes(512))` written to verify the new `MIN_KEY_SIZE` guard
  was itself what kept `java/insufficient-key-size` firing. Any integer literal below 2048 flowing
  into a key-size argument is a live alert, even inside a lambda nobody executes for real.
- The same trap applies to SQL string concatenation in injection tests, hardcoded credentials in
  "must reject local files" tests, and weak algorithms in "must refuse MD5" tests.

Rules:

1. Derive the bad value at runtime (`Integer.parseInt("512")`, a fixture file, a builder) so it is
   not a compile-time constant, or test the boundary through the public contract instead.
2. Say why in a comment, otherwise someone will "tidy" it back into a literal and reopen the alert.

## 7. Suppression comments are a hypothesis, CI is the verdict

`// codeql[rule-id]` and `# zizmor: ignore[rule-id]` are the correct way to record an accepted
risk, but they are not reliable enough to trust blindly: in the same PR the `java/rsa-without-oaep`
suppressions worked while `java/insufficient-key-size` silently did not. Likewise, zizmor ignore
comments must sit immediately above the offending **key** (`pull_request_target:`), not above
`on:` — placing them wrong produces no error, just an alert that never goes away.

So before declaring a scanning fix done, confirm against the actual run:

- `gh pr checks <n>` — `CodeQL`, `zizmor` and the `Test CI` matrix must all be green. `Analyze
  workflows` passing is **not** the same as there being zero alerts; the `CodeQL`/`zizmor` status
  checks are the ones that compare new alerts against the base.
- `zizmor` can be installed locally (`pip3 install zizmor`) and run offline against
  `.github/workflows/ .github/dependabot.yml`. Do it — it is much faster than a CI round trip.
- When the numbers do not move, **re-read the alert's own reported location**; do not keep editing
  code you assume is at fault.

A green local build proves nothing about any of this: the CodeQL check once failed for three
consecutive pushes while `./gradlew test` was green every time.
