#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

# Remove 'space', so filenames with spaces work well.
IFS="$(printf '\n\t')"

UDC_REFACTORING_IDS_FILE_NAME=udc-refactoring-ids
OUTPUT_FILE_NAME=udc-refactoring-data.csv
TIME=/usr/bin/time

# https://savannah.gnu.org/projects/parallel/
PARALLEL=$HOME/bin/parallel

# http://www.zlib.net/pigz/
GZ_UNZIPPER=$HOME/bin/pigz

NUMBER_OF_CORES=60

echo "userId,what,kind,bundleId,bundleVersion,description,time" > "$OUTPUT_FILE_NAME"

for file in *.csv.gz
do
  $GZ_UNZIPPER -t $file
  if [ $? -eq 0 ]
  then
  $TIME --format="Time elapsed processing $file=%E" $GZ_UNZIPPER -k -d -c -p $NUMBER_OF_CORES $file | $PARALLEL -j+0 --pipe --block 100M grep -E -f "$UDC_REFACTORING_IDS_FILE_NAME" | sed s/org\.eclipse\.jdt\.ui\.edit\.text\.java\.//g  >> "$OUTPUT_FILE_NAME"
  else
    echo "$file is corrupted."
  fi
done

$TIME --format="Time elapsed sorting the data by userId and time=%E" sort -t, -k 1,1n -k 7,7n $OUTPUT_FILE_NAME -o $OUTPUT_FILE_NAME

