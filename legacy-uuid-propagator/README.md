# Legacy UUID Trace Propagator

A DataFox OpenTelemetry Java propagator for legacy HTTP headers.

## Incoming headers

```http
X-CHJ-TraceId: 550e8400-e29b-41d4-a716-446655440000
X-CHJ-SpanId: 0123456789abcdef
```

The propagator accepts canonical UUIDs (36 characters) and the legacy unhyphenated 32-character hexadecimal Trace ID form. Both are normalized to OpenTelemetry's 128-bit lowercase Trace ID. Zero or malformed Trace IDs are ignored without changing the Context.

When `X-CHJ-SpanId` is missing or invalid, the **first 16 hexadecimal characters** of the normalized trace ID are used as the synthetic remote parent Span ID, retaining the behavior of the historical `LiAutoClientPropagator`. If this synthetic ID is all zero, the legacy context is ignored instead. A derived ID is **not** the genuine historical parent span; trace-tree continuity cannot be reconstructed without the upstream Span ID.

The legacy protocol does not have a sampling field. For compatibility, extracted contexts are marked sampled. Restrict these headers to trusted upstream sources: accepting arbitrary external Trace IDs and force-sampled flags can increase telemetry volume.

## Autoconfigure

This module registers `ConfigurablePropagatorProvider` under the name `datafox-legacy`.

For a compatible SDK or Java Agent distribution that includes this module in the correct classloader, configure:

```properties
otel.propagators=tracecontext,baggage,datafox-legacy
```

The legacy propagator is **extract-only**. It neither injects `X-CHJ-*` headers nor disables normal W3C propagation by other propagators. The historical `SkipPropagationContextKey` was not found in upstream OpenTelemetry Java 1.66.0 public API and is intentionally not copied. If suppressing outgoing W3C `traceparent` after legacy extraction is a required protocol guarantee, that needs an explicit separately evaluated integration. Order-dependent priority when both legacy and W3C headers are present is not a portable contract of Java's composite propagator; do not assume configuration order guarantees precedence.

## Integration

Published module coordinate (after a DataFox release): `com.datafox:datafox-legacy-uuid-propagator:<version>`.

DataFox Java Agent requires a separate packaging change to include this module. Merely publishing this library does not make the new propagator available inside the Agent. No SDK forks or custom Trace ID generators are needed.

References: [OpenTelemetry Context propagation specification](https://opentelemetry.io/docs/specs/otel/context/api-propagators/), [OpenTelemetry Java TextMapPropagator](https://github.com/open-telemetry/opentelemetry-java/blob/v1.66.0/context/src/main/java/io/opentelemetry/context/propagation/TextMapPropagator.java).
