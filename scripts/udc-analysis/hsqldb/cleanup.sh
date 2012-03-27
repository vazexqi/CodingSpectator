#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

COMPOSITE_REFACTORINGS_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/CompositeRefactorings"
MAIN1_SQL=main1.sql
MAIN2_SQL=main2.sql

tar cfz refactorings-under-study-context.csv.tar.gz refactorings-under-study-context.csv
rm -f refactorings-under-study-context.csv
rm -f matched-performed-refactorings.csv
rm -f transaction-patterns.csv
rm -f detailed-transactions.csv
mv -f *.csv "$COMPOSITE_REFACTORINGS_FOLDER"
mv -f *.tar.gz "$COMPOSITE_REFACTORINGS_FOLDER"
rm -f db_file.*
rm -f "$MAIN1_SQL"
rm -f "$MAIN2_SQL"

