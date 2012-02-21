#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

LOGS_CSV_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/"
CODINGSPECTATOR_ANALYSIS_GIT_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/scripts/codingspectator-analysis"
MAIN_SQL=main.sql

tar xfz "$LOGS_CSV_FOLDER/logs-hsqldb.tar.gz" -C .

ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/AggregatedUDCData/commands.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/OldICSE2012/refactoring_change_intensity.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/OldICSE2012/usage_time.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/OldICSE2012/refactoring_id_human_name_mapping.csv" .
ln -sf "$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/refactoringmapping.csv" .

echo "\c false" > "$MAIN_SQL"

for sql_file in functions.sql import-data.sql undone-refactorings-analysis.sql per-refactoring-id-analysis.sql per-user-analysis.sql per-refactoring-id-kind-analysis.sql usage-time-analysis.sql quick-assist-analysis.sql undone-refactorings-analysis.sql frequency-summary-analysis.sql refactoring-message-analysis.sql 
do
  find "$CODINGSPECTATOR_ANALYSIS_GIT_FOLDER" -iname $sql_file -print0 | xargs -0 -I {} echo "\i "{} >> "$MAIN_SQL"
done


