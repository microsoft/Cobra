#!/usr/bin/env bash

SCRIPT_DIR=$(dirname $0)
ABS_SCRIPT_DIR=$(realpath $SCRIPT_DIR)

BUILD_DIR=${ABS_SCRIPT_DIR}/build

if [[ $# -ne 1 ]]; then
  echo Usage: $(basename $0) "<doc-dir>"
  exit 1
fi

DOC_DIR=$1
if [[ ! -d $DOC_DIR ]]; then
  echo Invalid doc directory: $DOC_DIR directory not found.
  exit 2
fi

pushd $SCRIPT_DIR/..
GIT_ROOT=`git rev-parse --show-toplevel`
popd

DOC_ABS_PATH=$(realpath ${DOC_DIR})
DOC_REL_PATH=$(echo ${DOC_ABS_PATH} | sed s@^${GIT_ROOT}/@@g)

DOC_BUILD_DIR=$BUILD_DIR/$DOC_REL_PATH
cd $DOC_BUILD_DIR
bundle exec middleman server
