val stableVersion = "0.1.0-SNAPSHOT"
val alphaVersion = "0.1.0-alpha-SNAPSHOT"
val apidiffBaselineVersion = "0.1.0"

extra["tagVersion"] = "v$stableVersion"

allprojects {
  group = "com.datafox"
  version = if (findProperty("datafox.stable") == "true") stableVersion else alphaVersion
  extra["apidiffBaselineVersion"] = apidiffBaselineVersion
}
