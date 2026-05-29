# AnimScale

**Lock your Android animation speed — even with Developer Options off.**

AnimScale sets system animation scales (window, transition, animator) to your preferred value and keeps them there via a lightweight background service. The moment Android resets animations — such as when you toggle off Developer Options — the service restores your setting within milliseconds.

---

## Why this exists

Android's animation speed settings live inside Developer Options. Turning Developer Options off (e.g. to satisfy banking apps that refuse to open when it's enabled) resets animations back to 1×. AnimScale solves this by watching for that reset and immediately writing the value back — no root required.

---

## Features

- **Set** animation scale to 0.5× (recommended), 1× (default), or Off (0×)
- **Lock** the chosen scale via a persistent foreground service
- **Survives** Developer Options being turned off
- **Restores** scale on every reboot automatically
- **No root required** — uses a one-time ADB permission grant
- **No internet permission** — zero network access, zero telemetry

---

## Requirements

- Android 10+ (minSdk 29)
- A Mac/Linux/Windows machine with `adb` installed for the one-time setup
- USB Debugging **or** Wireless Debugging enabled during setup only

---

## One-time setup

### 1 — Clone and build

```bash
git clone https://github.com/YOUR_USERNAME/AnimScale.git
cd AnimScale
```

### 2 — Connect your phone

Enable **Developer Options** on your phone, then enable **USB Debugging** (or **Wireless Debugging**).

For wireless setup (no cable after pairing):
```bash
# Pair once
adb pair <ip>:<port>      # IP and port shown in Developer Options → Wireless Debugging

# Then connect
adb connect <ip>:<port>
```

### 3 — Run setup.sh

```bash
./setup.sh
```

This script:
1. Builds the debug APK
2. Installs it on your phone (skips any connected emulators automatically)
3. Grants the two required permissions:
   - `android.permission.SET_ANIMATION_SCALE`
   - `android.permission.WRITE_SECURE_SETTINGS`

> **These permissions survive reboots and app updates.** You only need to re-run `setup.sh` if you fully uninstall the app.

### 4 — Exempt from battery optimisation (important)

Samsung and other OEMs aggressively kill background services. To keep the lock working:

**Samsung One UI:**  
Settings → Battery → Background usage limits → Never sleeping apps → Add AnimScale

**Stock Android:**  
Settings → Apps → AnimScale → Battery → Unrestricted

### 5 — Lock your scale

Open the app, tap **Set 0.5× (recommended)**. The keeper service starts and the scale is locked. You can now turn off Developer Options — AnimScale will restore 0.5× the instant Android resets it.

---

## How it works

```
┌──────────────┐   writes    ┌─────────────────────┐
│  MainActivity │ ─────────► │  Settings.Global     │
└──────────────┘             │  window_anim_scale   │
                             │  transition_anim_*   │
┌──────────────────────────┐ │  animator_duration_* │
│  ScaleKeeperService      │ └─────────────────────┘
│  (foreground, START_STICKY)│        ▲
│                          │        │ ContentObserver
│  Watches for any change  │ ───────┘  (fires within
│  and immediately restores│           300 ms)
│  the target scale        │
└──────────────────────────┘
        ▲
        │ starts on boot
┌───────────────┐
│  BootReceiver │
└───────────────┘
```

**Permission model:**  
`WRITE_SECURE_SETTINGS` is a signature-level permission. It is granted once via ADB (`pm grant`) and is **not** revoked when Developer Options is disabled — unlike development-class permissions such as USB debugging itself.

---

## Building from Android Studio

If you prefer not to use the command line:

1. Open the project in Android Studio
2. Let Gradle sync (it uses Gradle 8.12 + Java 21 from the bundled JBR — configured automatically via `gradle.properties`)
3. Run the app on your device
4. Grant permissions manually via Terminal:
   ```bash
   adb shell pm grant com.ketangupta.animscale android.permission.SET_ANIMATION_SCALE
   adb shell pm grant com.ketangupta.animscale android.permission.WRITE_SECURE_SETTINGS
   ```

---

## Project structure

```
app/src/main/java/com/ketangupta/animscale/
├── MainActivity.kt          — UI, permission flow
├── AnimationScaleManager.kt — Read/write Settings.Global
├── ScaleKeeperService.kt    — Foreground service + ContentObserver
└── BootReceiver.kt          — Restarts keeper on device boot
```

---

## Contributing

Bug reports and pull requests are welcome. For significant changes, please open an issue first to discuss what you'd like to change.

---

## License

[MIT](LICENSE)
