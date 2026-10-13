# Security Policy / 安全策略

## Supported Versions / 受支持版本

security fixes are only applied to the latest release line; older lines are community-maintained on a best-effort basis.
安全修复仅覆盖最新 released 版本线，历史版本线不做承诺（如有强需求请在本仓库开 issue 说明场景）。

| Version | Spring Boot | JDK | Supported |
|:-:|:-:|:-:|:--|
| 4.5.x | 2.x / 3.x / 4.x | 8 / 17+ | :white_check_mark: |
| 4.3.x and below | 2.x / 3.x | 8 / 17+ | :x: |
| 3.x and below | 1.5.x / 2.x | 7 / 8 | :x: |

## Reporting a Vulnerability / 报告漏洞

**Please do NOT open a public issue for security vulnerabilities.**
**请勿为安全漏洞直接开公开 issue**（公开 issue 会让使用者在修复发布前先暴露于风险）。

Use one of the following channels, in preference order:

1. **GitHub Private Vulnerability Reporting** — the "Report a vulnerability" form on the
   [Security tab](https://github.com/baomidou/dynamic-datasource/security/advisories/new) of this repository.
2. **Email** to the maintainer (`tracy5546@gmail.com`), with `dynamic-datasource` in the subject line.

Please include, as accurately as you can:

- the affected artifact and version (e.g. `com.baomidou:dynamic-datasource-spring-boot3-starter:4.3.1`);
- a minimal reproducer, or the code/config path that makes the issue reachable;
- the impact (what an attacker gains, and under which configuration);
- any suggested fix or prior art (CVE, upstream issue).

### What to expect / 处理约定（目标值，非合同型 SLA）

Best-effort targets; this is a volunteer-maintained project, so the numbers are indicative and may be
adjusted by the maintainers.
目标时限，本项目由志愿者维护，以下数字仅为参考约定。

| Stage | Target |
|:--|:--|
| Acknowledgement of the report | within 5 business days |
| Confirmation + severity assessment | within 14 days, may involve a private draft advisory |
| Fix release + advisory publication | as soon as practical; the advisory credits the reporter unless anonymity is requested |

A GitHub Security Advisory with a CVE request will be published together with the fixed release, so
downstream users get an actionable notification rather than a bare commit.

## Scope notes / 边界说明

This library executes expressions and configuration that come from **your application's own
configuration and code**:

- SpEL used in `@DS("...")` values and dynamic datasource switching is resolved from annotations
  written by the application author; passing *untrusted external input* into a `@DS` value or into
  `DynamicDataSourceContextHolder.push(...)` is an application-level design decision.
- `ENC(...)` protects datasource credentials at rest in configuration files; it is not a
  substitute for a real secret manager, and the encryption key material lives with the
  application configuration.
- Datasource drivers, connection pools (Druid/HikariCP/…) and the database itself are separate
  components; vulnerabilities in those belong to their own projects.

Reports of the form "the application can be attacked because it feeds user input into X" are
still welcome — but please describe the concrete configuration path we could harden, otherwise
the report will be closed as informational.

## Hardening already applied / 已加固项（供参考）

- SpEL evaluation in `@DS` was hardened against expression injection (see commit `273fced`,
  SpEL injection / potential RCE in `DsSpelExpressionProcessor`).
- Build-time signing credentials are no longer kept in tracked files; publishing runs with
  in-memory keys supplied by CI secrets.
