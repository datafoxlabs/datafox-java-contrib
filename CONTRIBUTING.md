# Contributing to DataFox Java Contrib

本仓库基于 [OpenTelemetry Java Contrib](https://github.com/open-telemetry/opentelemetry-java-contrib) 的工程约定，贡献代码前应首先确认上游是否已经提供同样能力。复用 OTel 原生 API、SDK、Exporter 和 SPI，尽量避免 Fork 实现。

## 添加组件

一个子模块应该代表可独立复用的实际功能。新增模块时：

1. 在 `settings.gradle.kts` 注册子项目。
2. 新建模块 `build.gradle.kts`，使用 `datafox.java-conventions`。只有需要发布时才使用 `datafox.publish-conventions`。
3. 配置 `datafoxJava.moduleName` 与 `minJavaVersionSupported`（默认 Java 8）。若依赖 Java 11+ API，应显式提高最小版本。
4. 通过 `:dependencyManagement` 中的约束管理依赖版本；不要在每个模块重复写版本。
5. 添加正常路径及有意义的失败路径测试，并记录与 DataFox Java Agent 的版本兼容边界。

公开 API 应有英文 Javadoc；代码遵循 Google Java Format。对于改动的 Gradle Kotlin DSL，优先沿用上游插件的组织方式。

## 本地验证

使用 JDK 21 及 Gradle Wrapper：

```shell
./gradlew spotlessApply
./gradlew spotlessCheck build
```

兼容性检查（发布稳定组件后）使用 `jApiCmp`。对尚未首次发布的组件，不得伪造历史 API 基线。

## 代码审查原则

- 不复制上游模块或重新实现已有 OTel 特性。
- 不引入无用的工厂、注册表、配置层或额外线程。
- 不泄露 Maven Central、Kafka、Observability 后端的认证信息。
- 对 ClassLoader、JVM 兼容性、SDK 生命周期有影响时，添加针对性测试。
- Gradle 或插件版本升级应对照上游版本并运行全量构建。

复制或修改上游代码时，保留 SPDX / 原作者归属信息，并更新 `NOTICE`。

## 交付

PR 描述应明确：功能边界、采用的上游 API、验证方式及结果、兼容影响。不宣称未运行的构建或测试已通过。
