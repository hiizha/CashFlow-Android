# CashFlow Android

Android counterpart of the supplied KasuFlow iOS source. App label and application ID are `CashFlow` and `com.cashflow.tezha`.

## Features
- Income/expense dashboard and balance summary
- Add and delete transactions, categories and notes
- Transaction filters
- Local persistence on device (SharedPreferences JSON)
- JPY, IDR, USD, EUR, GBP and other common display currencies
- System / light / dark theme preference
- About section: Dibuat oleh Tesa Adi Wijayanto

Changing currency changes display formatting only; it does not convert previously entered values. Data stays on the device and is not synced between iOS and Android.

## Build without a computer
1. Create a GitHub repository and upload the contents of this folder to its root.
2. Open the repository's **Actions** tab.
3. Enable Actions if GitHub asks.
4. Select **Build CashFlow Android APK** → **Run workflow**.
5. Wait for the run to finish with a green check.
6. Open the completed run, scroll to **Artifacts**, and download `CashFlow-Android-APK`.
7. Extract the ZIP; install `app-debug.apk` on your Android phone. If prompted, allow installation from that source.

The workflow runs on GitHub-hosted Ubuntu and builds a debug APK. It requires no Mac or local Android Studio. For Play Store distribution, create a signed release build separately.

## Local build
Requires JDK 17 and Gradle 8.9:
`gradle assembleDebug`
