plugins {
  id("datafox.java-conventions")
  id("datafox.publish-conventions")
}

description = "OTLP Protobuf trace exporter for Apache Kafka"

datafoxJava {
  moduleName.set("com.datafox.contrib.kafka")
  minJavaVersionSupported.set(JavaVersion.VERSION_1_8)
}

dependencies {
  api("io.opentelemetry:opentelemetry-sdk-trace")
  api("org.apache.kafka:kafka-clients")

  // Uses upstream OTLP Protobuf marshaling without rebuilding the Protobuf object tree.
  implementation("io.opentelemetry:opentelemetry-exporter-otlp-common")

  testImplementation("io.opentelemetry:opentelemetry-sdk-testing")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
}
