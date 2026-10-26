val stableVersion = "0.1.0-SNAPSHOT"
val alphaVersion = "0.1.0-alpha-SNAPSHOT"
val apidiffBaselineVersion = "0.1.0"

val releaseVersion = providers.gradleProperty("releaseVersion").orNull
if (releaseVersion != null &&
  !Regex("""[0-9]+\.[0-9]+\.[0-9]+(?:-[0-9A-Za-z.]+)?""").matches(releaseVersion)) {
  throw GradleException("Invalid releaseVersion: $releaseVersion")
}

extra["tagVersion"] = "v${releaseVersion ?: stableVersion}"

allprojects {
  group = "com.datafox"
  version = releaseVersion ?: if (findProperty("datafox.stable") == "true") stableVersion else alphaVersion
  extra["apidiffBaselineVersion"] = apidiffBaselineVersion
}
