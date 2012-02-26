#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

ICSE_2012_SVN_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/"

rm -f commands.csv
rm -f refactoring_change_intensity.csv
rm -f refactoringmapping.csv
rm -f usage_time.csv
rm -f refactoring_id_human_name_mapping.csv
mv -f *.csv "$ICSE_2012_SVN_FOLDER/Results/"
rm -f db_file.*
rm -f *.sql

