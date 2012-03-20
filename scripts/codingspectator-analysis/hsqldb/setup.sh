#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

LOGS_CSV_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/"
CODINGSPECTATOR_ANALYSIS_GIT_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/scripts/codingspectator-analysis"
MAIN_SQL=main.sql
UNSORTED_USAGE_TIME_CSV="$CODINGSPECTATORDATA_SVN_FOLDER/combined.usage_time"
USAGE_TIME_CSV="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/usage_time.csv"

if [ -e "$UNSORTED_USAGE_TIME_CSV" ]; then
  echo 'USERNAME,WORKSPACE_ID,VERSION,USAGE_TIME_IN_MILLI_SECS' > "$USAGE_TIME_CSV"
  tail "$UNSORTED_USAGE_TIME_CSV" -n +2 | sort >> "$USAGE_TIME_CSV"
  echo "Updated $USAGE_TIME_CSV." 
fi

tar xfz "$LOGS_CSV_FOLDER/logs-hsqldb.tar.gz" -C .

ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/AggregatedUDCData/commands.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/refactoring_change_intensity.csv" .
ln -sf "$USAGE_TIME_CSV" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/refactoring_id_human_name_mapping.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/refactoring-complexity.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/refactoringmapping.csv" .

echo "\c false" > "$MAIN_SQL"

for sql_file in functions.sql import-data.sql undone-refactorings-analysis.sql per-refactoring-id-analysis.sql per-user-analysis.sql per-refactoring-id-kind-analysis.sql usage-time-analysis.sql quick-assist-analysis.sql undone-refactorings-analysis.sql refactoring-size-configuration-analysis.sql frequency-complexity-analysis.sql frequency-summary-analysis.sql refactoring-message-analysis.sql 
do
  find "$CODINGSPECTATOR_ANALYSIS_GIT_FOLDER" -iname $sql_file -print0 | xargs -0 -I {} echo "\i "{} >> "$MAIN_SQL"
done


