# 与 OpenTelemetry Java Contrib 的工程对齐

> 参考仓库：[opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib)
> 参考提交：[f2e17e8](https://github.com/open-telemetry/opentelemetry-java-contrib/commit/f2e17e8f336b2f7ddefb35ffb8b31ee537b32bce)（2026-10-26 读取）
> 目标仓库：`datafoxlabs/datafox-java-contrib`

## 1. 哪些内容进行了高保真移植？

| 上游结构 | DataFox 对应 | 保留的行为 |
|---|---|---|
| `buildSrc/build.gradle.kts` | 同路径 | Kotlin DSL 预编译插件、版本固定、Spotless Gradle 格式 |
| `otel.java-conventions` | `datafox.java-conventions` | Java Library、Toolchain、编译检查、JUnit、JaCoCo、Javadoc、源码与文档 JAR |
| `otel.errorprone-conventions` | `datafox.errorprone-conventions` | Error Prone、NullAway 的编译期校验 |
| `otel.spotless-conventions` | `datafox.spotless-conventions` | Google Java Format、Kotlin DSL ktlint、许可证与空格规范 |
| `otel.publish-conventions` | `datafox.publish-conventions` | Maven Publication、POM 元数据、可选 CI 签名 |
| `otel.japicmp-conventions` | `datafox.japicmp-conventions` | 稳定版 API Diff 检查流程 |
| `otel.animalsniffer-conventions` | `datafox.animalsniffer-conventions` | Java API 兼容性检查 |
| `otel.groovy-conventions` | `datafox.groovy-conventions` | Groovy 模块 Java 8 目标 |
| `dependencyManagement` | 同路径 | `java-platform`、BOM、版本约束 |
| `version.gradle.kts` | 同路径 | Stable/Alpha 分离与仓库独立版本号 |
| `.editorconfig`, `.gitignore` 等 | 同路径 | 一致的格式和基础配置 |
| `gradle/wrapper` | 同路径 | Gradle 9.8.1 及校验和 |

上述插件以官方实现为基础移植，修改了命名空间、Maven 坐标、版权模板和 DataFox 发布元数据，并保留了 Apache-2.0 原作者署名（见 `NOTICE`）。

## 2. 与上游有意不同的部分

**业务模块不复制。** 上游的 Kafka Exporter、云资源 Providers、JMX、JFR、Samplers 等已有实现，不应因为本仓库叫 Contrib 就整包 Fork。这里只搭建承载将来 DataFox 独立组件的**同类型工程基础设施**。

**不复制专属 Checks。** 上游 Error Prone 中的 `:custom-checks` 模块依赖上游私有检查逻辑，当前没有 DataFox 对应实现，因而移除了该 Project 依赖；保留公开的 Error Prone 和 NullAway。

**不复制 Weaver 生成任务。** 上游的 `otel.weaver-conventions` 服务于定义了 Weaver model 的组件。当前未引入任何 model，不需要 Docker-based Weaver 生命周期。

**不继承上游发布凭据和缓存服务。** Nexus / POM 指向 DataFox；Develocity Build Scan 显式 opt-in，避免向 OpenTelemetry 的远程缓存和组织项目写入数据。

**暂不发布空 Artifact。** 初始化时只有 `:dependencyManagement` 和不发布的 `:testing:conventions` 烟雾测试模块；等有真实可复用组件，再将业务模块纳入 `settings.gradle.kts` 并启用发布约定。

**API Diff 首次稳定版本前不可自动验证。** 从 `0.1.0` 开始定义基线；需在首次真实发布后更新基线版本及 API Diff 记录，避免把不存在的 Maven 发布版本视为现成历史记录。

## 3. 与 DataFox Java Agent 对齐

- Maven group：`com.datafox`
- Gradle：`9.8.1`
- OpenTelemetry SDK / Instrumentation BOM：`1.66.0` / `2.32.0-alpha`
- Java Toolchain：21，模块通过 `minJavaVersionSupported` 单独声明 API 最低版本。
- Contrib 可复用组件通过 Maven 依赖由发行版引用；Contrib 不包含 Java Agent 打包流程。

源参考：[DataFox Java Agent](https://github.com/datafoxlabs/datafox-java-agent)。

## 4. Gradle Wrapper

Gradle Wrapper 脚本、Gradle 9.8.1 发行包 SHA-256 配置以及官方生成的 `gradle-wrapper.jar` 均已提交。JAR 通过 GitHub Actions 在 Gradle 9.8.1 环境生成，生成提交：[25d145e](https://github.com/datafoxlabs/datafox-java-contrib/commit/25d145e86ba8daf19535afb5b72480a36f924c4d)。

CI 的独立 Wrapper 校验工作流使用 OpenTelemetry 上游相同的 `gradle/actions/wrapper-validation` 机制，正常构建不再临时生成 Wrapper。

## 5. 验证状态

- 已静态核对关键构建文件与上游主分支结构和插件 ID 的映射。
- [GitHub Actions Build #40254900592](https://github.com/datafoxlabs/datafox-java-contrib/actions/runs/40254900592)：Gradle Wrapper 生成、校验和、Spotless 和基础 Build 均通过。
- [GitHub Actions Build #40255324001](https://github.com/datafoxlabs/datafox-java-contrib/actions/runs/40255324001)：`:testing:conventions` 的 Java / Test 编译、Javadoc、JUnit 测试及 Spotless 全部通过。
- [Gradle Wrapper Validation #40255324000](https://github.com/datafoxlabs/datafox-java-contrib/actions/runs/40255324000)：官方 Wrapper 校验通过。
- 当前本地执行容器无法联网运行 Gradle，以上验证由 GitHub Actions 执行。
