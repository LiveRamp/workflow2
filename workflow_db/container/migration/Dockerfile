FROM centos:centos7

RUN yum -y install ruby ruby-devel make gcc mysql-devel gcc-c++ mysql && gem install bundler

RUN mkdir -p /apps

COPY database/ /apps/workflow_db

RUN mkdir -p /apps/workflow_db/config/

RUN cd /apps/workflow_db && bundle install

COPY container/migration/entrypoint.sh /apps/

COPY container/block-on-db.sh /apps/

ENTRYPOINT /apps/entrypoint.sh
