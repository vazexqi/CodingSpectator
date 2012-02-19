#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

HSQLDB_FOLDER="$CODINGSPECTATOR_GIT_FOLDER/plug-ins/common/org.hsqldb/ant/downloads/hsqldb-2.2.6/hsqldb"
HSQLDB_RC_FILE="sqltool.rc"

#java -XX:MaxPermSize=5G -Xms5G -Xmx70G -jar "$HSQLDB_FOLDER/lib/sqltool.jar" --rcFile $HSQLDB_RC_FILE mem
java -Xms5G -Xmx70G -jar "$HSQLDB_FOLDER/lib/sqltool.jar" --rcFile $HSQLDB_RC_FILE mem

