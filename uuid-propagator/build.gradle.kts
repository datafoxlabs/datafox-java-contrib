plugins {
  id("datafox.java-conventions")
  id("datafox.publish-conventions")
}

description = "UUID trace context propagation for DataFox"

datafoxJava {
  moduleName.set("com.datafox.contrib.propagation")
  minJavaVersionSupported.set(JavaVersion.VERSION_1_8)
}

dependencies {
  api("io.opentelemetry:opentelemetry-api")
  compileOnly("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi")

  testImplementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
}
