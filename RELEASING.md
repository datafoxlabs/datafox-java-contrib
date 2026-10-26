# Publishing DataFox Java Contrib to GitHub Packages

DataFox Contrib uses the GitHub Packages Maven registry. No domain, Sonatype
namespace, or GPG signing key is required.

Repository: `https://maven.pkg.github.com/datafoxlabs/datafox-java-contrib`

## Version

The first release is `0.1.0-alpha.1`:

- `com.datafox:datafox-kafka-exporter:0.1.0-alpha.1`
- `com.datafox:datafox-uuid-propagator:0.1.0-alpha.1`

Development builds remain `0.1.0-alpha-SNAPSHOT`. The publishing workflow
passes `-PreleaseVersion` to Gradle without changing the development default.

## Publishing

1. Confirm that the `main` branch passes `spotlessCheck`, `build`, and the Java 8 Kafka tests.
2. Open [Publish DataFox Java Contrib to GitHub Packages](https://github.com/datafoxlabs/datafox-java-contrib/actions/workflows/publish-maven.yml).
3. Run `workflow_dispatch` from `main` with version `0.1.0-alpha.1`.
4. The workflow checks the version, builds and validates Maven artifacts, and
   publishes both modules using the built-in `GITHUB_TOKEN` with `packages: write`.
5. Verify both packages in the repository's Packages tab before migrating Agent.

A published release version should be treated as immutable; publish fixes under
a new version rather than overwriting existing artifacts.

## Agent dependency

After publishing, the Agent can consume the pinned version:

```kotlin
implementation("com.datafox:datafox-kafka-exporter:0.1.0-alpha.1")
```

The Agent needs the GitHub Packages Maven repository and credentials with
`read:packages` access. For GitHub Actions, grant the Agent repository access
to each package through **Package settings → Manage Actions access**; its
`GITHUB_TOKEN` can then be used. For local builds, supply a GitHub user and
a personal access token (classic) with `read:packages` through Gradle properties
`gpr.user` and `gpr.key`. Do not commit tokens.

Keep the existing Agent submodule until the package is published and resolvable.
Then remove the composite build and submodule and rerun Agent build, Shadow
packaging checks, Java 8 Kafka smoke tests, and JVM compatibility tests.
