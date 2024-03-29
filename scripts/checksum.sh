#!/bin/bash

#
# Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

RESULT_FILE=$1

if [ -f $RESULT_FILE ]; then
  rm "$RESULT_FILE"
fi

touch "$RESULT_FILE"

checksum_file() {
  echo "$(openssl md5 "$1" | awk '{print $2}')" "$1"
}

FILES=()
while read -r -d ''; do
	FILES+=("$REPLY")
done < <(find . -type f \( -name "build.gradle*" -o -name "Deps.kt" \) -print0)

# Loop through files and append MD5 to result file
for FILE in "${FILES[@]}"; do
	checksum_file "$FILE" >> $RESULT_FILE
done
# Now sort the file so that it is
sort "$RESULT_FILE" -o "$RESULT_FILE"
