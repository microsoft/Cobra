#!/bin/bash

IMAGE_PREFIX="webapi-platform"

#DOCKER_REGISTRY="your-registry.example.com"
#AZURE_REGISTRY_SHORT="entrdapi"

DOCKER_REGISTRY="${DOCKER_REGISTRY:?Set DOCKER_REGISTRY env var}"
AZURE_REGISTRY_SHORT="${AZURE_REGISTRY_SHORT:?Set AZURE_REGISTRY_SHORT env var}"

export DOCKER_CONTENT_TRUST=1

GIT_ROOT=$(git rev-parse --show-toplevel)
# Source az-acr helper if available
if [ -f "$GIT_ROOT/buildTools/common-build-tools/scripts/lib-sh/az-acr.sh" ]; then
  . $GIT_ROOT/buildTools/common-build-tools/scripts/lib-sh/az-acr.sh
fi



#############################
# Environment sanity checks
#############################
if [ "$(uname)" != Linux ]; then
  echo "This script only runs on Linux"
  exit 1
fi

which docker >& /dev/null
if [ $? -ne 0 ]; then
  echo "Please install Docker: https://docs.docker.com/install/linux/docker-ce/centos/#install-using-the-repository"
  exit 1
fi

#############################
# Methods
#############################

removeAzureUntaggedImages() {
  #
  # Remove Azure images
  #
  echo "Cleaning up Untagged Images from Azure Container Registry:"
  azureAcrDeleteUntaggedImages $1
}

listImages() {
  echo "Leftover Local Docker images:"
  docker images

  echo "Azure Container Registry Remaining Webapi Images:"
  az acr repository show-manifests --name $AZURE_REGISTRY_SHORT --repository $1
}

prepDockerBuild() {
  if [ -x prep-docker-build.sh ]; then
    ./prep-docker-build.sh
  fi
}

dockerBuild() {
  DB_PATH_IMAGENAME="${DOCKER_REGISTRY}/$1"
  DB_PATH_IMAGENAME_LATEST="${DB_PATH_IMAGENAME}:latest"
  if [ $BRANCH_NAME != "master" ]; then
    TIMESTAMP=$(date +%Y%m%d%H%M%S)
    DB_PATH_IMAGENAME_VERSIONED="${DB_PATH_IMAGENAME}:$VERSION-$BRANCH_NAME-$TIMESTAMP"
  else
    DB_PATH_IMAGENAME_VERSIONED="${DB_PATH_IMAGENAME}:$VERSION"
  fi
  docker build -f ./docker/Dockerfile -t ${DB_PATH_IMAGENAME_LATEST} .

  #Tag with the version from the nuance-webapi-platform RPM
  docker tag $DB_PATH_IMAGENAME_LATEST $DB_PATH_IMAGENAME_VERSIONED
}

dockerPush() {
  DP_PATH_IMAGENAME="${DOCKER_REGISTRY}/$1"
  DP_PATH_IMAGENAME_LATEST="${DP_PATH_IMAGENAME}:latest"
  if [ $BRANCH_NAME != "master" ]; then
    DP_PATH_IMAGENAME_VERSIONED="${DP_PATH_IMAGENAME}:$VERSION-$BRANCH_NAME-$TIMESTAMP"
  else
    DP_PATH_IMAGENAME_VERSIONED="${DP_PATH_IMAGENAME}:$VERSION"
    docker push ${DP_PATH_IMAGENAME_LATEST}
  fi

  docker push ${DP_PATH_IMAGENAME_VERSIONED}

  #Push to Azure
  #DP_AZURE_IMAGENAME_LATEST="${AZURE_REGISTRY}/$1:latest"
  #DP_AZURE_IMAGENAME_VERSIONED="${AZURE_REGISTRY}/$1:$VERSION"
  #docker tag $DP_PATH_IMAGENAME_LATEST $DP_AZURE_IMAGENAME_LATEST
  #docker tag $DP_PATH_IMAGENAME_LATEST $DP_AZURE_IMAGENAME_VERSIONED
  #docker push $DP_AZURE_IMAGENAME_VERSIONED
  #docker push $DP_AZURE_IMAGENAME_LATEST
}


#############################
# Procedure
#############################

for dir in $(find -path "./*" -type d -name "docker"); do
  pushd ${dir}/../ || exit 1
    #get maven module artifact name
    ARTIFACT_NAME="$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)"
    #get the new version
    VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"

    IMAGENAME="${IMAGE_PREFIX}-${ARTIFACT_NAME}"
    echo "Building container for plugin: ${IMAGENAME}"

    prepDockerBuild
    dockerBuild builds/${IMAGENAME}

    if [[ ! -v JENKINS_CLOUD_ID && ! -v BUILD_SOURCESDIRECTORY ]]; then
      continue
    fi

    #End here if this is a merge request test build
    if [ ! -z $gitlabBranch ]; then
      removeAzureUntaggedImages builds/${IMAGENAME}
      continue
    fi

    dockerPush builds/${IMAGENAME}
    removeAzureUntaggedImages builds/${IMAGENAME}
    listImages builds/${IMAGENAME}

    FILE_NAME="${BUILD_SOURCESDIRECTORY}/plugins.properties"
    echo "${IMAGENAME}:${VERSION}">>${FILE_NAME}
  popd || exit 1
done
