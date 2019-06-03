FROM centos:centos7

RUN yum -y install mysql

RUN mkdir -p /apps

COPY container/sqldump/entrypoint.sh /apps/

COPY container/block-on-db.sh /apps/

COPY target/classes/workflow_db.dump /apps/workflow_db.sql

ENTRYPOINT /apps/entrypoint.sh
