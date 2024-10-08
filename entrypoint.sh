#!/bin/sh

echo "Импорт сертификата в $JAVA_HOME/jre/lib/security/cacerts"

keytool -import \
-alias vault \
-storepass changeit \
-keystore $JAVA_HOME/jre/lib/security/cacerts \
-noprompt \
-trustcacerts \
-file /workspace/certs/selfsigned.crt

echo "Сертификат успешно импортирован"

exec java -jar app.jar