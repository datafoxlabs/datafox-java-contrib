/*
 * Copyright The OpenTelemetry Authors
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
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Extracts legacy trace context from the X-CHJ-TraceId and X-CHJ-SpanId headers.
 *
 * <p>Accepts 32-character hexadecimal trace IDs and canonical 36-character UUIDs. If the span ID
 * is absent or invalid, it uses the first 16 hexadecimal characters of the trace ID as a synthetic
 * remote parent ID, preserving the legacy protocol behavior. It does not inject headers.
 */
public final class LegacyUuidTracePropagator implements TextMapPropagator {

  private static final String TRACE_ID_HEADER = "X-CHJ-TraceId";
  private static final String SPAN_ID_HEADER = "X-CHJ-SpanId";

  private static final LegacyUuidTracePropagator INSTANCE = new LegacyUuidTracePropagator();

  private LegacyUuidTracePropagator() {}

  /** Returns the singleton propagator instance. */
  public static LegacyUuidTracePropagator getInstance() {
    return INSTANCE;
  }

  @Override
  public Collection<String> fields() {
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

    String traceId = normalizeTraceId(getter.get(carrier, TRACE_ID_HEADER));
    if (traceId == null) {
      return context;
    }

    String spanId = getter.get(carrier, SPAN_ID_HEADER);
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
