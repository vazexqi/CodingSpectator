#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

CSV_TO_TRANSACTIONS_FOLDER=$CODINGSPECTATOR_GIT_FOLDER/plug-ins/edu.illinois.codingspectator.csvtotransactions
ant -f $CSV_TO_TRANSACTIONS_FOLDER/ant/build.xml
java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar $CSV_TO_TRANSACTIONS_FOLDER/ant/bin/csvtotransactions.jar

