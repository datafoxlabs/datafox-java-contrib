pluginManagement {
  plugins {
    id("com.gradle.develocity") version "4.6.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
  }
}

plugins {
  id("com.gradle.develocity")
  id("org.gradle.toolchains.foojay-resolver-convention")
}

// Build scans are opt-in. DataFox must not publish build metadata under upstream credentials.
develocity {
  buildScan {
    publishing.onlyIf { System.getenv("DATAFOX_BUILD_SCAN_CONSENT") == "yes" }
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = if (System.getenv("DATAFOX_BUILD_SCAN_CONSENT") == "yes") "yes" else "no"
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    if (providers.gradleProperty("useLocalMaven").isPresent) {
      mavenLocal()
    }
    maven {
      url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
  }
}

rootProject.name = "datafox-java-contrib"

include(":dependencyManagement")
include(":testing:conventions")
include(":uuid-propagator")
