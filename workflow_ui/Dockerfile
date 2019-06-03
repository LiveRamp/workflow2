FROM centos:centos7

RUN yum -y install java-1.8.0-openjdk

RUN mkdir -p /apps/workflow_ui/

RUN mkdir -p /var/www/tmp

COPY target/workflow_ui.job.jar /apps/workflow_ui/

RUN mkdir -p /apps/workflow_ui/config/

RUN ln -sf /apps/secrets/workflow_svc/database.yml /apps/workflow_ui/config/database.yml && \
    ln -sf /apps/secrets/workflow_svc/environment.yml /apps/workflow_ui/config/environment.yml

COPY container/entrypoint.sh /apps/

ENTRYPOINT /apps/entrypoint.sh
