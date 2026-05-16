#!/bin/bash

set -euo pipefail

STUDENT_ID="23338"
PASSWORD="6semStepchenkovBKS2302"
COUNTRY="RU"
ORG="MTUCI"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_FILE="$SCRIPT_DIR/signing-keystore.p12"
KEY_ALIAS="ticket_signing"

rm -f "$KEYSTORE_FILE"

keytool -genkeypair \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -sigalg SHA256withRSA \
  -validity 3650 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE_FILE" \
  -storepass "$PASSWORD" \
  -keypass "$PASSWORD" \
  -dname "CN=rbpo2-ticket-signing, OU=$STUDENT_ID, O=$ORG, C=$COUNTRY"

echo "Done"
echo "File: $KEYSTORE_FILE"
echo "Alias: $KEY_ALIAS"
echo "Password: $PASSWORD"
echo "Public cert (PEM):"
keytool -exportcert -rfc -alias "$KEY_ALIAS" -keystore "$KEYSTORE_FILE" -storetype PKCS12 -storepass "$PASSWORD"