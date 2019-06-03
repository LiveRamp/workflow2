FROM centos:centos7

RUN yum -y install centos-release-scl

RUN yum -y install rh-ruby23 rh-ruby23-ruby-devel

SHELL [ "/usr/bin/scl", "enable", "rh-ruby23" ]

RUN yum -y install make gcc mysql-devel gcc-c++ mysql

RUN gem install bundler

RUN mkdir -p /apps

COPY database/ /apps/workflow_db

RUN mkdir -p /apps/workflow_db/config/

RUN cd /apps/workflow_db && bundle install

COPY container/migration/entrypoint.sh /apps/

COPY container/block-on-db.sh /apps/

ENTRYPOINT /apps/entrypoint.sh
