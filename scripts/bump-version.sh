#!/bin/bash
#
# Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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
update_version "gradle.properties"

git add README.md gradle.properties
git commit -m "bump version number"
