#!/usr/bin/env bash

set -e

# get the java app running
cd /apps/workflow_ui/

# not really a work of art, could be done as ruby or java
JETTY_ENV=$( cat config/environment.yml | grep workflow_ui_jetty_db | awk -F: '{print $2}' | xargs )
DB_NAME=$( cat config/database.yml | sed -n -e "/^$JETTY_ENV:/,\$p" | grep database | awk -F: '{print $2}' | head -1 | xargs )
USERNAME=$( cat config/database.yml | sed -n -e "/^$JETTY_ENV:/,\$p" | grep username | awk -F: '{print $2}' | head -1 | xargs )
PASSWORD=$( cat config/database.yml | sed -n -e "/^$JETTY_ENV:/,\$p" | grep password | awk -F: '{print $2}' | head -1 | xargs )
HOST=$( cat config/database.yml | sed -n -e "/^$JETTY_ENV:/,\$p" | grep host | awk -F: '{print $2}' | head -1 | xargs )
PORT=$( cat config/database.yml | sed -n -e "/^$JETTY_ENV:/,\$p" | grep port | awk -F: '{print $2}' | head -1 | xargs )

# set up jetty session db if it doesn't exist already
mysql -h $HOST -P $PORT -u$USERNAME -p$PASSWORD -e "create database if not exists $DB_NAME" || true

java -Djava.io.tmpdir=/var/www/tmp -Xmx12000m  -Dlog4j.configuration=com/liveramp/workflow_ui/log4j/console.log4j.xml -Djava.net.preferIPv4Stack=true -cp workflow_ui.job.jar \
  com.liveramp.workflow_ui.WorkflowDbWebServer
