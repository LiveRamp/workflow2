FROM centos:centos7

RUN yum -y install java-1.8.0-openjdk

RUN mkdir -p /apps/workflow_monitor/

COPY target/workflow_monitor.job.jar /apps/workflow_monitor/

RUN mkdir -p /apps/workflow_monitor/config/

RUN ln -sf /apps/secrets/workflow_svc/database.yml /apps/workflow_monitor/config/database.yml && \
    ln -sf /apps/secrets/workflow_svc/environment.yml /apps/workflow_monitor/config/environment.yml

COPY container/entrypoint.sh /apps/

WORKDIR /apps/workflow_monitor

ENTRYPOINT /apps/entrypoint.sh
