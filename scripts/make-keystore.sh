#!/usr/bin/env bash
# Создаёт ключ подписи ОДИН РАЗ. Сохраните mycycle.jks и пароль в надёжном месте (менеджер паролей, облако):
# потеряете ключ — новый билд не встанет поверх старого, придётся удалять приложение и данные пропадут.
set -euo pipefail

ALIAS="mycycle"
read -r -s -p "Придумайте пароль ключа (мин. 6 символов): " PASS
echo
keytool -genkeypair -v -keystore mycycle.jks -alias "$ALIAS" \
  -keyalg RSA -keysize 2048 -validity 36500 \
  -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=MyCycle, O=Private, C=RU"

base64 -w0 mycycle.jks > keystore-base64.txt 2>/dev/null || base64 mycycle.jks | tr -d '\n' > keystore-base64.txt

cat <<EOF

Готово. Теперь добавьте секреты в репозиторий (Settings -> Secrets and variables -> Actions)
или выполните в папке репозитория (нужен gh CLI):

  gh secret set KEYSTORE_BASE64 < keystore-base64.txt
  gh secret set KEYSTORE_PASSWORD --body '<ваш пароль>'
  gh secret set KEY_PASSWORD      --body '<ваш пароль>'
  gh secret set KEY_ALIAS         --body '$ALIAS'

Файлы mycycle.jks и keystore-base64.txt в git НЕ коммитить (они уже в .gitignore).
EOF
