/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datafox.contrib.propagation.internal.LegacyUuidTracePropagatorProvider;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class LegacyUuidTracePropagatorProviderTest {

  @Test
  void registersAutoconfigureProvider() {
    boolean found = false;
    for (ConfigurablePropagatorProvider provider :
        ServiceLoader.load(ConfigurablePropagatorProvider.class)) {
      if ("datafox-legacy".equals(provider.getName())) {
        assertEquals("datafox-legacy", provider.getName());
        found = true;
      }
    }
    assertTrue(found);
  }

  @Test
  void readsHeaderNamesFromConfig() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("datafox.propagation.legacy.trace-id-header"))
        .thenReturn("trace-identifier");
    when(config.getString("datafox.propagation.legacy.span-id-header"))
        .thenReturn("parent-identifier");
    TextMapPropagator propagator = new LegacyUuidTracePropagatorProvider().getPropagator(config);
    assertTrue(propagator instanceof LegacyUuidTracePropagator);
  }

  @Test
  void rejectsMissingHeaderConfiguration() {
    ConfigProperties config = mock(ConfigProperties.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> new LegacyUuidTracePropagatorProvider().getPropagator(config));
  }
}
