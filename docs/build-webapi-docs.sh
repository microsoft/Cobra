#!/usr/bin/env bash

# Build the webapi docs
# Must be run after unit test execution, to ensure that the config and error code input generator have been run
# Invoked during the build of  webapi-webapp-war in the prepare-package phase, since the doc is
# included in the war file.

#End here if this is a merge request test build
if [ ! -z $gitlabBranch ]; then
  exit 0
fi

SCRIPT_LOCATION=$(dirname $0)
SCRIPT_LOCATION=$(realpath $SCRIPT_LOCATION)

pushd ${SCRIPT_LOCATION}
GIT_ROOT=`git rev-parse --show-toplevel`
popd

cd $GIT_ROOT/docs

GUIDES="$(find $GIT_ROOT/docs/webapi-guides/* -maxdepth 0)"
echo $GUIDES

# Disabling docs for now.
#for doc in $GUIDES
#do
#  echo Building guide $doc
#  $SCRIPT_LOCATION/build-doc.sh $doc || exit 2
#done
