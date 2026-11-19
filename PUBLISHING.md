# Publishing

Releases go to Maven Central through the [Central Portal](https://central.sonatype.com) with the
`com.vanniktech.maven.publish` plugin. Credentials and the signing key live in `~/.gradle/gradle.properties`
(never in the repo):

```properties
mavenCentralUsername=<portal token user>
mavenCentralPassword=<portal token password>
signingInMemoryKey=<ASCII-armored private key>
signingInMemoryKeyPassword=<key password>
```

1. Set `version` in `build.gradle`, add the CHANGELOG.md entry, wait for green CI.
2. `./gradlew publishAndReleaseToMavenCentral --no-configuration-cache`
3. Tag and create the GitHub release.
