/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datafox.contrib.propagation.internal.UuidTracePropagatorProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UuidTracePropagatorProviderTest {

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
  void registersAutoconfigureProvider() {
    boolean found = false;
    for (ConfigurablePropagatorProvider provider :
        ServiceLoader.load(ConfigurablePropagatorProvider.class)) {
      if ("datafox-uuid".equals(provider.getName())) {
        assertTrue(provider instanceof UuidTracePropagatorProvider);
        found = true;
      }
    }
    assertTrue(found);
  }

  @Test
  void usesConfiguredHeaderNames() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("datafox.propagation.uuid.trace-id-header"))
        .thenReturn("trace-identifier");
    when(config.getString("datafox.propagation.uuid.span-id-header"))
        .thenReturn("parent-identifier");

    TextMapPropagator propagator = new UuidTracePropagatorProvider().getPropagator(config);
    Map<String, String> headers = new HashMap<>();
    headers.put("trace-identifier", "550e8400-e29b-41d4-a716-446655440000");
    headers.put("parent-identifier", "0123456789abcdef");
    SpanContext extracted =
        Span.fromContext(propagator.extract(Context.root(), headers, GETTER)).getSpanContext();
    assertEquals("550e8400e29b41d4a716446655440000", extracted.getTraceId());
    assertEquals("0123456789abcdef", extracted.getSpanId());

    // Other names are not silently accepted.
    Map<String, String> otherHeaders = new HashMap<>();
    otherHeaders.put("X-Trace-Id", "550e8400e29b41d4a716446655440000");
    assertSame(Context.root(), propagator.extract(Context.root(), otherHeaders, GETTER));
  }

  @Test
  void rejectsMissingHeaderConfiguration() {
    ConfigProperties config = mock(ConfigProperties.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> new UuidTracePropagatorProvider().getPropagator(config));
  }
}
