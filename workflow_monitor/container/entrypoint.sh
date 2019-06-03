#!/usr/bin/env bash

java -Xmx4096m -Dlog4j.configuration=com/liveramp/workflow2/workflow_monitor/log4j/console.log4j.xml -cp workflow_monitor.job.jar \
  com.liveramp.workflow_monitor.WorkflowDbMonitorRunner
