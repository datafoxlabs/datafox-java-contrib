# Publishing DataFox Java Contrib modules

项目沿用 OpenTelemetry Java Contrib 的 Maven Publish、Nexus Publishing 与 Signing 约定，但不复制上游发布流程或凭据。

当前版本由 `version.gradle.kts` 管理，默认为 `0.1.0-alpha-SNAPSHOT`。设置 `-Pdatafox.stable=true` 可切换到配置的稳定版本（当前为 `0.1.0-SNAPSHOT`，仍属 SNAPSHOT）。

## 前置条件

- 至少有一个实际组件应用 `datafox.publish-conventions`，并配置 `description` 和 `datafoxJava.moduleName`。
- 组件与 DataFox Agent 使用兼容的 OpenTelemetry BOM。
- 已完成构建、格式检查、测试；稳定版还应检查相对上一个已发布版本的 API Diff。
- Maven Central 命名空间 `com.datafox` 的发布所有权与凭据已验证。

## 发布所需环境变量

`SONATYPE_USER`、`SONATYPE_KEY` 用于 Nexus 发布；CI 中提供 `GPG_PRIVATE_KEY` 和 `GPG_PASSWORD` 时启用 PGP 签名。不要提交这些值到仓库。

发布前先使用 `./gradlew publishToMavenLocal` 检查生成的 Jar、sources、Javadoc 与 POM。当前仓库尚未启用自动发布工作流，所有远程发布均需专门审查及明确触发。

## 发布边界

发布 Contrib Library 与打包 Java Agent 是独立生命周期：Contrib 构件经 Maven 版本依赖被 `datafox-java-agent` 引用；不要在 Contrib 发布流程中 Shadow 或重新构建上游 Java Agent。
