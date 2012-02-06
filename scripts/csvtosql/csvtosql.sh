#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

HSQLDB_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/org.hsqldb/ant/downloads/hsqldb-2.2.6/hsqldb"
HSQLDB_JARS="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/org.hsqldb/ant/downloads/hsqldb-2.2.6/hsqldb/lib/*.jar"
CSVTOSQL_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/edu.illinois.codingspectator.csvtosql"
CSVTOSQL_JAR="$CSVTOSQL_FOLDER/ant/bin/csvtosql.jar"

ant -f "$CSVTOSQL_FOLDER/ant/build.xml"

CS_CSV="$CODINGSPECTATOR_SVN_FOLDER/Experiment/Summer2011/InternalAndExternal/ICSE2012/Data/logs.csv" java -XX:MaxPermSize=1G -Xms1G -Xmx60G -cp $HSQLDB_JARS:$CSVTOSQL_JAR -jar $CSVTOSQL_JAR

