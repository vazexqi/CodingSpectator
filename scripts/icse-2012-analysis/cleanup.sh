#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

ICSE_2012_GIT_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/scripts/icse-2012-analysis"
ICSE_2012_SVN_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/"

rm -f "$ICSE_2012_GIT_FOLDER"/db_file.*
rm -f "$ICSE_2012_GIT_FOLDER/commands.csv"
rm -f "$ICSE_2012_GIT_FOLDER/refactoring_change_intensity.csv"
rm -f "$ICSE_2012_GIT_FOLDER/refactoringmapping.csv"
mv "$ICSE_2012_GIT_FOLDER"/*.csv "$ICSE_2012_SVN_FOLDER/Results/"

