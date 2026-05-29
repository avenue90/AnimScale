#!/bin/bash
set -e

APP_ID="com.ketangupta.animscale"
PERMISSION="android.permission.SET_ANIMATION_SCALE"
APK="app/build/outputs/apk/debug/app-debug.apk"
GRADLE="$HOME/.gradle/wrapper/dists/gradle-8.12-all/ejduaidbjup3bmmkhw3rie4zb/gradle-8.12/bin/gradle"
JAVA_HOME_AS="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
ADB="$HOME/Library/Android/sdk/platform-tools/adb"

echo "==> Building APK..."
JAVA_HOME="$JAVA_HOME_AS" "$GRADLE" assembleDebug -p "$(dirname "$0")" -q

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
