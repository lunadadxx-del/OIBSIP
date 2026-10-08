#!/bin/bash
# Manual Android build without Gradle: aapt2 compile/link + javac + d8 + zipalign + apksigner.
# Produces a signed, installable debug APK per app.
set -e
SDK=$HOME/android-sdk
BT=$SDK/build-tools/34.0.0
ANDROID_JAR=$SDK/platforms/android-34/android.jar
KEYSTORE=$HOME/oibsip-debug.keystore

if [ ! -f "$KEYSTORE" ]; then
  keytool -genkeypair -keystore "$KEYSTORE" -alias androiddebugkey -keyalg RSA \
    -keysize 2048 -validity 10950 -storepass android -keypass android \
    -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null
fi

build_app() {
  local dir=$1   # e.g. Android-Task1-UnitConverter
  local name=$2  # e.g. unit-converter
  cd ~/workspace/oibsip-build/$dir
  rm -rf build/manual && mkdir -p build/manual/gen build/manual/classes build/manual/dex

  echo "[$name] aapt2 compile..."
  $BT/aapt2 compile --dir app/src/main/res -o build/manual/compiled_res.zip

  echo "[$name] aapt2 link..."
  $BT/aapt2 link -o build/manual/app-unsigned.apk \
    --manifest app/src/main/AndroidManifest.xml \
    -I "$ANDROID_JAR" \
    --min-sdk-version 24 --target-sdk-version 34 \
    --java build/manual/gen \
    build/manual/compiled_res.zip

  echo "[$name] javac..."
  find app/src/main/java build/manual/gen -name "*.java" > build/manual/sources.txt
  javac -source 17 -target 17 -nowarn -cp "$ANDROID_JAR" \
    -d build/manual/classes @build/manual/sources.txt

  echo "[$name] d8..."
  $BT/d8 --lib "$ANDROID_JAR" --min-api 24 --output build/manual/dex \
    $(find build/manual/classes -name "*.class")

  echo "[$name] package + align + sign..."
  cp build/manual/app-unsigned.apk build/manual/app-dex.apk
  (cd build/manual/dex && zip -q -X ../app-dex.apk classes.dex)
  $BT/zipalign -f 4 build/manual/app-dex.apk build/manual/app-aligned.apk
  $BT/apksigner sign --ks "$KEYSTORE" --ks-pass pass:android --key-pass pass:android \
    --out "$name-debug.apk" build/manual/app-aligned.apk
  $BT/apksigner verify "$name-debug.apk" && echo "[$name] signature OK"
  $BT/aapt2 dump badging "$name-debug.apk" | head -2
  echo "[$name] DONE: $(stat -c%s $name-debug.apk) bytes -> $name-debug.apk"
}

build_app Android-Task1-UnitConverter unit-converter
build_app Android-Task3-Calculator calculator
build_app Android-Task5-Stopwatch stopwatch
echo "ALL_BUILDS_DONE"
