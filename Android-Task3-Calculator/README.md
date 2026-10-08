# OIBSIP — Android App Development · Task 3: Calculator

A clean, fully functional native Android calculator app built with **Java + XML**
(no Kotlin, no Jetpack Compose, no view binding).

## Features

- Large right-aligned display showing the current input and result, plus a smaller
  expression line (e.g. `5 +`, or `5 + 3 =` after evaluation)
- Number buttons 0–9 and a decimal point button (only one decimal point per number)
- Operator buttons: `+` (add), `-` (subtract), `x` (multiply), `/` (divide)
- `=` evaluates the expression
- `C` resets everything
- `DEL` (backspace) deletes the last entered character
- **Division by zero** shows `Error`; the next input clears it automatically
- **Operator chaining with immediate execution**: `5 + 3 x 2 =` evaluates
  left-to-right → `((5+3) = 8, then 8x2)` → `16`
- Button grid laid out with `GridLayout` (4 columns, weighted rows/columns so it
  fills any screen without the display overlapping the buttons)
- All clicks wired in Java via `setOnClickListener` (no `android:onClick` in XML)
- All logic is synchronous with guards against empty input, so rapid/repeated
  taps cannot crash the app

## Tech Stack

- Android Studio, Java 17, XML layouts
- `androidx.appcompat:appcompat:1.7.0` (`Theme.AppCompat.Light.NoActionBar` based theme)
- `GridLayout` for the button grid, `findViewById` for view access
- `compileSdk 34`, `minSdk 24`, `targetSdk 34`
- Android Gradle Plugin 8.5.2

## Project Structure

```
Android-Task3-Calculator/
├── README.md
├── settings.gradle
├── build.gradle                 # root: AGP 8.5.2 plugin
├── gradle.properties            # android.useAndroidX=true
└── app/
    ├── build.gradle             # namespace + applicationId com.pavan.oibsip.calculator
    └── src/main/
        ├── AndroidManifest.xml  # MainActivity as launcher, label "Calculator"
        ├── java/com/pavan/oibsip/calculator/
        │   └── MainActivity.java
        └── res/
            ├── layout/activity_main.xml   # display + 4-col GridLayout
            └── values/
                ├── strings.xml
                ├── colors.xml
                └── themes.xml             # Theme.Calculator (NoActionBar)
```

## How to Build

1. Open the `Android-Task3-Calculator` folder in **Android Studio**
   (File → Open, select this folder). Let Gradle sync.
2. Run on an emulator or physical device via **Run → Run 'app'**,
   or build the debug APK:
   - `./gradlew assembleDebug` (from this folder, after generating a Gradle wrapper
     or using the one bundled with Android Studio)
   - APK lands at `app/build/outputs/apk/debug/app-debug.apk`

## Notes

- This folder follows the OIBSIP strict folder naming format
  (`OIBSIP/[TrackName]-[Level/Task]-[ProjectName]/`) — copy it as-is into your
  `OIBSIP` repository before pushing.
