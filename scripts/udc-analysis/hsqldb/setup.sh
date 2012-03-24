#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

LOGS_CSV_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/"
MAIN_SQL=main.sql

tar xfz "$LOGS_CSV_FOLDER/logs-hsqldb.tar.gz" -C .

echo "\c false" > "$MAIN_SQL"

for sql_file in functions.sql export-refactorings.sql
do
  find "$CODINGSPECTATOR_GIT_FOLDER" -iname $sql_file -print0 | xargs -0 -I {} echo "\i "{} >> "$MAIN_SQL"
done


