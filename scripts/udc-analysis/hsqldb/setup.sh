#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

LOGS_CSV_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/"
MAIN1_SQL=main1.sql
MAIN2_SQL=main2.sql

tar xfz "$LOGS_CSV_FOLDER/logs-hsqldb.tar.gz" -C .

ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/matched-performed-refactorings.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/CompositeRefactorings/transaction-patterns.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/CompositeRefactorings/detailed-transactions.csv" .

echo "\c false" > "$MAIN1_SQL"

for sql_file in functions.sql export-refactorings.sql
do
  find "$CODINGSPECTATOR_GIT_FOLDER" -iname $sql_file -print0 | xargs -0 -I {} echo "\i "{} >> "$MAIN1_SQL"
done

echo "\c false" > "$MAIN2_SQL"

for sql_file in max-lengths.sql compute-transaction-statistics.sql
do
  find "$CODINGSPECTATOR_GIT_FOLDER" -iname $sql_file -print0 | xargs -0 -I {} echo "\i "{} >> "$MAIN2_SQL"
done

