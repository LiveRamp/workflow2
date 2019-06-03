#!/usr/bin/env bash

set -e

DIR="/tmp/workflow2_types_gem/"

rm -rf ${DIR}

mkdir -p ${DIR}/lib
cp gen-rb/* ${DIR}/lib
cp workflow2_types.gemspec ${DIR}

cd ${DIR}

for f in `ls lib/*`
do
  sed -i '' '/thrift/!s/^require /require_relative /g' $f
done

gem build workflow2_types.gemspec

echo "Gem built in $DIR"
