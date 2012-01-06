#!/bin/bash
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

TEMP_UDC_REFACTORING_CSV_FILE=$(mktemp --tmpdir="$HOME") || { echo "Failed to create temporary CSV file."; exit 1; }
CSV_TO_TRANSACTIONS_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/edu.illinois.codingspectator.csvtotransactions"
TIMESTAMPED_UDC_DATA_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/TimestampedUDCData/"
CSV_TO_TRANSACTIONS=(java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar "$CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar")
TRANSACTIONS_TO_RULES="$CODINGSPECTATOR_GIT_FOLDER/scripts/udc-analysis/transactionstorules.R"
UDC_COMMAND_IDS_UNDER_STUDY="$CODINGSPECTATOR_GIT_FOLDER/scripts/udc-analysis/udc-refactoring-ids"
ant -f "$CSV_TO_TRANSACTIONS_FOLDER/ant/build.xml"

#echo "userId,what,kind,bundleId,bundleVersion,description,time" > "$TEMP_UDC_REFACTORING_CSV_FILE"
#tar xfz "$TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-data.csv.tar.gz" --to-stdout | grep -E -f "$UDC_COMMAND_IDS_UNDER_STUDY" >> "$TEMP_UDC_REFACTORING_CSV_FILE"
tar xfz "$TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-data.csv.tar.gz" --to-stdout >> "$TEMP_UDC_REFACTORING_CSV_FILE"

#See <http://stackoverflow.com/q/114814/130224> and <http://stackoverflow.com/q/8385627/130224>
echo "The input UDC file has $(expr $(cat "$TEMP_UDC_REFACTORING_CSV_FILE" | sed '/^\s*$/d' | wc -l) - 1) rows of data, i.e. non-header rows."

for time_window in 1 2 5 10 30 60
do
  TEMP_TRANSACTIONS_FILE=$(mktemp --tmpdir="$HOME") || { echo "Failed to create temporary transaction file."; exit 1; }
  "${CSV_TO_TRANSACTIONS[@]}" -t "$time_window" < "$TEMP_UDC_REFACTORING_CSV_FILE" > "$TEMP_TRANSACTIONS_FILE"
  echo "There are $(expr $(cat "$TEMP_TRANSACTIONS_FILE" | sed '/^\s*$/d' | wc -l) - 1) transaction(s) with time window of $time_window min(s)."
  #cat "$TEMP_TRANSACTIONS_FILE" | "$TRANSACTIONS_TO_RULES" > "$TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-association-rules-t-$time_window.csv"
  rm -rf "$TEMP_TRANSACTIONS_FILE"
done

rm -rf "$TEMP_UDC_REFACTORING_CSV_FILE"

