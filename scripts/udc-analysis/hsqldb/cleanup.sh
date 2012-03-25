#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

COMPOSITE_REFACTORINGS_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/CompositeRefactorings"

rm -f matched-performed-refactorings.csv
mv -f *.csv "$COMPOSITE_REFACTORINGS_FOLDER"
rm -f db_file.*
rm -f *.sql

