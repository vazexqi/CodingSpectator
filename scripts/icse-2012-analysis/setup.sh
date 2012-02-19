#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

LOGS_CSV_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/"
ICSE_2012_ANALYSIS_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/scripts/icse-2012-analysis/"

tar xfz "$LOGS_CSV_FOLDER/logs-hsqldb.tar.gz" -C "$ICSE_2012_ANALYSIS_FOLDER"

ln -s "$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/AggregatedUDCData/commands.csv" "$ICSE_2012_ANALYSIS_FOLDER/commands.csv"
ln -s "$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/OldICSE2012/refactoring_change_intensity.csv" "$ICSE_2012_ANALYSIS_FOLDER/refactoring_change_intensity.csv"
ln -s "$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/refactoringmapping.csv" "$ICSE_2012_ANALYSIS_FOLDER/refactoringmapping.csv"

