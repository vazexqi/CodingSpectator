#!/bin/bash
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

TEMP_UDC_REFACTORING_CSV_FILE=$(mktemp --tmpdir="$HOME") || { echo "Failed to create temporary CSV file."; exit 1; }
CSV_TO_TRANSACTIONS_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/edu.illinois.codingspectator.csvtotransactions"
CSV_TO_TRANSACTIONS=(java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar "$CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar")

ant -f "$CSV_TO_TRANSACTIONS_FOLDER/ant/build.xml"

for time_window in 10
do
  "${CSV_TO_TRANSACTIONS[@]}" -i REFACTORING_ID -s TIMESTAMP -t "$time_window" -d detailed-transactions.csv -p transaction-patterns.csv < refactorings-under-study.csv | sed '/^\s*$/d' > transactions.csv
done

