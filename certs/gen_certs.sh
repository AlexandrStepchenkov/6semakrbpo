#!/bin/bash

STUDENT_ID="23338"
PASSWORD="6semStepchenkovBKS2302"
COUNTRY="RU"
ORG="MTUCI"

rm -f *.pem *.srl *.csr *.p12 *.jks

echo "Генерируем Root CA (Корневой сертификат)"
openssl req -x509 -newkey rsa:4096 -days 365 -nodes \
  -keyout rootCA.key -out rootCA.pem \
  -subj "/C=$COUNTRY/O=$ORG/OU=$STUDENT_ID/CN=rbpoCA"

echo "Генерируем Intermediate CA (Промежуточный)"
openssl req -newkey rsa:4096 -days 365 -nodes \
  -keyout interCA.key -out interCA.csr \
  -subj "/C=$COUNTRY/O=$ORG/OU=$STUDENT_ID/CN=rbpoIntermediateCA"

openssl x509 -req -in interCA.csr -CA rootCA.pem -CAkey rootCA.key \
  -CAcreateserial -out interCA.pem -days 365

echo "Генерируем Server Certificate"
openssl req -newkey rsa:4096 -days 365 -nodes \
  -keyout server.key -out server.csr \
  -subj "/C=$COUNTRY/O=$ORG/OU=$STUDENT_ID/CN=localhost"

openssl x509 -req -in server.csr -CA interCA.pem -CAkey interCA.key \
  -CAcreateserial -out server.pem -days 365

echo "Создаем цепочку и упаковываем в PKCS12"
cat server.pem interCA.pem rootCA.pem > full_chain.pem

openssl pkcs12 -export \
  -in full_chain.pem \
  -inkey server.key \
  -out keystore.p12 \
  -name "laba6_key" \
  -passout pass:$PASSWORD

echo "Done"
echo "Файл для проекта: keystore.p12"
echo "Пароль: $PASSWORD"
echo "Файл для браузера (Доверенный): rootCA.pem"