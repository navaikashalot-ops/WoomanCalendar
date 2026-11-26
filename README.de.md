# WoomanCalendar

[English](README.md) · [Русский](README.ru.md) · [Українська](README.uk.md) · **Deutsch** · [Español](README.es.md)

Ein privater Perioden- und Zykluskalender für Android, der offline funktioniert. Markiere den ersten Tag deiner Periode — der Kalender prognostiziert die nächsten Perioden, das fruchtbare Fenster und den Eisprung. Kein Konto, keine Werbung, keine Analyse, keine Server: Deine Daten bleiben auf dem Handy und werden auf **dein eigenes Google Drive** gespiegelt.

> Prognosen sind ungefähr und weder ärztlicher Rat noch ein Verhütungsmittel.

## Funktionen

- Monatskalender mit Periode, prognostizierter Periode, fruchtbarem Fenster und Eisprung
- Beginn/Ende der Periode mit einem Tipp markieren; Stärke, Symptome und Notizen pro Tag
- Prognose aus den letzten 6 Zyklen (neuere zählen stärker)
- Daten bleiben bei App-Updates erhalten (gleicher Paketname + gleicher Signaturschlüssel)
- Automatische JSON-Sicherung in eine Datei auf deinem Google Drive — ohne Google-Cloud-Registrierung und ohne Anmeldung in der App
- Sprachen: English, Русский, Українська, Deutsch, Español (folgt der Systemsprache; App-Sprache ab Android 13)

## Screenshots

<!-- Echte Screenshots in docs/screenshots/ ablegen (siehe docs/screenshots/README.md) und einkommentieren:
<p>
  <img src="docs/screenshots/calendar.png" width="260" alt="Kalender">
  <img src="docs/screenshots/day.png" width="260" alt="Tag">
  <img src="docs/screenshots/settings.png" width="260" alt="Einstellungen und Sicherung">
</p>
-->
_Screenshots folgen in Kürze._

## Installation

1. Öffne auf dem Handy das [neueste Release](https://github.com/navaikashalot-ops/WoomanCalendar/releases/latest) und lade `WoomanCalendar-<N>.apk` herunter.
2. Öffne die Datei. Erlaube bei Nachfrage die Installation aus dieser Quelle (Browser oder Dateimanager): *Einstellungen → Apps → Spezieller App-Zugriff → Unbekannte Apps installieren*.
3. Tippe auf **Installieren** und öffne die App.
4. **Aktualisieren:** Neue APK einfach über die alte installieren. **Nicht deinstallieren** — sonst gehen die lokalen Daten verloren.

Benötigt Android 8.0 (API 26) oder neuer.

## Sicherung in Google Drive (einmalig)

1. In der App: **Einstellungen** → **Sicherungsdatei erstellen** → im Systemdialog *Google Drive* wählen → `cycle-backup.json` speichern.
2. Danach wird die Datei nach jeder Änderung automatisch aktualisiert.
3. Neues Handy oder Neuinstallation: **Einstellungen → Aus Datei wiederherstellen** → `cycle-backup.json` auf Drive wählen.

Auch Androids Auto Backup legt eine Kopie an, die Wiederherstellung ist bei APKs außerhalb von Google Play aber nicht garantiert — verlasse dich auf die Drive-Datei.

## Selbst bauen (GitHub Actions)

1. Forke dieses Repository.
2. Einmalig Signaturschlüssel erzeugen: `bash scripts/make-keystore.sh` (JDK nötig). **Bewahre `mycycle.jks` und das Passwort sicher auf** — mit einem anderen Schlüssel lässt sich kein Update über die alte App installieren, lokale Daten gehen verloren.
3. Repository-Secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS` anlegen (das Skript gibt die Befehle aus).
4. Auf `main` pushen oder den Workflow **Build APK** starten. Die APK erscheint unter *Releases*.

Stack: Kotlin, Jetpack Compose, Room, kotlinx.serialization.

## Datenschutz

Keine Netzwerkberechtigungen, keine Analyse. Die Daten liegen im privaten App-Speicher und in der Sicherungsdatei, die du auswählst.
