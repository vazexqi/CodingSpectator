#!/bin/bash
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

TEMP_UDC_REFACTORING_CSV_FILE=$(mktemp --tmpdir="$HOME") || { echo "Failed to create temporary CSV file."; exit 1; }
CSV_TO_TRANSACTIONS_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/edu.illinois.codingspectator.csvtotransactions"
CSV_TO_TRANSACTIONS=(java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar "$CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar")
TRANSACTIONS_TO_RULES="$CODINGSPECTATOR_GIT_FOLDER/scripts/udc-analysis/transactionstorules.R"
COMPOSITE_REFACTORINGS_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/CompositeRefactorings"
TRANSACTIONS_CSV="$COMPOSITE_REFACTORINGS_FOLDER/transactions.csv"
DETAILED_TRANSACTIONS_CSV="$COMPOSITE_REFACTORINGS_FOLDER/detailed-transactions.csv"
TRANSACTION_PATTERNS_CSV="$COMPOSITE_REFACTORINGS_FOLDER/transaction-patterns.csv"
REFACTORINGS_UNDER_STUDY_CSV="$COMPOSITE_REFACTORINGS_FOLDER/refactorings-under-study.csv"

ant -f "$CSV_TO_TRANSACTIONS_FOLDER/ant/build.xml"

for time_window in 10
do
  "${CSV_TO_TRANSACTIONS[@]}" -i REFACTORING_ID -s CODINGTRACKER_TIMESTAMP -f USERNAME -f WORKSPACE_ID -f CODINGSPECTATOR_VERSION -t "$time_window" -d "$DETAILED_TRANSACTIONS_CSV" -p "$TRANSACTION_PATTERNS_CSV" < "$REFACTORINGS_UNDER_STUDY_CSV" | sed '/^\s*$/d' > "$TRANSACTIONS_CSV"
    cat "$TRANSACTIONS_CSV" | "$TRANSACTIONS_TO_RULES" --rules="$COMPOSITE_REFACTORINGS_FOLDER/refactoring-association-rules-t-$time_window.csv" --itemsets="$COMPOSITE_REFACTORINGS_FOLDER/frequent-refactoring-itemsets-t-$time_window.csv"
done

