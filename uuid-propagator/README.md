# UUID Trace Propagator

为使用 UUID 作为 Trace ID 的现有系统提供 OpenTelemetry Trace Context 兼容能力。

应用收到 HTTP 请求后，本组件从**可配置的 Header** 中读取 Trace ID 和 Span ID，创建远程父 `SpanContext`，使 OpenTelemetry 新建的 Span 能延续传入的 Trace ID。

## 使用方式

包含模块并启用 SDK 自动配置后，使用：

```properties
otel.propagators=tracecontext,baggage,datafox-uuid

datafox.propagation.uuid.trace-id-header=X-Request-Trace-Id
datafox.propagation.uuid.span-id-header=X-Request-Span-Id
```

以上 Header 名称只是示例，不是默认值。**启用本传播器时两个配置必须提供**；缺失或空白会导致初始化失败。未启用 `datafox-uuid` 时无需配置。

示例请求：

```http
X-Request-Trace-Id: 550e8400-e29b-41d4-a716-446655440000
X-Request-Span-Id: 0123456789abcdef
```

将被转换为：

- Trace ID：`550e8400e29b41d4a716446655440000`（128 bit）
- Remote Parent Span ID：`0123456789abcdef`（64 bit）
- TraceFlags：`SAMPLED`（沿用先前协议的采样行为）

同时支持没有连字符的 32 字符十六进制 Trace ID；大小写统一为小写。非法格式、全零 Trace ID 会被忽略，不影响请求执行。

## 与普通 OpenTelemetry 传播的关系

该传播器**只提取，不注入**自定义 Header。如果传入的 Context 已存在有效 SpanContext，也不会覆盖它。

当 Span ID 缺失或格式非法时，为了与既有协议兼容，会使用规范化 Trace ID 的前 16 个十六进制字符作为**合成父 Span ID**。这不等同于真实上游父 Span，无法恢复缺失的历史 Span 树；如果合成值全零，则不提取该上下文。

传入的协议没有采样标志，因此继续将提取的远程父 Context 标记为 `SAMPLED`。只应对可信任上游接受这些 Header，以免外部请求任意设置 Trace ID 并扩大采样量。

启用 `tracecontext` 时，其他 W3C 传播器仍可以向下游写入 `traceparent`。复合传播器不承诺多种协议之间的执行顺序，**不要依赖 `otel.propagators` 中的排列顺序实现 W3C / UUID 优先级**。本模块不复制旧代码依赖的非标准 `SkipPropagationContextKey`。

## 发布和集成

- Gradle 模块：`:uuid-propagator`
- Maven Artifact（发布后）：`com.datafox:datafox-uuid-propagator:<version>`
- Java API：`com.datafox.contrib.propagation.UuidTracePropagator`
- SDK SPI：`ConfigurablePropagatorProvider`，名称 `datafox-uuid`
- 需要在 DataFox Java Agent 中额外打包和注册该组件；目前这个仓库的实现不代表 Agent 已集成。

参考：[OpenTelemetry TextMapPropagator](https://github.com/open-telemetry/opentelemetry-java/blob/v1.66.0/context/src/main/java/io/opentelemetry/context/propagation/TextMapPropagator.java) · [OTel Context Propagators](https://opentelemetry.io/docs/specs/otel/context/api-propagators/)
