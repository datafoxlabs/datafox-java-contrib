# Publishing DataFox Java Contrib

Contrib libraries are published before DataFox Java Agent. The release workflow is
[Publish DataFox Java Contrib](.github/workflows/publish-maven.yml).

## Versioning

Development builds remain `0.1.0-alpha-SNAPSHOT`. A release tag `v0.1.0-alpha.1`
corresponds to Maven version `0.1.0-alpha.1`. The release version is supplied to
Gradle as `-PreleaseVersion=0.1.0-alpha.1`, and does not alter the default
development version.

The first release contains two independent Maven artifacts:

- `com.datafox:datafox-kafka-exporter:0.1.0-alpha.1`
- `com.datafox:datafox-uuid-propagator:0.1.0-alpha.1`

Both have JAR, sources, Javadoc, POM and Gradle module metadata.

## Release procedure

1. Confirm `main` passes Gradle build, Spotless and Java 8 compatibility tests.
2. Verify ownership of the Maven Central namespace `com.datafox` in Sonatype Central Portal.
3. Configure the GitHub Actions environment `maven-central` with `SONATYPE_USER`,
   `SONATYPE_KEY`, `GPG_PRIVATE_KEY`, and `GPG_PASSWORD`. Configure environment
   protection / required reviewers to prevent accidental releases.
4. Create an immutable annotated tag `v0.1.0-alpha.1` on the tested commit and push it.
   A tag push runs validation and local Maven publication **without remote upload**.
5. Once validation passes, manually run **Publish DataFox Java Contrib** against that
   exact tag, with `publish_maven=true`. This publishes both signed artifacts and
   attempts to close and release the Sonatype staging repository.
6. Verify the two artifact URLs and signatures in Maven Central before changing the
   Java Agent dependency from its pinned Git submodule to Maven coordinates.

The workflow uses the Sonatype OSSRH Staging API compatibility endpoint already
configured in `build.gradle.kts`. This requires an account whose Central Portal
namespace and token are enabled for the compatibility service. If the namespace
cannot use that service, migrate the publishing transport to the Central Portal
Publishing API rather than repeatedly retrying an incompatible endpoint.

## Local checks

```sh
./gradlew spotlessCheck build \
  :kafka-exporter:publishToMavenLocal \
  :uuid-propagator:publishToMavenLocal \
  -PreleaseVersion=0.1.0-alpha.1
```

The workflow checks the generated POM for SNAPSHOT dependencies. It does not
publish remotely on push or PR. Never store release credentials in Git.

## Agent migration

Until Maven Central contains the artifacts, the Agent continues to use the
existing pinned Contrib Git submodule. After release, update `:custom` to
`implementation("com.datafox:datafox-kafka-exporter:0.1.0-alpha.1")`,
remove the Gradle included build and Git submodule, then run the complete Agent
build, Shadow checks, Java 8 Kafka Broker smoke test, and JVM compatibility matrix.
