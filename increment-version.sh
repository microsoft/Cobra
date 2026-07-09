#!/bin/bash

usage()
{
  echo "Usage: $0 <old version string> <new version string>"
  echo "Example: $0 2.4.0-SNAPSHOT 2.4.1-SNAPSHOT"
}

if [ $# -ne 2 ]; then
  usage
  exit 1
fi

OLD_VERSION_STRING="$1"
NEW_VERSION_STRING="$2"

for file in `grep -rl "$OLD_VERSION_STRING" *`; do
  sed "s!$OLD_VERSION_STRING!$NEW_VERSION_STRING!g" $file > $file.new
  mv $file.new $file
done

exit 0
