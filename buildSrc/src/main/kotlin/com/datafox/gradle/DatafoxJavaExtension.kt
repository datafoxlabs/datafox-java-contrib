/*
 * Copyright The DataFox Authors
 * Adapted from OpenTelemetry Java Contrib
 * SPDX-License-Identifier: Apache-2.0
 */

package com.datafox.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.provider.Property

abstract class DatafoxJavaExtension {
  abstract val moduleName: Property<String>

  abstract val minJavaVersionSupported: Property<JavaVersion>

  abstract val maxJavaVersionForTests: Property<JavaVersion>

  init {
    minJavaVersionSupported.convention(JavaVersion.VERSION_1_8)
  }
}
