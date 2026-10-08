# Unit Converter — OIBSIP Android Task 1

A native Android unit converter app built with **Java + XML** (no Kotlin, no Compose).

## Features

- **4 conversion categories**: Length (8 units), Weight (5 units), Temperature (Celsius / Fahrenheit / Kelvin), Volume (7 US + metric units)
- Category Spinner — switching categories repopulates the source/target unit Spinners and resets their selections
- Source-unit and target-unit Spinners with a full list of units per category
- Numeric input field (`numberSigned | numberDecimal`) with validation:
  - Empty or non-numeric input shows a Toast: *"Please enter a valid number"*
- Convert button that computes and displays the result with a unit label (e.g. `1 in = 2.54 cm`)
- Temperature conversions run through a Celsius pivot; inputs that fall **below absolute zero (-273.15 °C)** show the error message *"Below absolute zero (-273.15 C)"* in the result area instead of a value
- Results formatted to up to 4 decimal places with trailing zeros stripped (e.g. `2.54`, not `2.5400`)
- Clean Material-style UI: primary blue `#1565C0`, white cards on a light-gray background, `ScrollView` + `LinearLayout` so nothing overlaps on small screens

## Tech Stack

- Java 17, Android SDK (compileSdk 34, minSdk 24, targetSdk 34)
- Android Gradle Plugin 8.5.2, AndroidX AppCompat 1.7.0
- Plain `findViewById` (no view binding), `Theme.AppCompat.Light.NoActionBar`-based theme

## Project Structure

```
Android-Task1-UnitConverter/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── README.md
└── app/
    ├── build.gradle
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/pavan/oibsip/unitconverter/MainActivity.java
        └── res/
            ├── layout/activity_main.xml
            ├── values/{strings.xml, colors.xml, themes.xml}
            └── drawable/{card_background.xml, input_background.xml}
```

## How to Build

1. Open the `Android-Task1-UnitConverter` folder in Android Studio (Hedgehog or newer, with JDK 17).
2. Let Gradle sync (requires AGP 8.5.2 → Gradle 8.7+, downloaded automatically).
3. Run on an emulator or device, or build a debug APK:

```bash
./gradlew assembleDebug
```

## Screenshots

_Add screenshots of the app here (OIBSIP submission requirement)._

## Notes

- Input validation and absolute-zero handling are implemented as described above — the app never crashes on bad input.
- Temperature uses a Celsius pivot: `C = (F − 32) × 5/9`, `F = C × 9/5 + 32`, `K = C + 273.15`. The absolute-zero check runs on the Celsius-pivot value, so e.g. `-500 °F` is correctly rejected.
