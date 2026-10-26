/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.testing;

import io.opentelemetry.sdk.trace.SdkTracerProvider;

/** Internal fixture used to verify the Java build conventions. */
public final class ConventionsFixture {
  private ConventionsFixture() {}

  public static String sdkTracerProviderName() {
    return SdkTracerProvider.class.getSimpleName();
  }
}
