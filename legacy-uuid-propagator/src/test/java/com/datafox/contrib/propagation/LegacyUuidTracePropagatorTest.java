/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LegacyUuidTracePropagatorTest {

  private static final String UUID = "550e8400-e29b-41d4-a716-446655440000";
  private static final String TRACE_ID = "550e8400e29b41d4a716446655440000";
  private static final String PARENT_ID = "0123456789abcdef";
  private static final TextMapPropagator PROPAGATOR = LegacyUuidTracePropagator.getInstance();

  private static final TextMapGetter<Map<String, String>> GETTER =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Set<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @Test
  void extractsCanonicalUuidAndParentSpanId() {
    SpanContext parent = extract(headers(UUID, PARENT_ID));
    assertEquals(TRACE_ID, parent.getTraceId());
    assertEquals(PARENT_ID, parent.getSpanId());
    assertTrue(parent.isRemote());
    assertTrue(parent.isSampled());
  }

  @Test
  void extractsLegacyUnhyphenatedTraceId() {
    SpanContext parent = extract(headers(TRACE_ID, PARENT_ID));
    assertEquals(TRACE_ID, parent.getTraceId());
    assertEquals(PARENT_ID, parent.getSpanId());
  }

  @Test
  void normalizesUppercaseIds() {
    SpanContext parent =
        extract(headers("550E8400-E29B-41D4-A716-446655440000", "ABCDEF1234567890"));
    assertEquals(TRACE_ID, parent.getTraceId());
    assertEquals("abcdef1234567890", parent.getSpanId());
  }

  @Test
  void usesHistoricalParentFallbackWhenMissing() {
    SpanContext parent = extract(headers(UUID, null));
    assertEquals("550e8400e29b41d4", parent.getSpanId());
  }

  @Test
  void usesHistoricalParentFallbackWhenInvalid() {
    SpanContext parent = extract(headers(UUID, "invalid"));
    assertEquals("550e8400e29b41d4", parent.getSpanId());
  }

  @Test
  void rejectsMalformedOrNilTraceIdsWithoutThrowing() {
    String[] values = {
      "",
      "1",
      "550e8400-e29b-41d4-a716-44665544000",
      "550e8400_e29b_41d4_a716_446655440000",
      "550e8400-e29b-41d4-a716-44665544000g",
      "00000000-0000-0000-0000-000000000000"
    };
    for (String value : values) {
      Map<String, String> carrier = headers(value, PARENT_ID);
      Context root = Context.root();
      assertSame(root, PROPAGATOR.extract(root, carrier, GETTER));
    }
  }

  @Test
  void rejectsInvalidSyntheticParent() {
    String traceId = "0000000000000000a716446655440000";
    Context root = Context.root();
    assertSame(root, PROPAGATOR.extract(root, headers(traceId, null), GETTER));
  }

  @Test
  void keepsExistingSpanContext() {
    SpanContext valid =
        SpanContext.createFromRemoteParent(
            "00112233445566778899aabbccddeeff",
            "1122334455667788",
            TraceFlags.getDefault(),
            TraceState.getDefault());
    Context context = Context.root().with(Span.wrap(valid));
    Context extracted = PROPAGATOR.extract(context, headers(UUID, PARENT_ID), GETTER);
    assertSame(context, extracted);
    assertEquals(valid, Span.fromContext(extracted).getSpanContext());
  }

  @Test
  void ignoresAbsentLegacyHeader() {
    Context context = Context.root();
    assertSame(context, PROPAGATOR.extract(context, Collections.emptyMap(), GETTER));
  }

  @Test
  void doesNotInjectLegacyHeaders() {
    Map<String, String> headers = new HashMap<>();
    TextMapSetter<Map<String, String>> setter = Map::put;
    PROPAGATOR.inject(
        Context.root().with(Span.wrap(extract(headers(UUID, PARENT_ID)))), headers, setter);
    assertTrue(headers.isEmpty());
    assertTrue(PROPAGATOR.fields().isEmpty());
  }

  private static SpanContext extract(Map<String, String> headers) {
    return Span.fromContext(PROPAGATOR.extract(Context.root(), headers, GETTER)).getSpanContext();
  }

  private static Map<String, String> headers(String traceId, String spanId) {
    Map<String, String> result = new HashMap<>();
    result.put("X-CHJ-TraceId", traceId);
    if (spanId != null) {
      result.put("X-CHJ-SpanId", spanId);
    }
    return result;
  }
}
