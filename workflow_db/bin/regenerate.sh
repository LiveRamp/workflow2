#!/bin/bash

set -e
set -o pipefail

rvm use 2.3.1

export GEM_HOME=gems/

BASEDIR=$1

echo "Using ruby version:"

ruby --version

which ruby

echo "Basedir: $BASEDIR"

cd ${BASEDIR}

echo "Creating directories"
DUMP_DEST="$BASEDIR/target/classes/com/liveramp/db_schemas"
CLASS_DEST="$BASEDIR/target/classes"
mkdir -p "$DUMP_DEST"
mkdir -p "$CLASS_DEST"


DB_DUMP_PATH="workflow_db.dump"

echo "Removing dump path $DB_DUMP_PATH"
rm -f ${DB_DUMP_PATH}

# init-db using bundle
echo "Running init-db"

cd "$BASEDIR/database/"

echo "Bundle installing"
bundle install

echo "Migrating"
export RAILS_ENV=regenerate
bundle exec rake db:drop db:create
bundle exec rake db:migrate

# generate schema dump
echo "Generating schema dump"

cd ${BASEDIR}

echo "Dumping with user root, pass , db workflow_db_regenerate, port 31313 to path $DB_DUMP_PATH"
mysqldump --host 127.0.0.1 --user=root --port=31313 --no-data workflow_db_regenerate --routines | sed -e 's/DEFINER[ ]*=[ ]*`[^`]*`@`[^`]*`//g' > ${DB_DUMP_PATH}

echo "Copying db dump from ${DB_DUMP_PATH} to ${DUMP_DEST}"
cp ${DB_DUMP_PATH} ${DUMP_DEST}
mv ${DB_DUMP_PATH} ${CLASS_DEST}

# generate jack models
echo "Generating jack models"

cd ${BASEDIR}

BUILD_DIR="/tmp/workflow_db-build/jack-tmp";
echo "Using temporary dir $BUILD_DIR"
rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}

echo "Unzipping dependencies"
unzip './target/dependency/*.jar' -d ${BUILD_DIR}

echo "Removing old autogenerated files"
grep -H -r 'Autogenerated by Jack' ${BASEDIR}/src/main/java | cut -d: -f1 | grep '.\.java$' | xargs rm -f || true

echo "Moving to build directory"
cd ${BUILD_DIR};

echo "Bundle installing"
bundle install --verbose

echo "Generating jack models"
bundle exec ruby rb/jack.rb ${BASEDIR}/project.yml ${BASEDIR}/src/main/java

echo "Done!"