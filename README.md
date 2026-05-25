# SecureScreen

[![F-Droid](https://fdroid.gitlab.io/artwork/badge/get-it-on.png)](https://f-droid.org/packages/com.securescreen.app/)
[![Buy me a coffee](https://img.shields.io/badge/Buy%20me%20a%20coffee-Support-yellow?logo=buy-me-a-coffee&logoColor=white)](https://buymeacoffee.com/adikul1023)

SecureScreen is a Kotlin Android app that prevents screenshots and screen recording for selected apps using `FLAG_SECURE`-based enforcement.

## Features

- Select protected apps from installed launchable applications
- Search apps by name or package before enabling protection
- Foreground app detection via `UsageStatsManager`
- Foreground service with persistent notification
- Accessibility-service overlay to keep touch input working while protection is active
- Transparent `SecureActivity` enforcement with `FLAG_SECURE`
- Optional watermark overlay with timestamp and session ID
- Settings for watermark toggle, opacity, and aggressive mode flag
- Boot receiver to recover service state after reboot/update
- Watchdog to keep protection alive during idle or screen-off periods

## Get It

- F-Droid: https://f-droid.org/packages/com.securescreen.app/

## Build

1. Open this project in Android Studio (Jellyfish or newer recommended).
2. Let Gradle sync complete.
3. If `gradle-wrapper.jar` is missing, run `gradle wrapper` once from the project root.
4. Run on a physical Android device (Android 8.0+).

## Required User Setup

- Grant Usage Access permission in system settings.
- Enable notification permissions on Android 13+ if prompted.
- Enable the SecureScreen accessibility service when prompted.
- Grant Overlay permission only if watermark is enabled.

## Play Store Release

Please support me to bring the app to Play Store.

[![Buy me a coffee](https://img.shields.io/badge/Buy%20me%20a%20coffee-Support-yellow?logo=buy-me-a-coffee&logoColor=white)](https://buymeacoffee.com/adikul1023)

## Project Stats

![Stars](https://img.shields.io/github/stars/adikul1023/SecureScreen?style=flat)
![Commits](https://img.shields.io/github/commit-activity/m/adikul1023/SecureScreen?style=flat)
![Pull Requests](https://img.shields.io/github/issues-pr/adikul1023/SecureScreen?style=flat)
![Closed Pull Requests](https://img.shields.io/github/issues-pr-closed/adikul1023/SecureScreen?style=flat)
![Issues](https://img.shields.io/github/issues/adikul1023/SecureScreen?style=flat)
![Closed Issues](https://img.shields.io/github/issues-closed/adikul1023/SecureScreen?style=flat)
![Views](https://komarev.com/ghpvc/?username=adikul1023&repo=SecureScreen&style=flat)
