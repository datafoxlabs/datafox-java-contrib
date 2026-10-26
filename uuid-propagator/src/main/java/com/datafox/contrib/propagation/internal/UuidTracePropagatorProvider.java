/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.propagation.internal;

import com.datafox.contrib.propagation.UuidTracePropagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

/** Provides UUID-based propagation through the SDK's auto-configuration SPI. */
public final class UuidTracePropagatorProvider implements ConfigurablePropagatorProvider {

  @Override
  public TextMapPropagator getPropagator(ConfigProperties config) {
    return UuidTracePropagator.create(
        config.getString("datafox.propagation.uuid.trace-id-header"),
        config.getString("datafox.propagation.uuid.span-id-header"));
  }

  @Override
  public String getName() {
    return "datafox-uuid";
  }
}
