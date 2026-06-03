#!/bin/bash
set -e

DS_NAME="${DATASOURCE_NAME:-LocauxDS}"
DB_HOST="${DB_HOST:-locaux-db}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-locaux_db}"
DB_USER="${DB_USER:-locaux_user}"
DB_PASSWORD="${DB_PASSWORD:-locaux_pass}"
JDBC_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}"

CLI_FILE="/tmp/configure-datasource.cli"
cat > "${CLI_FILE}" <<EOF
embed-server --server-config=standalone.xml --std-out=discard
/subsystem=datasources/jdbc-driver=mysql:add(driver-name=mysql,driver-module-name=com.mysql,driver-class-name=com.mysql.cj.jdbc.Driver)
try
/subsystem=datasources/data-source=${DS_NAME}:remove
catch
end-try
/subsystem=datasources/data-source=${DS_NAME}:add(jndi-name=java:jboss/datasources/${DS_NAME},driver-name=mysql,connection-url="${JDBC_URL}",user-name=${DB_USER},password=${DB_PASSWORD},use-java-context=true,enabled=true,validate-on-match=true,background-validation=false)
/subsystem=datasources/data-source=${DS_NAME}/connection-properties=useSSL:add(value=false)
/subsystem=datasources/data-source=${DS_NAME}/connection-properties=serverTimezone:add(value=UTC)
/subsystem=datasources/data-source=${DS_NAME}/connection-properties=allowPublicKeyRetrieval:add(value=true)
stop-embedded-server
EOF

echo "Configuration offline de la source de donnees ${DS_NAME}..."
/opt/jboss/wildfly/bin/jboss-cli.sh --file="${CLI_FILE}"

echo "Demarrage de WildFly..."
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
