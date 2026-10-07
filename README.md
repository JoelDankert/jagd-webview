# Jagd WebView (Android)

Minimal Android app that opens the existing Jagd/Revierverwaltung web app at https://revierverwaltung.duckdns.org/ in a WebView. The web app is hosted separately; this repository does not include or replace it.

## Build

Install Android SDK Platform 36 and a JDK compatible with Android Gradle Plugin 9.0.1, then run:

```sh
./gradlew assembleDebug
```

The debug-signed APK is written to `app/build/outputs/apk/debug/app-debug.apk`. A copy of the tested debug APK is also committed at `apk/Jagd-WebView-debug.apk` as requested. It is a debug build, not a Play Store release. Keep `local.properties` and signing keys out of Git.

The app requires an internet connection and a valid HTTPS certificate for the web app. No certificate errors are bypassed.
