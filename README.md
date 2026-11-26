# WoomanCalendar

**English** · [Русский](README.ru.md) · [Deutsch](README.de.md) · [Español](README.es.md)

A private, offline-first period and cycle tracker for Android. Mark the first day of your period and the calendar predicts upcoming periods, the fertile window and ovulation. No account, no ads, no analytics, no servers — your data stays on your phone and is mirrored to **your own Google Drive**.

> Predictions are approximate and are not medical advice or a contraceptive method.

## Features

- Month calendar with period, predicted period, fertile window and ovulation
- One-tap marking of period start/end; per-day flow, symptoms and notes
- Prediction from your last 6 cycles (recent cycles weigh more)
- Data survives app updates (same package name + same signing key)
- Automatic JSON backup to a file on your Google Drive — no Google Cloud registration, no sign-in inside the app
- Languages: English, Русский, Українська, Deutsch, Español (follows system language; per-app language on Android 13+)

## Screenshots

<!-- Add real screenshots to docs/screenshots/ (see docs/screenshots/README.md), then uncomment:
<p>
  <img src="docs/screenshots/calendar.png" width="260" alt="Calendar">
  <img src="docs/screenshots/day.png" width="260" alt="Day dialog">
  <img src="docs/screenshots/settings.png" width="260" alt="Settings and backup">
</p>
-->
_Screenshots are coming soon._

## Install

1. On your phone open the [latest release](https://github.com/navaikashalot-ops/WoomanCalendar/releases/latest) and download `WoomanCalendar-<N>.apk`.
2. Open the file. If Android asks, allow installing apps from this source (browser or file manager) — *Settings → Apps → Special access → Install unknown apps*.
3. Tap **Install**, then open the app.
4. **Updating:** install the new APK over the old one. **Do not uninstall** — uninstalling erases local data.

Requires Android 8.0 (API 26) or newer.

## Backup to Google Drive (do this once)

1. Open **Settings** in the app → **Create backup file** → pick *Google Drive* in the system picker → save `cycle-backup.json`.
2. From now on the file is updated automatically after every change.
3. New phone or reinstall: **Settings → Restore from file** → choose `cycle-backup.json` on Drive.

Android's own Auto Backup also stores a copy, but restoring it for APKs installed outside Google Play is not guaranteed — rely on the Drive file.

## Build it yourself (GitHub Actions)

1. Fork this repository.
2. Create a signing key once: `bash scripts/make-keystore.sh` (needs a JDK). **Keep `mycycle.jks` and its password safe** — a different key means updates cannot be installed over the old app and local data is lost.
3. Add repository secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS` (the script prints the commands).
4. Push to `main` or run the **Build APK** workflow. The APK appears under *Releases*.

Stack: Kotlin, Jetpack Compose, Room, kotlinx.serialization.

## Privacy

No network permissions, no analytics. Data lives in the app's private storage and in the backup file you choose.
