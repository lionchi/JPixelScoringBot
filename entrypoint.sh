#!/bin/sh

keytool -import \
-alias vault \
-storepass changeit \
-keystore $JAVA_HOME/jre/lib/security/cacerts \
-noprompt \
-trustcacerts \
-file /workspace/certs/selfsigned.crt

exec java -jar app.jar