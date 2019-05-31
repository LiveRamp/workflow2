#!/usr/bin/env bash

DIR="${0%/*}"

# wait for mysql

echo "Waiting for mysql to start"
source "$DIR/block-on-db.sh"

echo "Creating database"
mysql -h ${DB_HOSTNAME} -P ${DB_PORT} -u ${DB_USERNAME} --password=${DB_PASSWORD} -e "create database workflow_docker_env"

echo "Dumping records"
mysql -h ${DB_HOSTNAME} -P ${DB_PORT} -u ${DB_USERNAME} --password=${DB_PASSWORD} workflow_docker_env < /apps/workflow_db.sql