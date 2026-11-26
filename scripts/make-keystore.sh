#!/usr/bin/env bash
# Создаёт ключ подписи ОДИН РАЗ. Сохраните mycycle.jks и пароль в надёжном месте (менеджер паролей, облако):
# потеряете ключ — новый билд не встанет поверх старого, придётся удалять приложение и данные пропадут.
#
# Использование:
#   bash scripts/make-keystore.sh                  # keytool ищется автоматически
#   bash scripts/make-keystore.sh --keytool PATH    # указать keytool вручную
#   bash scripts/make-keystore.sh --alias mycycle   # другой alias (по умолчанию mycycle)
#
# keytool входит в JDK и обычно НЕ попадает в PATH (в том числе в Git Bash / MSYS2 UCRT64).
# Поэтому скрипт сам перебирает типовые места: JAVA_HOME, Android Studio (jbr), JetBrains,
# DBeaver, каталоги JDK в Program Files, ~/.jdks, MSYS2. Если ничего не нашлось — подскажет,
# что установить.
set -euo pipefail

ALIAS="mycycle"
KEYSTORE="mycycle.jks"
OUT_B64="keystore-base64.txt"
KEYTOOL="${KEYTOOL:-}"

die() {
  printf '\nОшибка: %s\n' "$*" >&2
  exit 1
}

usage() {
  cat <<'EOF'
Создаёт ключ подписи Android (mycycle.jks) и его base64-копию для секрета KEYSTORE_BASE64.

Использование:
  bash scripts/make-keystore.sh [--keytool PATH] [--alias NAME] [-h|--help]

  --keytool PATH   путь к keytool из JDK (если скрипт его не нашёл)
  --alias NAME     alias ключа, по умолчанию mycycle

Переменные окружения: JAVA_HOME, KEYTOOL.
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
    --keytool)
      [ $# -ge 2 ] || die "после --keytool нужен путь к keytool"
      KEYTOOL="$2"
      shift 2
      ;;
    --alias)
      [ $# -ge 2 ] || die "после --alias нужен alias"
      ALIAS="$2"
      shift 2
      ;;
    -h | --help)
      usage
      exit 0
      ;;
    *)
      die "неизвестный аргумент: $1 (см. --help)"
      ;;
  esac
done

# Windows-путь (C:\Foo) -> путь, понятный bash (/c/Foo), если есть cygpath.
winpath() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -u "$1" 2>/dev/null || printf '%s\n' "$1"
  else
    printf '%s\n' "$1"
  fi
}

# Печатает каталоги bin, где может лежать keytool.
keytool_bin_dirs() {
  local var val
  for var in JAVA_HOME JDK_HOME ANDROID_JAVA_HOME JAVA_HOME_17_X64; do
    val="${!var-}"
    if [ -n "$val" ]; then
      printf '%s\n' "$(winpath "$val")/bin"
    fi
  done

  local localapp
  localapp="$(winpath "${LOCALAPPDATA:-NONE}")"

  local had_nullglob
  had_nullglob="$(shopt -p nullglob || true)"
  shopt -s nullglob
  local dirs=(
    "/c/Program Files/Java"/*/bin
    "/c/Program Files/Eclipse Adoptium"/*/bin
    "/c/Program Files/Microsoft"/jdk*/bin
    "/c/Program Files/Amazon Corretto"/*/bin
    "/c/Program Files/Zulu"/*/bin
    "/c/Program Files/BellSoft"/*/bin
    "/c/Program Files/Android/Android Studio/jbr/bin"
    "/c/Program Files/Android/Android Studio/jre/bin"
    "/c/Program Files/JetBrains"/*/jbr/bin
    "$HOME"/.jdks/*/bin
    "$HOME"/.sdkman/candidates/java/current/bin
    "$localapp"/DBeaver/jre/bin
    "$localapp"/JetBrains/*/jbr/bin
    "$localapp"/Programs/*/jbr/bin
    "$localapp"/Programs/*/jre/bin
    "$localapp"/Programs/*/bin
    "$localapp"/Android/Sdk/jbr/bin
    "/usr/lib/jvm"/*/bin
    "/Library/Java/JavaVirtualMachines"/*/Contents/Home/bin
    "/opt/homebrew/opt/openjdk"*/bin
    "/ucrt64/bin"
    "/mingw64/bin"
    "/clang64/bin"
  )
  eval "${had_nullglob:-shopt -u nullglob}"

  local d
  for d in "${dirs[@]}"; do
    printf '%s\n' "$d"
  done

  # Явные подсказки из gradle.properties (org.gradle.java.home).
  local gp
  for gp in "${HOME:-/nonexistent}/.gradle/gradle.properties" \
    "$(dirname "$0")/../gradle.properties"; do
    if [ -f "$gp" ]; then
      local home
      home="$(sed -n 's/^[[:space:]]*org\.gradle\.java\.home[[:space:]]*=[[:space:]]*//p' "$gp" | tail -n 1)"
      if [ -n "$home" ]; then
        printf '%s\n' "$(winpath "$home")/bin"
      fi
    fi
  done
}

find_keytool() {
  if [ -n "$KEYTOOL" ]; then
    [ -x "$KEYTOOL" ] || die "по пути KEYTOOL=$KEYTOOL нет исполняемого keytool"
    printf '%s\n' "$KEYTOOL"
    return 0
  fi

  if command -v keytool >/dev/null 2>&1; then
    command -v keytool
    return 0
  fi

  local d
  while IFS= read -r d; do
    [ -n "$d" ] || continue
    if [ -x "$d/keytool" ]; then
      printf '%s\n' "$d/keytool"
      return 0
    fi
    if [ -x "$d/keytool.exe" ]; then
      printf '%s\n' "$d/keytool.exe"
      return 0
    fi
  done < <(keytool_bin_dirs)

  return 1
}

no_keytool_help() {
  cat >&2 <<'EOF'
keytool не найден — а он входит в JDK, без Java ключ подписи не создать.
(В Git Bash / MSYS2 keytool не попадает в PATH даже при установленном JDK.)

Установите JDK 17, любой из вариантов:

  Windows (winget):  winget install -e --id EclipseAdoptium.Temurin.17.JDK
  Windows (MSYS2):   pacman -S mingw-w64-ucrt-x86_64-jdk
  macOS:             brew install --cask temurin@17
  Debian/Ubuntu:     sudo apt install openjdk-17-jdk-headless

Затем закройте и откройте терминал заново (PATH обновится) и запустите скрипт снова.

Если JDK уже установлен, но скрипт его не видит — укажите keytool напрямую:

  bash scripts/make-keystore.sh --keytool "/c/Program Files/Java/jdk-17/bin/keytool.exe"

Подойдёт и Java, встроенная в другую программу (Android Studio, IntelliJ, DBeaver):
путь оканчивается на .../jbr/bin/keytool.exe или .../jre/bin/keytool.exe.
EOF
}

KEYTOOL_BIN="$(find_keytool || true)"
if [ -z "$KEYTOOL_BIN" ]; then
  no_keytool_help
  exit 2
fi

printf 'keytool: %s\n' "$KEYTOOL_BIN"

# Существующий ключ не перезаписываем: новый ключ = сломанные обновления приложения.
if [ -e "$KEYSTORE" ]; then
  die "файл $(pwd)/$KEYSTORE уже существует.
Этот ключ уже создан, и пересоздавать его нельзя: APK с другим ключом не встанет
поверх установленного приложения, и данные придётся потерять.
Если нужен base64 от существующего ключа — сделайте его вручную:
  base64 < $KEYSTORE | tr -d '\\n' > $OUT_B64
Удалять $KEYSTORE можно только если вы уверены, что старое приложение вам больше не нужно."
fi

# Пароль спрашиваем дважды: опечатка сделает ключ бесполезным.
printf 'Придумайте пароль ключа (мин. 6 символов): '
IFS= read -r -s PASS || die "не удалось прочитать пароль: запустите скрипт в терминале, а не через пайп"
printf '\nПовторите пароль: '
IFS= read -r -s PASS2 || die "не удалось прочитать пароль повторно"
printf '\n'

[ "${#PASS}" -ge 6 ] || die "пароль короче 6 символов — keytool такой не примет"
[ "$PASS" = "$PASS2" ] || die "пароли не совпадают, попробуйте снова"
unset PASS2

printf '\nГенерирую ключ (RSA 2048, срок 100 лет)...\n'
"$KEYTOOL_BIN" -genkeypair -v -noprompt \
  -keystore "$KEYSTORE" -alias "$ALIAS" \
  -keyalg RSA -keysize 2048 -validity 36500 \
  -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=MyCycle, O=Private, C=RU" || die "keytool не смог создать ключ (сообщение выше)"

[ -s "$KEYSTORE" ] || die "файл $KEYSTORE не создан"

# Проверяем, что ключ читается именно этим паролем и alias.
"$KEYTOOL_BIN" -list -keystore "$KEYSTORE" -storepass "$PASS" -alias "$ALIAS" >/dev/null 2>&1 ||
  die "ключ создан, но не читается с указанным паролем. Удалите $KEYSTORE и запустите скрипт снова"

FINGERPRINT="$("$KEYTOOL_BIN" -J-Duser.language=en -J-Duser.country=US \
  -list -v -keystore "$KEYSTORE" -storepass "$PASS" -alias "$ALIAS" 2>/dev/null |
  sed -n 's/.*SHA256: *//p' | head -n 1 | tr -d ' ')"

# base64: GNU base64 переносит строки, BSD — нет; убираем переносы в обоих случаях.
if command -v base64 >/dev/null 2>&1; then
  base64 <"$KEYSTORE" | tr -d '\r\n' >"$OUT_B64"
else
  die "не найден base64. Сделайте копию вручную в PowerShell:
  [Convert]::ToBase64String([IO.File]::ReadAllBytes('$KEYSTORE')) | Set-Content -NoNewline $OUT_B64"
fi

[ -s "$OUT_B64" ] || die "не удалось закодировать $KEYSTORE в base64"

cat <<EOF

Готово.
  ключ:            $(pwd)/$KEYSTORE
  base64 для CI:   $(pwd)/$OUT_B64
  alias:           $ALIAS
EOF
if [ -n "$FINGERPRINT" ]; then
  printf '  отпечаток:       %s\n' "$FINGERPRINT"
fi
cat <<'EOF'

Отпечаток (SHA-256) сохраните рядом с паролем: по нему потом можно проверить,
что APK подписан тем же ключом:
  keytool -printcert -jarfile app-release.apk

Добавьте секреты в репозиторий (Settings -> Secrets and variables -> Actions)
или выполните в папке репозитория (нужен gh CLI):

  gh secret set KEYSTORE_BASE64 < keystore-base64.txt
  gh secret set KEYSTORE_PASSWORD     # значение вставьте по запросу
  gh secret set KEY_PASSWORD          # то же самое
EOF
printf "  gh secret set KEY_ALIAS --body '%s'\n" "$ALIAS"
cat <<'EOF'

Пароль через --body не передавайте: он останется в истории команд.
Пароль и файл ключа сохраните в менеджере паролей. Файлы mycycle.jks и
keystore-base64.txt в git НЕ коммитить (они уже в .gitignore).
EOF
