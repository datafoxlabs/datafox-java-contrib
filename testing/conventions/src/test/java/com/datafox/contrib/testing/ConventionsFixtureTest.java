/*
 * Copyright The DataFox Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.contrib.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConventionsFixtureTest {
  @Test
  void resolvesSdkFromManagedBom() {
    assertEquals("SdkTracerProvider", ConventionsFixture.sdkTracerProviderName());
  }
}
