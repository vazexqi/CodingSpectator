#!/bin/sh
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details. -->

# In bash, you can run this script using "ECLIPSE_HOME=/path/to/eclipse/installation/folder WORKSPACE=/path/to/codingspectator/workspace/ /path/to/this/script".

# See <http://help.eclipse.org/indigo/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_apt_building_with_apt.htm>
$ECLIPSE_HOME/eclipse -nosplash -application org.eclipse.jdt.apt.core.aptBuild -data $WORKSPACE 

