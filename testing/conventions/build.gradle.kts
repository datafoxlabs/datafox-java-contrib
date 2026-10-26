plugins {
  id("datafox.java-conventions")
}

description = "Smoke-test project for the DataFox Java build conventions (not published)"

datafoxJava {
  moduleName.set("com.datafox.contrib.testing")
  minJavaVersionSupported.set(JavaVersion.VERSION_1_8)
}

dependencies {
  implementation("io.opentelemetry:opentelemetry-sdk-trace")
}
