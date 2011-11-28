#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

HSQLDB_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/org.hsqldb/ant/downloads/hsqldb-2.2.6/hsqldb"
HSQLDB_RC_FILE="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/edu.illinois.codingspectator.csvtosql/hsqldb/sqltool.rc"

java -XX:MaxPermSize=512m -Xms40m -Xmx20G -jar "$HSQLDB_FOLDER/lib/sqltool.jar" --rcFile $HSQLDB_RC_FILE db_file

