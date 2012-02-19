#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

HSQLDB_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/org.hsqldb/ant/downloads/hsqldb-2.2.6/hsqldb"
HSQLDB_JARS="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/org.hsqldb/ant/downloads/hsqldb-2.2.6/hsqldb/lib/*.jar"
CSVTOSQL_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/edu.illinois.codingspectator.csvtosql"
CSVTOSQL_JAR="$CSVTOSQL_FOLDER/ant/bin/csvtosql.jar"
LOGS_CSV_FOLDER="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/"

ant -f "$CSVTOSQL_FOLDER/ant/build.xml"
rm -f "$LOGS_CSV_FOLDER/logs.csv"
tar -xOf "$LOGS_CSV_FOLDER/logs.csv.tar.gz" > "$LOGS_CSV_FOLDER/logs.csv"
CS_CSV="$LOGS_CSV_FOLDER/logs.csv" java -XX:MaxPermSize=1G -Xms1G -Xmx60G -cp $HSQLDB_JARS:$CSVTOSQL_JAR -jar $CSVTOSQL_JAR
rm "$LOGS_CSV_FOLDER/logs.csv"
tar cfz "$LOGS_CSV_FOLDER/logs-hsqldb.tar.gz" db_file*
rm db_file*

