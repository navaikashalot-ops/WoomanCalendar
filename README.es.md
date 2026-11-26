# WoomanCalendar

[English](README.md) · [Русский](README.ru.md) · [Deutsch](README.de.md) · **Español**

Calendario menstrual privado para Android que funciona sin conexión. Marca el primer día de tu regla y el calendario prevé las próximas reglas, la ventana fértil y la ovulación. Sin cuenta, sin anuncios, sin analítica y sin servidores: tus datos se quedan en el teléfono y se duplican en **tu propio Google Drive**.

> Las previsiones son aproximadas; no son consejo médico ni un método anticonceptivo.

## Funciones

- Calendario mensual con regla, regla prevista, ventana fértil y ovulación
- Marca el inicio/fin de la regla con un toque; flujo, síntomas y notas por día
- Previsión a partir de los últimos 6 ciclos (los recientes pesan más)
- Los datos se conservan al actualizar la app (mismo nombre de paquete y misma clave de firma)
- Copia JSON automática en un archivo de tu Google Drive, sin registro en Google Cloud ni inicio de sesión en la app
- Idiomas: English, Русский, Українська, Deutsch, Español (según el idioma del sistema; idioma por app en Android 13+)

## Capturas de pantalla

<!-- Añade capturas reales en docs/screenshots/ (ver docs/screenshots/README.md) y descomenta:
<p>
  <img src="docs/screenshots/calendar.png" width="260" alt="Calendario">
  <img src="docs/screenshots/day.png" width="260" alt="Día">
  <img src="docs/screenshots/settings.png" width="260" alt="Ajustes y copia">
</p>
-->
_Las capturas llegarán pronto._

## Instalación

1. En el teléfono abre la [última versión](https://github.com/navaikashalot-ops/WoomanCalendar/releases/latest) y descarga `WoomanCalendar-<N>.apk`.
2. Abre el archivo. Si Android lo pide, permite instalar desde esta fuente (navegador o gestor de archivos): *Ajustes → Aplicaciones → Acceso especial → Instalar apps desconocidas*.
3. Pulsa **Instalar** y abre la app.
4. **Actualizar:** instala el nuevo APK sobre el anterior. **No desinstales** la app: al desinstalar se borran los datos locales.

Requiere Android 8.0 (API 26) o superior.

## Copia en Google Drive (una sola vez)

1. En la app: **Ajustes** → **Crear archivo de copia** → elige *Google Drive* en el selector del sistema → guarda `cycle-backup.json`.
2. A partir de ahora el archivo se actualiza solo tras cada cambio.
3. Teléfono nuevo o reinstalación: **Ajustes → Restaurar desde archivo** → elige `cycle-backup.json` en Drive.

La copia automática de Android (Auto Backup) también guarda una copia, pero su restauración no está garantizada para APK instalados fuera de Google Play: confía en el archivo de Drive.

## Compilar tú mismo (GitHub Actions)

1. Haz un fork de este repositorio.
2. Crea la clave de firma una vez: `bash scripts/make-keystore.sh` (requiere JDK). **Guarda `mycycle.jks` y su contraseña**: con otra clave no se podrá instalar una actualización sobre la app anterior y se perderán los datos locales.
3. Añade los secretos del repositorio `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS` (el script muestra los comandos).
4. Haz push a `main` o ejecuta el workflow **Build APK**. El APK aparecerá en *Releases*.

Stack: Kotlin, Jetpack Compose, Room, kotlinx.serialization.

## Privacidad

Sin permisos de red ni analítica. Los datos están en el almacenamiento privado de la app y en el archivo de copia que elijas.
