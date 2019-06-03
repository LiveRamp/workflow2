#!/usr/bin/env bash

set -euo pipefail
DIR="${0%/*}"

$DIR/start-mysql.sh

docker pull liveramp/workflow2_sqldump:latest
docker run --rm --name test_migrate --network=workflow_test_shared -e "DB_PORT=3306" -e "DB_USERNAME=root" -e "DB_HOSTNAME=mysql" liveramp/workflow2_sqldump:latest