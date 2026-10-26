/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation.internal;

import com.datafox.contrib.propagation.LegacyUuidTracePropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

/** Registers the legacy trace propagator under the name {@code datafox-legacy}. */
public final class LegacyUuidTracePropagatorProvider implements ConfigurablePropagatorProvider {

  @Override
  public TextMapPropagator getPropagator(ConfigProperties config) {
    return LegacyUuidTracePropagator.getInstance();
  }

  @Override
  public String getName() {
    return "datafox-legacy";
  }
}
