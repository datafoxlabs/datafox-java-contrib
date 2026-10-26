/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertSame(LegacyUuidTracePropagator.getInstance(), provider.getPropagator(null));
        found = true;
      }
    }
    assertTrue(found);
  }
}
