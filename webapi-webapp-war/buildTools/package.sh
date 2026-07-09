#!/bin/sh

CONFIG_PATH=$1
TARGET_PATH=$2

# target/package/webapps is created and populated with the maven-dependency-plugin.

#Copy the config/ folder
cp -r "${CONFIG_PATH}" "${TARGET_PATH}"

#Create the tenants/ folder and tenants and config links in the jetty base folders
pushd `pwd` >& /dev/null
cd "${TARGET_PATH}"
mkdir -p tenants
mkdir -p wav

for instance in 1 2 3 4; do
  mkdir -p nina-webapi-base-$instance
  cd nina-webapi-base-$instance
  ln -f -s ../tenants .
  ln -f -s ../config .
  ln -f -s ../wav .
  cd ..
done
popd >& /dev/null

