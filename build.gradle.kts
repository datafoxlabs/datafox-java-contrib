import java.time.Duration

plugins {
  id("datafox.spotless-conventions")
  id("idea")
  id("io.github.gradle-nexus.publish-plugin")
}

apply(from = "version.gradle.kts")

description = "DataFox contributions and extensions for OpenTelemetry Java"

nexusPublishing {
  packageGroup.set("com.datafox")
  repositories {
    sonatype {
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
      username.set(System.getenv("SONATYPE_USER"))
      password.set(System.getenv("SONATYPE_KEY"))
    }
  }
  connectTimeout.set(Duration.ofMinutes(5))
  clientTimeout.set(Duration.ofMinutes(5))
}

if (project.findProperty("skipTests") as String? == "true") {
  subprojects {
    tasks.withType<Test>().configureEach {
      enabled = false
    }
  }
}
