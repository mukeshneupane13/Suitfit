# SuitFlow Android APK

This repository package contains:

- A password-protected Android app (`Mukesh@123`)
- The complete SuitFlow try-on experience bundled for offline use
- Camera and gallery upload support
- A GitHub Actions workflow that builds an installable debug APK
- A minimized release APK build

## Build

The workflow at `.github/workflows/build-apk.yml` runs automatically after the
files are pushed to the `main` branch. The installable output is published as
the `SuitFlow-APK` workflow artifact.

## Android requirements

- Minimum Android version: Android 7.0 (API 24)
- Target Android version: Android 15 (API 35)
- Application ID: `com.suitflow.app`

