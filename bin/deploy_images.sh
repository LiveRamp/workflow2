#!/usr/bin/env bash

set -euo pipefail

docker pull centos:centos7

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

# Workflow UI container
WORKFLOW_UI_REPO="liveramp/workflow2_ui"
pushd workflow_ui
docker build -t workflow2_ui:latest -f Dockerfile .
docker tag workflow2_ui ${WORKFLOW_UI_REPO}
docker push ${WORKFLOW_UI_REPO}
popd

# Workflow monitor container
WORKFLOW_MONITOR_REPO="liveramp/workflow2_monitor"
pushd workflow_monitor
docker build -t workflow2_monitor:latest -f Dockerfile .
docker tag workflow2_monitor ${WORKFLOW_MONITOR_REPO}
docker push ${WORKFLOW_MONITOR_REPO}
popd
