# Publishing

Releases go to Maven Central through the [Central Portal](https://central.sonatype.com) with the
`com.vanniktech.maven.publish` plugin, from CI: `.github/workflows/release.yml` (environment `release`).

1. Set `version` in `build.gradle`, add the `## X.Y.Z` entry to CHANGELOG.md (and `docs/content/changelog.md`),
   wait for green CI.
2. Tag `vX.Y.Z` and push the tag. The workflow runs CI, checks the tag against `version` and the changelog,
   runs `./gradlew publishAndReleaseToMavenCentral --no-configuration-cache` and creates the GitHub release
   with the changelog section as notes.

Secrets of the `release` environment, mapped to the plugin's Gradle properties as `ORG_GRADLE_PROJECT_*`:

| Secret | Gradle property |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | `mavenCentralUsername` (portal token user) |
| `MAVEN_CENTRAL_PASSWORD` | `mavenCentralPassword` (portal token password) |
| `SIGNING_KEY` | `signingInMemoryKey` (ASCII-armored private key) |
| `SIGNING_KEY_PASSWORD` | `signingInMemoryKeyPassword` |

A manual release from a workstation works the same with those properties in `~/.gradle/gradle.properties`
(never in the repo): `./gradlew publishAndReleaseToMavenCentral --no-configuration-cache`.

Check the artifact locally (publishes into `build/m2`, never `~/.m2`): `./scripts/smoke-artifact.sh`.
