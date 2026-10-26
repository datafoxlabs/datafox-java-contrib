/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Extracts legacy trace context from configurable trace ID and span ID headers.
 *
 * <p>Accepts 32-character hexadecimal trace IDs and canonical 36-character UUIDs. If the span ID is
 * absent or invalid, it uses the first 16 hexadecimal characters of the trace ID as a synthetic
 * remote parent ID, preserving the legacy protocol behavior. It does not inject headers.
 */
public final class LegacyUuidTracePropagator implements TextMapPropagator {

  private final String traceIdHeader;
  private final String spanIdHeader;

  private LegacyUuidTracePropagator(String traceIdHeader, String spanIdHeader) {
    this.traceIdHeader = traceIdHeader;
    this.spanIdHeader = spanIdHeader;
  }

  /** Creates a propagator with the specified legacy header names. */
  public static LegacyUuidTracePropagator create(String traceIdHeader, String spanIdHeader) {
    if (traceIdHeader == null
        || traceIdHeader.trim().isEmpty()
        || spanIdHeader == null
        || spanIdHeader.trim().isEmpty()) {
      throw new IllegalArgumentException("Legacy trace and span ID header names must be set");
    }
    return new LegacyUuidTracePropagator(traceIdHeader, spanIdHeader);
  }

  @Override
  public List<String> fields() {
    // Legacy headers are extracted only; they are never injected.
    return Collections.emptyList();
  }

  @Override
  public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {}

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
    if (Span.fromContext(context).getSpanContext().isValid()) {
      return context;
    }

    String traceId = normalizeTraceId(getter.get(carrier, traceIdHeader));
    if (traceId == null) {
      return context;
    }

    String spanId = getter.get(carrier, spanIdHeader);
    if (spanId != null) {
      spanId = spanId.toLowerCase(Locale.ROOT);
    }
    if (!SpanId.isValid(spanId)) {
      spanId = traceId.substring(0, SpanId.getLength());
    }
    if (!SpanId.isValid(spanId)) {
      return context;
    }

    SpanContext parent =
        SpanContext.createFromRemoteParent(
            traceId, spanId, TraceFlags.getSampled(), TraceState.getDefault());
    return context.with(Span.wrap(parent));
  }

  @Nullable
  private static String normalizeTraceId(@Nullable String value) {
    if (value == null) {
      return null;
    }

    if (value.length() == TraceId.getLength()) {
      String normalized = value.toLowerCase(Locale.ROOT);
      return TraceId.isValid(normalized) ? normalized : null;
    }

    if (value.length() != 36
        || value.charAt(8) != '-'
        || value.charAt(13) != '-'
        || value.charAt(18) != '-'
        || value.charAt(23) != '-') {
      return null;
    }

    StringBuilder normalized = new StringBuilder(TraceId.getLength());
    for (int i = 0; i < value.length(); i++) {
      if (i != 8 && i != 13 && i != 18 && i != 23) {
        normalized.append(value.charAt(i));
      }
    }
    String traceId = normalized.toString().toLowerCase(Locale.ROOT);
    return TraceId.isValid(traceId) ? traceId : null;
  }
}
