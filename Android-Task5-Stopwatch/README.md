# OIBSIP Android — Task 5: Stopwatch

A native Android stopwatch app built for the Oasis Infobyte SIP internship
(Android App Development track, Task 5).

## Features

- **Large time display** in `HH:MM:SS.cs` format (hours, minutes, seconds, centiseconds), e.g. `00:01:23.45`
- **Start** — begins timing from 0, or resumes from the paused state
- **Pause** — freezes the timer at the current elapsed time
- **Reset** — stops the timer, resets the display to `00:00:00.00`, clears all laps
- **Button state changes** — Start is disabled while running, Pause is disabled
  when not running, Lap is disabled unless the timer is running
- **Lap (bonus)** — records the current formatted time into a scrollable
  `ListView` below the buttons (`ArrayAdapter<String>`), with the newest lap on top
- **Lifecycle-safe** — navigating away pauses only the UI ticks; the timer keeps
  correct time because it is anchored on the monotonic system clock
- **Rotation-safe** — running state, timing values, and the lap list are saved
  in `onSaveInstanceState` and restored in `onCreate`, so rotating the device
  never resets the stopwatch
- Clean UI: primary blue `#1565C0`, large monospace time text, buttons in a row

## Tech Stack

- **Language:** Java (no Kotlin, no Jetpack Compose, no view binding — `findViewById` only)
- **UI:** Android XML layouts, AppCompat (`Theme.AppCompat.Light.NoActionBar`)
- **Timing:** `android.os.Handler` + `Runnable` (50 ms) with
  `android.os.SystemClock.elapsedRealtime()`
- Android Gradle Plugin 8.5.2 · compileSdk/targetSdk 34 · minSdk 24 · Java 17

## Project Structure

```
Android-Task5-Stopwatch/
├── settings.gradle
├── build.gradle                 # root — AGP 8.5.2 plugin
├── gradle.properties            # android.useAndroidX=true
├── app/
│   ├── build.gradle             # namespace/applicationId com.pavan.oibsip.stopwatch
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/pavan/oibsip/stopwatch/MainActivity.java
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/{strings,colors,themes}.xml
└── README.md
```

## How to Build

1. Open the folder `Android-Task5-Stopwatch` in **Android Studio** (it will be
   detected as a Gradle project).
2. Let Gradle sync, then press **Run** to install on an emulator or device —
   or use **Build → Build APK(s)**.
3. Command line (requires an Android SDK + JDK 17):
   ```bash
   ./gradlew assembleDebug
   ```
   The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Note on the Timing Approach

The display is refreshed by a `Handler` + `Runnable` posting every 50 ms, but
the *elapsed time is never derived from tick counts*. Instead it is computed as

```
elapsed = accumulatedBefore + (SystemClock.elapsedRealtime() - startBase)
```

`elapsedRealtime()` is a monotonic clock (unaffected by wall-clock changes),
so delays, GC pauses, or the activity going to the background cannot introduce
drift.

- `onPause()` removes the handler callbacks (no pointless UI work in the
  background) but **keeps** the running state and timing fields — the clock
  keeps accruing while the user is away.
- `onResume()` re-posts the ticker if still running and refreshes the display;
  the shown time is immediately correct.
- Rotation: `onSaveInstanceState` stores the running flag, `startBase`,
  `accumulated`, the lap counter, and the lap list; `onCreate` restores them.
  Because `elapsedRealtime()` is process-monotonic, the restored values remain
  exact after the recreate.
