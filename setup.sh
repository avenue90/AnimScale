#!/bin/bash
# setup.sh — build, install, and grant permissions in one step.
# Deploys a debug APK (debuggable=true); intended for personal/developer use only.
set -e

APP_ID="com.ketangupta.animscale"
PERMISSION="android.permission.SET_ANIMATION_SCALE"
ADB="$HOME/Library/Android/sdk/platform-tools/adb"

# If the system JDK is 25+ (breaks Gradle Kotlin DSL), fall back to Android Studio's JBR.
# Remove this block if your system Java is 17 or 21.
AS_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
if [ -d "$AS_JBR" ]; then
  export JAVA_HOME="$AS_JBR"
fi

echo "==> Building APK..."
"$(dirname "$0")/gradlew" assembleDebug -p "$(dirname "$0")" -q

APK="$(dirname "$0")/app/build/outputs/apk/debug/app-debug.apk"

# Pick the first real (non-emulator) device
DEVICE=$("$ADB" devices | awk '/\tdevice$/ && !/emulator/ {print $1; exit}')
if [ -z "$DEVICE" ]; then
  echo "ERROR: No real device found. Connect your phone with USB debugging on."
  exit 1
fi
echo "    Device: $DEVICE"

echo "==> Installing on device..."
"$ADB" -s "$DEVICE" install -r "$APK"

echo "==> Granting permissions (one-time)..."
"$ADB" -s "$DEVICE" shell pm grant "$APP_ID" "$PERMISSION"
"$ADB" -s "$DEVICE" shell pm grant "$APP_ID" android.permission.WRITE_SECURE_SETTINGS

echo ""
echo "Done! Animation Scale is installed and ready."
