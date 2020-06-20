#!/bin/bash
set -e

if [ "$#" -ne 2 ]
  then
    echo "Use: ./bump-version old_version new_version"
    exit
fi

old_version=$1
new_version=$2

update_version() {
    echo "Updating version from '$old_version' to '$new_version' in $1"
    sed -i.bak s/"$new_version"-SNAPSHOT/"$new_version"/g "$1"
    sed -i.bak s/"$old_version"/"$new_version"/g "$1"
    rm "$1".bak
}

update_version "README.md"
update_version "build.gradle.kts"

git add README.md build.gradle.kts
git commit -m "bump version number"
