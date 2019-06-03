#!/usr/bin/env bash

set -euo pipefail

DIR="${0%/*}"
export COMPOSE_PROJECT_NAME=workflow_test

docker-compose -f "$DIR/test-db-compose.yml" -v down