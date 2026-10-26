# Kafka Span Exporter

将 OpenTelemetry Java SDK 的 Span 批量导出到 Apache Kafka。每次 `export(Collection<SpanData>)` 发送一条 Kafka 消息，其值是 **OTLP `ExportTraceServiceRequest` Protobuf 二进制**（可能包含多个 Trace）。

## 使用

```java
Properties config = new Properties();
config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
config.put(ProducerConfig.CLIENT_ID_CONFIG, "datafox-otel");

Producer<String, byte[]> producer =
    new KafkaProducer<>(config, new StringSerializer(), new ByteArraySerializer());

SpanExporter exporter = KafkaSpanExporter.create("otel-traces", producer);
BatchSpanProcessor processor = BatchSpanProcessor.builder(exporter).build();
```

其中使用标准 Kafka Producer 配置；认证、TLS、`acks`、`linger.ms`、`delivery.timeout.ms` 等参数均由 Kafka 自身管理。将 `processor` 注册到 OTel `SdkTracerProvider`，由 SDK 管理 Exporter 的生命周期。Producer 的所有权转移给 Exporter；其 `shutdown()` 将调用 Producer `close(Duration.ofSeconds(5))`。

为保护应用线程，推荐 `BatchSpanProcessor`，不推荐在业务线程上直接使用 `SimpleSpanProcessor`。Exporter 自身不创建线程池。Kafka Producer 的 `send()` 为异步回调，但发送前的序列化及 Kafka 元数据等待可能同步执行；`flush()` 也会同步等待 Kafka Producer 完成缓冲消息。

## 兼容性与数据格式

- 最低 Java 8（Gradle `--release 8`），Kafka Clients 固定为 **3.9.2**；Kafka 4.x 不再支持 Java 8。
- 基于 OpenTelemetry SDK **1.66.0**。使用上游 `TraceRequestMarshaler` 直接写出 OTLP Binary，不构造并重新解析中间 Protobuf 对象。
- `TraceRequestMarshaler` 属于 `io.opentelemetry.exporter.internal` **内部、不稳定 API**。升级 OTel SDK 时必须重新执行构建及兼容测试。
- Kafka Record 的 Key 为 `null`，不保证同一 Trace ID 的所有 Span 落在同一分区。
- 一个 `export()` 调用发送一个 Kafka Record；成功仅表示 Kafka Producer 回调成功（取决于 Producer `acks` 配置）。
- 目前没有额外的 Object Pool、重试队列或定制 Sender 线程池。背压交由 SDK BatchSpanProcessor 与 Kafka Producer 参数处理。
- 本模块只有 `SpanExporter` 能力，**没有 SDK 自动配置 SPI**。DataFox Agent 需要另行实现 `ConfigurableSpanExporterProvider`、Shadow 打包及 JVM 兼容测试。

## 测试

`./gradlew :kafka-exporter:test` 使用 Kafka `MockProducer` 验证异步成功/失败回调、OTLP Protobuf 格式、空批次、flush 和 shutdown；不依赖 Kafka Broker。

本阶段不含真实 Broker 集成测试，尚未验证 Agent 最终 JAR 的 ClassLoader 与运行时打包。
