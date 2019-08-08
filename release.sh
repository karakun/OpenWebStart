#!/bin/bash

# Going to project root folder
ROOT="$(git rev-parse --show-toplevel)"
cd $ROOT

USAGE="USAGE: release.sh <release-version> <master-version> 
<release-version> : The release-version string defined as major.minor (Example 1.5 creates 1.5.X branch and 1.5.0 release).
<master-version>  : The master-version  string defined as major.minor (Example 1.6 switches master to 1.6.0-SNAPSHOT)."

# Getting version number parameter for release-version
VERSION_VALID=0
if [[ $1 =~ ^[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    VERSION_VALID=1
fi
if [ $VERSION_VALID -eq 0 ]; then
    echo "$USAGE"
    exit 1
fi
VERSION_NUMBER=$1
echo "Release version should be $VERSION_NUMBER.X"

# Getting version number parameter for master-version
VERSION_VALID=0
if [[ $2 =~ ^[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    VERSION_VALID=1
fi
if [ $VERSION_VALID -eq 0 ]; then
    echo "$USAGE"
    exit 1
fi
NEW_MASTER_VERSION=$2.0-SNAPSHOT
echo "Master version should be $NEW_MASTER_VERSION"
echo "Working directory $ROOT"

# Checking for uncommitted modifications
UNCOMMITED_CHANGES=0
MODIFYED_FILES="$(git status -s | grep M)"
if [ "$MODIFYED_FILES" != "" ]; then
	UNCOMMITED_CHANGES=1
fi
DELETED_FILES="$(git status -s | grep D)"
if [ "$DELETED_FILES" != "" ]; then
	UNCOMMITED_CHANGES=1
fi
UNKOWN_FILES="$(git status -s | grep \?\?)"
if [ "$UNKOWN_FILES" != "" ]; then
	UNCOMMITED_CHANGES=1
fi
if [ $UNCOMMITED_CHANGES -eq 1 ]; then
	echo "Uncommited changes found in $ROOT! Please fix that!"
	exit -1        
fi

# Checkout master
echo "Checking out master"
git checkout master

# Getting last version from remote
echo "Pull latest version of master"
git pull

# Setting maven version
./mvnw versions:set -DnewVersion=$VERSION_NUMBER.0

# Maven clean verify
echo "Maven clean verify"
./mvnw clean verify
MAVEN_RESULT="$(echo $?)"
if [ "$MAVEN_RESULT" != "0" ]; then
	echo "Maven build broken"
	./mvnw versions:revert
	exit -1
fi

# Maven commit
echo "Updating version to $VERSION_NUMBER.0"
./mvnw versions:commit

# Create version branch and pull it
BRANCH_NAME="release/$VERSION_NUMBER.X"
echo "Creating branch $BRANCH_NAME"
git branch $BRANCH_NAME
git checkout $BRANCH_NAME
echo "Switching to branch $BRANCH_NAME"

# Commit and push
echo "Committing changes to branch $BRANCH_NAME"
git add -A
git commit -m "Version updated to $VERSION_NUMBER.0"

# Maven clean install
echo "Maven clean install"
./mvnw clean install
MAVEN_RESULT="$(echo $?)"
if [ "$MAVEN_RESULT" != "0" ]; then
	echo "Maven build broken"
	exit -1
fi

echo "Create tag for $VERSION_NUMBER.0"
git tag $VERSION_NUMBER.0

echo "Switching to master branch"
git checkout master

# Update version of master
echo "Updating version of master to $NEW_MASTER_VERSION"
./mvnw versions:set -DnewVersion=$NEW_MASTER_VERSION
./mvnw versions:commit

# Maven clean install
echo "Maven clean install"
./mvnw clean install
MAVEN_RESULT="$(echo $?)"
if [ "$MAVEN_RESULT" != "0" ]; then
	echo "Maven build broken"
	exit -1
fi

echo "Committing changes to master"
git add -A
git commit -m "Master updated to $NEW_MASTER_VERSION"
git push

exit 1
