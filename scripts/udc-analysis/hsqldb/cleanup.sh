#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

COMPOSITE_REFACTORINGS_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/CompositeRefactorings"

tar cfz refactorings-under-study-context.csv.tar.gz refactorings-under-study-context.csv
rm -f refactorings-under-study-context.csv
rm -f matched-performed-refactorings.csv
mv -f *.csv "$COMPOSITE_REFACTORINGS_FOLDER"
mv -f *.tar.gz "$COMPOSITE_REFACTORINGS_FOLDER"
rm -f db_file.*
rm -f *.sql

