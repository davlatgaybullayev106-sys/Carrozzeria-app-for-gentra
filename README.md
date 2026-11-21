# CARROZZERIA DSP — Android prototype 0.1

Target device: Android 10 / UIS8581A / 1280x720 landscape.

## What this prototype does
- Requests Android output-mix Equalizer using audio session `0`.
- Builds sliders for the actual number of EQ bands exposed by the device (not a fake 31-band UI).
- Attempts BassBoost and Virtualizer output effects.
- Includes simple starter presets.
- Shows a status message if global output EQ is rejected by the firmware.

## Important limitations
- This is an experimental prototype, not a verified APK for the Dominant head unit.
- Android `AudioEffect` session 0 is not guaranteed to work on every vendor firmware or every audio route. FM radio, Bluetooth, USB, navigation prompts, and factory MCU audio may use separate paths.
- The factory EQ changing YouTube audio proves that the factory DSP affects YouTube; it does not prove a third-party app can control the factory DSP.
- This prototype does not yet control the Dominant factory DSP, provide 31 independent hardware bands, time alignment, crossover configuration, or save profiles persistently.
- Do not change factory/MCU settings or flash firmware for this test.

## Build APK
Open this folder in Android Studio (Giraffe or newer), allow Gradle sync, then choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

If Gradle asks for SDK components, install Android SDK Platform 35 and Build Tools offered by Android Studio. For the target head unit, install the debug APK and test at low volume first.

## Gradle wrapper
Loyihada `gradlew` (Gradle 8.9) mavjud. Terminaldan: `./gradlew assembleDebug` (JDK 17 + Android SDK 35 kerak).
GitHub'ga yuklasangiz, `.github/workflows/build.yml` APK'ni avtomatik yig'adi (Actions > Artifacts).
