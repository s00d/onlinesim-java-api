#!/usr/bin/env bash
# Smoke test of the PUBLISHED artifact, offline from any OnlineSim host:
#  1. publishToMavenLocal into build/m2 (never ~/.m2);
#  2. check the publication: jar + sources + javadoc + POM + Gradle module, POM fields Maven Central requires,
#     Gson as the only dependency, every class file major 55 (Java 11);
#  3. resolve it from an empty Gradle project and run the README "Testing" example against MockOnlineSim.
# SMOKE_JAVA_HOME (optional) runs the example on another JDK, e.g. 11; the build itself needs JDK 17+.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
version="$(sed -n "s/^version = '\(.*\)'$/\1/p" build.gradle)"
repo="$root/build/m2"
app="$root/build/smoke-app"
fail() { echo "smoke: $*" >&2; exit 1; }

rm -rf "$repo/ru/onlinesim" "$app"
./gradlew publishToMavenLocal "-Dmaven.repo.local=$repo" --no-daemon --quiet

dir="$repo/ru/onlinesim/onlinesim-api/$version"
base="$dir/onlinesim-api-$version"
for f in "$base.jar" "$base-sources.jar" "$base-javadoc.jar" "$base.pom" "$base.module"; do
  [[ -s "$f" ]] || fail "missing $f"
done
# grep without -q: an early exit would SIGPIPE unzip under pipefail.
unzip -l "$base-javadoc.jar" | grep 'ru/onlinesim/mock/MockOnlineSim.html' >/dev/null || fail "javadoc jar lacks the mock"
unzip -l "$base-sources.jar" | grep 'ru/onlinesim/mock/MockOnlineSim.java' >/dev/null || fail "sources jar lacks the mock"

pom="$base.pom"
for tag in '<name>' '<description>' '<url>' '<licenses>' '<license>' '<scm>' '<connection>' '<developers>' '<developer>'; do
  grep -q "$tag" "$pom" || fail "POM lacks $tag"
done
deps="$(grep -o '<artifactId>[^<]*</artifactId>' "$pom" | sed 's/<[^>]*>//g' | grep -v '^onlinesim-api$' | sort -u | tr '\n' ' ')"
[[ "$deps" == "gson " ]] || fail "POM dependencies are '$deps', expected only gson"
echo "publication $version: jar, sources, javadoc, pom, module ok; POM complete; deps: $deps"

classes=0
while IFS= read -r cls; do
  major="$(unzip -p "$base.jar" "$cls" | od -An -j6 -N2 -tu1 | awk 'NF {print $1 * 256 + $2}')"
  [[ "$major" == 55 ]] || fail "$cls has class file major $major, expected 55"
  classes=$((classes + 1))
done < <(unzip -Z1 "$base.jar" | grep '\.class$')
echo "class files: $classes, all major 55"

mkdir -p "$app/src/main/java"
cat > "$app/settings.gradle" <<'EOF'
rootProject.name = 'smoke-app'
EOF
cat > "$app/build.gradle" <<EOF
plugins { id 'application' }
repositories {
    maven { url = uri('$repo') }
    mavenCentral()
}
dependencies { implementation 'ru.onlinesim:onlinesim-api:$version' }
tasks.withType(JavaCompile).configureEach { options.release = 11 }
application { mainClass = 'MockExample' }
EOF
# The first java block after "## Testing" in README.md.
awk '/^## Testing/ {t=1} t && /^```java/ {b=1; next} b && /^```/ {exit} b {print}' README.md \
  > "$app/src/main/java/MockExample.java"
grep -q 'class MockExample' "$app/src/main/java/MockExample.java" || fail "README Testing example not found"

./gradlew -p "$app" installDist --no-daemon --quiet
JAVA_HOME="${SMOKE_JAVA_HOME:-${JAVA_HOME:-}}" "$app/build/install/smoke-app/bin/smoke-app"
echo "README \"Testing\" example from the artifact: ok ($("${SMOKE_JAVA_HOME:-${JAVA_HOME:-/usr}}/bin/java" -version 2>&1 | head -1))"
