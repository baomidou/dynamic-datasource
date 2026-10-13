# 支持策略

本文件说明 dynamic-datasource 三条 Spring Boot 产线的维护状态、变更准入与支持渠道。

## 产线状态

| 产线 | Starter 构件 | 上游状态 | 本项目默认准入 |
| --- | --- | --- | --- |
| Spring Boot 1.5.x ~ 2.x.x | `dynamic-datasource-spring-boot-starter` | 上游 OSS 支持已结束 | **冻结**：仅关键缺陷与安全修复 |
| Spring Boot 3.x | `dynamic-datasource-spring-boot3-starter` | 3.5 为 3.x 最后一个特性版本，OSS 支持窗口已结束 | **维护**：关键缺陷与安全修复 + 低成本回移 |
| Spring Boot 4.x | `dynamic-datasource-spring-boot4-starter` | 当前主线 | **主线**：承载全部变更，新特性首先落地 |

三条产线共享同一份核心逻辑（`dynamic-datasource-creator` / `-spring` / `-spring-boot-common`），
差异仅在 starter 层的自动装配与 `javax` / `jakarta` 适配。

## 变更准入原则

1. **关键缺陷与安全修复**（数据正确性、事务语义、native 兼容等）：所有受影响产线同步发布。
2. **与运行时代际无关的改进**（文档、构建、测试、CI）：所有产线同步生效。
3. **新特性、新配置项**：默认只进主线（Boot 4）；是否回移旧产线按移植成本个案判断，
   不接受"配置项存在但行为不一致"的半支持状态。
4. 产线冻结不会删除任何已发布构件，历史版本在 Maven Central 永久可用。

## 获取支持

- **问题与缺陷**：使用 [Issue 模板](https://github.com/baomidou/dynamic-datasource/issues/new/choose) 提交，附最小复现与版本信息。
- **使用咨询**：QQ 群（见 [Wiki 首页](https://github.com/baomidou/dynamic-datasource/wiki/Home)）。
- **安全问题**：请勿开公开 Issue，按 [SECURITY.md](./SECURITY.md) 流程联系维护者。
- **文档**：优先查 [Wiki](https://github.com/baomidou/dynamic-datasource/wiki/Home) 与 <https://doc.xiuceyun.cn>。

## 版本节奏

- 补丁版本（`x.y.z`）：缺陷修复随时发布。
- 次版本（`x.y`）：承载新特性与新配置项。
