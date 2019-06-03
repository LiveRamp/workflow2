#!/usr/bin/env bash

DIR="${0%/*}"

# wait for mysql
source "$DIR/block-on-db.sh"

# set up the database files from environment vars
sed -e "s/\${DB_USERNAME}/$DB_USERNAME/" \
  -e "s/\${DB_PASSWORD}/$DB_PASSWORD/" \
  -e "s/\${DB_HOSTNAME}/$DB_HOSTNAME/" \
  -e "s/\${DB_PORT}/$DB_PORT/" \
  /apps/workflow_db/config/database.yml.tpl \
  > /apps/workflow_db/config/database.yml


# set up the rails db
export RAILS_ENV=docker_env
cd /apps/workflow_db/

echo "Creating database..."
bundle exec rake db:create

echo "Running migrations..."
bundle exec rake db:migrate

echo "Migrations complete"