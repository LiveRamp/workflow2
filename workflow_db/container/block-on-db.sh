#!/usr/bin/env bash

function mysql_conn_error {
  mysql -h ${DB_HOSTNAME} -P ${DB_PORT} -u ${DB_USERNAME} --password=${DB_PASSWORD} -e "show databases"
  return $?
}

# wait until mysql is running
while true
do

  mysql_conn_error
  RES=$?
  echo "Got result: $RES"
  if [ "$RES" == "0" ]; then
    echo "MySQL started"
    break
  fi

  echo "Waiting for MySQL to start"
  sleep 1

done
