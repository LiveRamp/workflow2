#!/usr/bin/env bash

set -e

docker_login() {
    local restore_x
    if [[ $- =~ x ]]; then
      restore_x=true
    else
      restore_x=false
    fi

    # Disable xtrace to hide password ($DOCKER_PASS).
    set +x
    echo "${DOCKER_PASS}" | docker login -u ${DOCKER_USER} --password-stdin
    if [[ ${restore_x} ]]; then
        set -x
    fi
}

docker_login

MIGRATION_REPO=liveramp/workflow2_db_migrations
docker build -t db_migrations:latest -f container/migration/Dockerfile .
docker tag db_migrations ${MIGRATION_REPO}
docker push ${MIGRATION_REPO}

SQLDUMP_REPO=liveramp/workflow2_sqldump
docker build -t sqldump:latest -f container/sqldump/Dockerfile .
docker tag sqldump ${SQLDUMP_REPO}
docker push ${SQLDUMP_REPO}
