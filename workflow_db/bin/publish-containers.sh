#!/usr/bin/env bash

MIGRATION_REPO=liveramp/workflow2_db_migrations
docker build -t db_migrations:latest -f container/migration/Dockerfile .
docker tag db_migrations ${MIGRATION_REPO}
docker push ${MIGRATION_REPO}

SQLDUMP_REPO=liveramp/workflow2_sqldump
docker build -t sqldump:latest -f container/sqldump/Dockerfile .
docker tag sqldump ${SQLDUMP_REPO}
docker push ${SQLDUMP_REPO}
