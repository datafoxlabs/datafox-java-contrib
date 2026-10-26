# DataFox Java Contrib

面向 OpenTelemetry Java API / SDK 的 DataFox 扩展组件库。工程结构与 Gradle 约定参考 [OpenTelemetry Java Contrib](https://github.com/open-telemetry/opentelemetry-java-contrib)。

本仓库负责可独立复用、测试和发布的 Java 组件；Java Agent 发行版的启动、Shadow 打包、Bootstrap 和 Instrumentation 集成属于 [datafox-java-agent](https://github.com/datafoxlabs/datafox-java-agent)。

## 工程结构

```text
datafox-java-contrib/
├── buildSrc/                 # 预编译 Gradle 约定插件
├── dependencyManagement/     # Java Platform：集中管理依赖版本
├── uuid-propagator/    # UUID Trace Context 兼容
├── testing/conventions/      # 构建约定和 BOM 的烟雾测试（不发布）
├── buildscripts/             # Spotless 许可证模板
├── .github/workflows/        # 构建、格式检查和 Wrapper 验证
├── build.gradle.kts          # 项目构建入口
├── settings.gradle.kts       # 插件和依赖仓库、项目模块
├── version.gradle.kts        # 稳定版 / Alpha 版本
└── gradle/                   # Gradle Wrapper 配置
```

与上游一致，**业务组件作为独立 Gradle 子项目维护**，各模块通过约定插件获得统一的 Java toolchain、静态检查、依赖约束、测试、源代码包和发布元数据。目前包含一个 UUID Trace Context 兼容组件，其他能力按实际需求新增独立模块。

## 版本与约定

- Gradle：9.8.1；Java 编译 toolchain：21，按模块配置目标最低版本（默认 Java 8）。
- OpenTelemetry SDK：1.66.0；Instrumentation BOM Alpha：2.32.0-alpha，与现有 DataFox Java Agent 对齐。
- Maven group：`com.datafox`；组件默认 artifact 命名：`datafox-<module>`。
- 开发版本：`0.1.0-alpha-SNAPSHOT`；通过 `-Pdatafox.stable=true` 选择稳定版本。
- 静态检查：Spotless / Google Java Format、Error Prone、NullAway；测试：JUnit Jupiter。
- 公开组件可通过 `datafox.publish-conventions` 配置 Maven Publish / POM / CI 条件签名，**默认不上传远程仓库**。

新增组件时在 `settings.gradle.kts` 中 `include(":your-module")`，并创建相应的 `your-module/build.gradle.kts`：

```kotlin
plugins {
  id("datafox.java-conventions")
  id("datafox.publish-conventions")
}

description = "Description of the DataFox component"

datafoxJava {
  moduleName.set("com.datafox.contrib.example")
  minJavaVersionSupported.set(JavaVersion.VERSION_1_8)
}

dependencies {
  api("io.opentelemetry:opentelemetry-sdk-trace")
}
```

## 已实现的组件

- [uuid-propagator](uuid-propagator/README.md)：通过可配置 Header 读取 UUID Trace ID 和父 Span ID，创建远程父 Context。通过 `datafox-uuid` 注册 SDK SPI；尚未纳入 DataFox Java Agent 打包。

## 本地构建

需要 JDK 17+ 运行 Gradle；建议使用 JDK 21 或更新版本。

```shell
./gradlew build
./gradlew spotlessCheck
```

Gradle Wrapper 已通过官方 Gradle 9.8.1 生成，包含 `gradle-wrapper.jar` 和校验和配置。CI 执行 `spotlessCheck`、`build`，并独立校验 Wrapper。

## 工程原则

1. 优先复用 OTel Java / Java Contrib 已有 API 与组件，不复制上游业务实现。
2. 每个组件独立维护源代码、测试和发布坐标；不为假设能力建立空模块。
3. 测试通过、依赖一致和版本兼容后再引入生产级扩展功能。
4. OpenTelemetry 源码与 Gradle 约定的借鉴来源见 [工程溯源记录](docs/upstream-alignment.md)。

许可证：[Apache License 2.0](LICENSE)。
