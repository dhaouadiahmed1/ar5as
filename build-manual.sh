#!/usr/bin/env bash
# Ar5as v0.1 — MANUAL build (no Gradle).
#
# Why: this sandbox blocks raw TCP ("other_tcp: Deny"), which breaks the
# Gradle daemon protocol (loopback TCP). Gradle itself is fine — the same
# project builds with ./gradlew on GitHub Actions (see .github/workflows).
# This script does exactly what AGP does, step by step, with the same
# compilers (kotlinc 2.0.0 + Compose compiler 2.0.0, aapt2, d8):
#   resolve deps -> dedup -> aapt2 compile/link -> javac (R.java) -> kotlinc
#   -> d8 -> zipalign -> apksigner
# NOTE (v0.2): javac runs BEFORE kotlinc so Kotlin code can reference R
# (product photos); v0.1 had no R references in Kotlin.
# NOTE (v0.2 push): photos live in the repo as base64 TEXT (app/images-b64/)
# because the GitHub App snapshot push is text-only; they are decoded to
# JPGs in res/drawable/ by scripts/decode_images.py (step [1b/8] below)
# before aapt2 compiles the resources.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
export JAVA_HOME="$HOME/workspace/tools/jdk-21.0.13+9"
export PATH="$JAVA_HOME/bin:$PATH"
SDK="$HOME/workspace/tools/android-sdk"
KOTLINC="$HOME/workspace/tools/kotlinc-2.0.20"
DEPS="$HOME/workspace/tools/ar5as-deps"
AAPT2="$SDK/build-tools/34.0.0/aapt2"
D8="$SDK/build-tools/34.0.0/d8"
ZIPALIGN="$SDK/build-tools/34.0.0/zipalign"
APKSIGNER="$SDK/build-tools/34.0.0/apksigner"
ANDROID_JAR="$SDK/platforms/android-34/android.jar"
BUILD="$ROOT/build/manual"
INPUTS="$BUILD/inputs"
GEN="$BUILD/gen"
CLASSES="$BUILD/classes"
DEX="$BUILD/dex"

echo "== [1/8] resolving + deduplicating dependencies =="
python3 "$HOME/workspace/tools/resolve-deps.py" > "$BUILD/resolve.log" 2>&1 || {
  echo "resolve failed, see $BUILD/resolve.log"; tail -20 "$BUILD/resolve.log"; exit 1; }
mkdir -p "$BUILD"
python3 "$HOME/workspace/tools/dedup-deps.py"
rm -rf "$GEN" "$CLASSES" "$DEX"
mkdir -p "$GEN" "$CLASSES" "$DEX" "$BUILD/res-flat"
CP="$(cat "$INPUTS/cp.txt"):$ANDROID_JAR"
echo "  classpath jars: $(echo "$CP" | tr ':' '\n' | wc -l)"

echo "== [1b/8] decode product photos (base64 text -> JPG drawables) =="
python3 "$ROOT/scripts/decode_images.py" || { echo "decode_images failed"; exit 1; }

echo "== [2/8] aapt2 compile (app + library resources) =="
"$AAPT2" compile --dir "$ROOT/app/src/main/res" -o "$BUILD/res-flat/app.zip"
i=0
while IFS= read -r resdir; do
  [ -n "$resdir" ] || continue
  i=$((i + 1))
  "$AAPT2" compile --dir "$resdir" -o "$BUILD/res-flat/lib$i.zip" 2>/dev/null || true
done < "$INPUTS/resdirs.txt"
LIB_FLATS=("$BUILD"/res-flat/lib*.zip)
echo "  lib resource sets: ${#LIB_FLATS[@]}"

echo "== [3/8] aapt2 link =="
# aapt2 needs a package attribute; inject it into a temp copy (the Gradle
# build uses the namespace from build.gradle.kts instead, manifest unchanged).
sed 's|<manifest |<manifest package="app.ar5as" android:versionCode="3" android:versionName="0.3" |' "$ROOT/app/src/main/AndroidManifest.xml" > "$BUILD/AndroidManifest.xml"
EXTRA_ARGS=()
while IFS= read -r p; do
  [ -n "$p" ] && EXTRA_ARGS+=(--extra-packages "$p")
done < "$INPUTS/pkgs.txt"
"$AAPT2" link -I "$ANDROID_JAR" \
  --manifest "$BUILD/AndroidManifest.xml" \
  --min-sdk-version 26 --target-sdk-version 34 \
  --java "$GEN" \
  "${EXTRA_ARGS[@]}" \
  --auto-add-overlay \
  -o "$BUILD/app-unaligned.apk" \
  "$BUILD/res-flat/app.zip" "${LIB_FLATS[@]}"
echo "  linked APK: $(du -h "$BUILD/app-unaligned.apk" | cut -f1)"

echo "== [4/8] javac (generated R.java) — FIRST, so kotlinc can resolve R =="
R_JAVA="$(find "$GEN" -name '*.java')"
if [ -n "$R_JAVA" ]; then
  # shellcheck disable=SC2086
  javac --release 17 -cp "$ANDROID_JAR" -d "$CLASSES" $R_JAVA
  echo "  R classes: $(find "$CLASSES" -name 'R*.class' | wc -l)"
fi

echo "== [5/8] kotlinc (Compose) =="
PLUGIN="$(ls "$DEPS"/*kotlin-compose-compiler-plugin__*.jar)"
SOURCES="$(find "$ROOT/app/src/main/java" -name '*.kt')"
# shellcheck disable=SC2086
"$KOTLINC/bin/kotlinc" -jvm-target 17 \
  -Xplugin="$PLUGIN" \
  -cp "$CP:$CLASSES" \
  -d "$CLASSES" $SOURCES
echo "  kotlin classes: $(find "$CLASSES" -name '*.class' | wc -l)"

echo "== [6/8] d8 (dex) =="
mapfile -t DEX_JARS < "$INPUTS/dex.txt"
DEX_JARS+=("$KOTLINC/lib/kotlin-stdlib.jar")
# d8 does not accept a bare directory of .class files — jar them first.
(cd "$CLASSES" && jar --create --file="$BUILD/app-classes.jar" .)
"$D8" --release --min-api 26 --lib "$ANDROID_JAR" --output "$DEX" \
  "$BUILD/app-classes.jar" "${DEX_JARS[@]}"
ls -la "$DEX"

echo "== [7/8] package + align + sign =="
python3 - "$BUILD/app-unaligned.apk" "$DEX" <<'EOF'
import sys, zipfile, glob, os
apk, dexdir = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(apk, "a") as z:
    for dex in sorted(glob.glob(os.path.join(dexdir, "classes*.dex"))):
        z.write(dex, os.path.basename(dex))
print("added dex files")
EOF
"$ZIPALIGN" -f 4 "$BUILD/app-unaligned.apk" "$BUILD/app-aligned.apk"
if [ ! -f "$ROOT/debug.keystore" ]; then
  keytool -genkeypair -keystore "$ROOT/debug.keystore" -alias androiddebugkey \
    -keypass android -storepass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug, O=Ar5as" -noprompt
fi
"$APKSIGNER" sign --ks "$ROOT/debug.keystore" --ks-pass pass:android \
  --key-pass pass:android --ks-key-alias androiddebugkey \
  --out "$ROOT/build/manual/ar5as-debug.apk" "$BUILD/app-aligned.apk"
"$APKSIGNER" verify --verbose "$ROOT/build/manual/ar5as-debug.apk"
echo
echo "== DONE =="
ls -la "$ROOT/build/manual/ar5as-debug.apk"
"$AAPT2" dump badging "$ROOT/build/manual/ar5as-debug.apk" | head -8
