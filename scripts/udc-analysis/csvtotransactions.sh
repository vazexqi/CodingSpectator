#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

CSV_TO_TRANSACTIONS_FOLDER=$CODINGSPECTATOR_GIT_FOLDER/plug-ins/edu.illinois.codingspectator.csvtotransactions
TIMESTAMPED_UDC_DATA_FOLDER=$CODINGSPECTATOR_SVN_FOLDER/Experiment/UDCData/TimestampedUDCData/
ant -f $CSV_TO_TRANSACTIONS_FOLDER/ant/build.xml
java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar $CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar -i $TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-data.csv -o $TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-transactions-t-1 -t 1
java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar $CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar -i $TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-data.csv -o $TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-transactions-t-2 -t 2
java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar $CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar -i $TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-data.csv -o $TIMESTAMPED_UDC_DATA_FOLDER/udc-refactoring-transactions-t-5 -t 5

