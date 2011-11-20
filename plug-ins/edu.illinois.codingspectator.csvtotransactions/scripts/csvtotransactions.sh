#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
ant -f ../ant/build.xml
java -XX:MaxPermSize=512m -Xms40m -Xmx1G -jar ../ant/bin/csvtotransactions.jar
