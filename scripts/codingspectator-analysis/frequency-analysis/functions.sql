--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

/*
The following sections contain the functions that we are using. We are using a
SqlTool specific feature: Raw Mode
<http://hsqldb.org/doc/guide/ch08.html#raw-section>. The symbols "\." and ".;"
begin and end Raw Mode respectively.

Some of the sections are split artificially because SqlTool seems to have
problems with large amount of text in a single Raw Mode entry so we split the
text up.
*/

\.

DROP FUNCTION IS_ECLIPSE_PERFORMED IF EXISTS;

CREATE FUNCTION IS_ECLIPSE_PERFORMED(RECORDER VARCHAR(100), KIND VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'ECLIPSE' AND KIND = 'PERFORMED';

.;

\.

DROP FUNCTION IS_CODINGTRACKER_PERFORMED IF EXISTS;

CREATE FUNCTION IS_CODINGTRACKER_PERFORMED(RECORDER VARCHAR(100), KIND
VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'CODINGTRACKER' AND KIND = 'PERFORMED';

DROP FUNCTION IS_CODINGTRACKER_UNDONE IF EXISTS;

CREATE FUNCTION IS_CODINGTRACKER_UNDONE(RECORDER VARCHAR(100), KIND
VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'CODINGTRACKER' AND KIND = 'UNDONE';

DROP FUNCTION IS_CODINGTRACKER_REDONE IF EXISTS;

CREATE FUNCTION IS_CODINGTRACKER_REDONE(RECORDER VARCHAR(100), KIND
VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'CODINGTRACKER' AND KIND = 'REDONE';

.;

\.

DROP FUNCTION IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS IF EXISTS;

DROP FUNCTION IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS IF EXISTS;

DROP FUNCTION IS_CODINGSPECTATOR_PERFORMED IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_PERFORMED(RECORDER VARCHAR(100), KIND
VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'CODINGSPECTATOR' AND KIND = 'PERFORMED';

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS IF EXISTS;

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS IF EXISTS;

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS IF EXISTS;

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_CANCELED(RECORDER VARCHAR(100), KIND
VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'CODINGSPECTATOR' AND KIND = 'CANCELLED';

DROP FUNCTION IS_CODINGSPECTATOR_UNAVAILABLE IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_UNAVAILABLE(RECORDER VARCHAR(100), KIND
VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN RECORDER = 'CODINGSPECTATOR' AND KIND = 'UNAVAILABLE';

.;

\.

DROP FUNCTION IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS(RECORDER
VARCHAR(100), KIND VARCHAR(100), STATUS VARCHAR(100000))

RETURNS BOOLEAN

CONTAINS SQL

RETURN IS_CODINGSPECTATOR_PERFORMED(RECORDER, KIND) AND STATUS LIKE
'_WARNING%';

DROP FUNCTION IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS(RECORDER
VARCHAR(100), KIND VARCHAR(100), STATUS VARCHAR(100000))

RETURNS BOOLEAN

CONTAINS SQL

RETURN IS_CODINGSPECTATOR_PERFORMED(RECORDER, KIND) AND STATUS LIKE '_ERROR%';

.;

\.

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS(RECORDER
VARCHAR(100), KIND VARCHAR(100), STATUS VARCHAR(100000))

RETURNS BOOLEAN

CONTAINS SQL

RETURN IS_CODINGSPECTATOR_CANCELED(RECORDER, KIND) AND STATUS LIKE '_WARNING%';

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS(RECORDER VARCHAR(100),
KIND VARCHAR(100), STATUS VARCHAR(100000))

RETURNS BOOLEAN

CONTAINS SQL

RETURN IS_CODINGSPECTATOR_CANCELED(RECORDER, KIND) AND STATUS LIKE '_ERROR%';

DROP FUNCTION IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS IF EXISTS;

CREATE FUNCTION IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS(RECORDER VARCHAR(100),
KIND VARCHAR(100), STATUS VARCHAR(100000))

RETURNS BOOLEAN

CONTAINS SQL

RETURN IS_CODINGSPECTATOR_CANCELED(RECORDER, KIND) AND STATUS LIKE '_FATAL%';

.;

\.

DROP FUNCTION IS_JAVA_REFACTORING IF EXISTS;

CREATE FUNCTION IS_JAVA_REFACTORING(ID VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN ID <> 'org.eclipse.jdt.ui.copy' AND ID <> 'org.eclipse.jdt.ui.delete'
AND ID LIKE '%jdt%';

DROP FUNCTION IS_REFACTORING_ID_IN_ICSE2012_PAPER IF EXISTS;

CREATE FUNCTION IS_REFACTORING_ID_IN_ICSE2012_PAPER(ID VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN ID IN

('org.eclipse.jdt.ui.change.method.signature',

'org.eclipse.jdt.ui.change.type',

'org.eclipse.jdt.ui.convert.anonymous',

'org.eclipse.jdt.ui.promote.temp',

'org.eclipse.jdt.ui.self.encapsulate',

'org.eclipse.jdt.ui.extract.class',

'org.eclipse.jdt.ui.extract.constant',

'org.eclipse.jdt.ui.extract.interface',

'org.eclipse.jdt.ui.extract.temp',

'org.eclipse.jdt.ui.extract.method',

'org.eclipse.jdt.ui.extract.superclass',

'org.eclipse.jdt.ui.infer.typearguments',

'org.eclipse.jdt.ui.inline.constant',

'org.eclipse.jdt.ui.inline.temp',

'org.eclipse.jdt.ui.inline.method',

'org.eclipse.jdt.ui.introduce.indirection',

'org.eclipse.jdt.ui.introduce.factory',

'org.eclipse.jdt.ui.introduce.parameter',

'org.eclipse.jdt.ui.introduce.parameter.object',

'org.eclipse.jdt.ui.move',

'org.eclipse.jdt.ui.move.method',

'org.eclipse.jdt.ui.move.static',

'org.eclipse.jdt.ui.move.inner',

'org.eclipse.jdt.ui.pull.up',

'org.eclipse.jdt.ui.push.down',

'org.eclipse.jdt.ui.rename.compilationunit',

'org.eclipse.jdt.ui.rename.type',

'org.eclipse.jdt.ui.rename.class',

'org.eclipse.jdt.ui.rename.enum.constant',

'org.eclipse.jdt.ui.rename.field',

'org.eclipse.jdt.ui.rename.local.variable',

'org.eclipse.jdt.ui.rename.method',

'org.eclipse.jdt.ui.rename.package',

'org.eclipse.jdt.ui.rename.type.parameter',

'org.eclipse.jdt.ui.use.supertype');

DROP FUNCTION IS_REFACTORING_ID_SUPPORTED_BY_CS IF EXISTS;

CREATE FUNCTION IS_REFACTORING_ID_SUPPORTED_BY_CS(ID VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN ID IN

('org.eclipse.jdt.ui.change.method.signature',

'org.eclipse.jdt.ui.promote.temp',

'org.eclipse.jdt.ui.extract.constant',

'org.eclipse.jdt.ui.extract.interface',

'org.eclipse.jdt.ui.extract.temp',

'org.eclipse.jdt.ui.extract.method',

'org.eclipse.jdt.ui.extract.superclass',

'org.eclipse.jdt.ui.inline.constant',

'org.eclipse.jdt.ui.inline.temp',

'org.eclipse.jdt.ui.inline.method',

'org.eclipse.jdt.ui.move',

'org.eclipse.jdt.ui.move.method',

'org.eclipse.jdt.ui.move.static',

'org.eclipse.jdt.ui.pull.up',

'org.eclipse.jdt.ui.push.down',

'org.eclipse.jdt.ui.rename.compilationunit',

'org.eclipse.jdt.ui.rename.type',

'org.eclipse.jdt.ui.rename.class',

'org.eclipse.jdt.ui.rename.enum.constant',

'org.eclipse.jdt.ui.rename.field',

'org.eclipse.jdt.ui.rename.local.variable',

'org.eclipse.jdt.ui.rename.method',

'org.eclipse.jdt.ui.rename.package',

'org.eclipse.jdt.ui.rename.type.parameter',

'org.eclipse.jdt.ui.use.supertype');

DROP FUNCTION IS_REFACTORING_ID_IN_QUICK_ASSIST IF EXISTS;

CREATE FUNCTION IS_REFACTORING_ID_IN_QUICK_ASSIST(ID VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN ID IN

('org.eclipse.jdt.ui.convert.anonymous',

'org.eclipse.jdt.ui.promote.temp',

'org.eclipse.jdt.ui.self.encapsulate',

'org.eclipse.jdt.ui.extract.constant',

'org.eclipse.jdt.ui.extract.temp',

'org.eclipse.jdt.ui.extract.method',

'org.eclipse.jdt.ui.inline.temp',

'org.eclipse.jdt.ui.rename.compilationunit',

'org.eclipse.jdt.ui.rename.type',

'org.eclipse.jdt.ui.rename.class',

'org.eclipse.jdt.ui.rename.enum.constant',

'org.eclipse.jdt.ui.rename.field',

'org.eclipse.jdt.ui.rename.local.variable',

'org.eclipse.jdt.ui.rename.method');

DROP FUNCTION IS_JAVA_RENAME_REFACTORING_ID IF EXISTS;

CREATE FUNCTION IS_JAVA_RENAME_REFACTORING_ID(ID VARCHAR(100))

RETURNS BOOLEAN

CONTAINS SQL

RETURN ID IN

('org.eclipse.jdt.ui.rename.compilationunit',

'org.eclipse.jdt.ui.rename.type',

'org.eclipse.jdt.ui.rename.class',

'org.eclipse.jdt.ui.rename.enum.constant',

'org.eclipse.jdt.ui.rename.field',

'org.eclipse.jdt.ui.rename.local.variable',

'org.eclipse.jdt.ui.rename.method');

DROP FUNCTION JAVA_REFACTORING_ID IF EXISTS;

CREATE FUNCTION JAVA_REFACTORING_ID(ID VARCHAR(100))

RETURNS VARCHAR(100)

CONTAINS SQL

RETURN

(CASE ID

WHEN 'org.eclipse.jdt.ui.rename.compilationunit' THEN
'org.eclipse.jdt.ui.rename.class'

WHEN 'org.eclipse.jdt.ui.rename.type' THEN 'org.eclipse.jdt.ui.rename.class'

ELSE ID END);

.;

