#!/bin/bash

set -x -e

SCRIPT_LOCATION=`dirname $0`

# Source az-acr helper if available
if [ -f "$SCRIPT_LOCATION/../buildTools/common-build-tools/scripts/lib-sh/az-acr.sh" ]; then
  . $SCRIPT_LOCATION/../buildTools/common-build-tools/scripts/lib-sh/az-acr.sh
fi


if [ "$(uname)" != Linux ]; then
  echo "This script only runs on Linux"
  exit 1
fi

which docker >& /dev/null
if [ $? -ne 0 ]; then
  echo "Please install Docker: https://docs.docker.com/install/linux/docker-ce/centos/#install-using-the-repository"
  exit 1
fi

if [ $# -lt 2 ]; then
  echo "Usage: $0 <version> <date>"
  exit 1
fi

checkRC() {
  if [ $? -ne 0 ]; then
    rm -f *.rpm
    exit 1
  fi
}

removeDockerImages() {
  docker rmi -f $(docker images -q --filter "label=org.opencontainers.image.title=WebAPI" | sort -u)
}

VERSION=$1
DATE=$2
REPO="builds/webapi-platform"
LATEST_TAG="latest"
IMAGENAME="${DOCKER_REGISTRY:?Set DOCKER_REGISTRY env var}/$REPO:$LATEST_TAG"

RPM_DIR="../webapi-webapp-war/target/rpm/nuance-webapi-platform/RPMS/noarch"
RPM="nuance-webapi-platform-$VERSION-$DATE.el7.noarch.rpm"

if [ ! -f ${RPM_DIR}/$RPM ]; then
  echo "Cannot find ${RPM_DIR}/$RPM"
  echo "Please run \"mvn -P rpm clean install\" prior to running this script"
  exit 1
fi

cp ${RPM_DIR}/$RPM .

export DOCKER_CONTENT_TRUST=0
#Update mcr.microsoft.com/cbl-mariner/base/core:2.0, but allow to fail in case of pull rate limits
docker pull mcr.microsoft.com/cbl-mariner/base/core:2.0 || true
export DOCKER_CONTENT_TRUST=1

if [[ -v JENKINS_CLOUD_ID ]]; then
  docker build --no-cache -f Dockerfile -t $IMAGENAME \
         --build-arg BUILD_SYSTEM="$JENKINS_CLOUD_ID" \
         --build-arg BUILD_ID="$BUILD_ID" \
         --build-arg PRODUCT_VERSION="$VERSION-$BUILD_LONGDATE" \
         --build-arg VCS_ID="$GIT_COMMIT" \
         --build-arg VCS_BRANCH="$BRANCH_NAME" \
         --build-arg BUILD_URL="$BUILD_URL" .
elif [[ -v BUILD_SOURCESDIRECTORY ]]; then
  docker build --no-cache -f Dockerfile -t $IMAGENAME \
         --build-arg BUILD_SYSTEM="$AGENT_NAME" \
         --build-arg BUILD_ID="$BUILD_BUILDID" \
         --build-arg PRODUCT_VERSION="$VERSION-$BUILD_LONGDATE" \
         --build-arg VCS_ID="$BUILD_SOURCEVERSION" \
         --build-arg VCS_BRANCH="$BUILD_SOURCEBRANCH" \
         --build-arg BUILD_URL="${SYSTEM_TEAMFOUNDATIONCOLLECTIONURI}/${SYSTEM_TEAMPROJECT}/_build/results?buildId=${BUILD_BUILDID}" .
else
  docker build --no-cache -f Dockerfile -t $IMAGENAME .
fi
checkRC

rm -f $RPM

VERSIONED_TAG="$VERSION-$DATE"
VERSIONED_IMAGENAME_SUFFIX="$REPO:$VERSION-$DATE"
VERSIONED_IMAGENAME="$NCR_REGISTRY/$VERSIONED_IMAGENAME_SUFFIX"

#Tag with the version from the nuance-webapi-platform RPM
docker tag $IMAGENAME $VERSIONED_IMAGENAME

if [[ ! -v JENKINS_CLOUD_ID && ! -v BUILD_SOURCESDIRECTORY ]]; then
  exit 0
fi

#End here if this is a merge request test build
if [ ! -z $gitlabBranch ]; then
  removeDockerImages
  exit 0
fi

mkdir -p $DOCKER_CONFIG/trust/private
cp -r $HOME/.docker/trust/private/* $DOCKER_CONFIG/trust/private/

docker push $VERSIONED_IMAGENAME
docker push $IMAGENAME

#REGISTRY=entrdapi
REGISTRY=${AZURE_REGISTRY_SHORT:?Set AZURE_REGISTRY_SHORT env var}
REPOSITORY=builds/webapi-platform

echo "Cleanup Untagged Images from Azure Container Registry:"
az acr repository show-manifests --name $REGISTRY --repository $REPOSITORY  --query "[?tags[0]==null].digest" -o tsv \
   | xargs -I% az acr repository delete --name $REGISTRY --image $REPOSITORY@% --yes || true

echo "Azure Container Registry Remaining Webapi Images:"
az acr repository show-manifests --name $REGISTRY --repository $REPOSITORY
