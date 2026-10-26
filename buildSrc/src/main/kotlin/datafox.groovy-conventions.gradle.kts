plugins {
  groovy

  id("datafox.java-conventions")
  id("datafox.spotless-conventions")
}

tasks {
  withType<GroovyCompile> {
    sourceCompatibility = "8"
    targetCompatibility = "8"
  }
}
