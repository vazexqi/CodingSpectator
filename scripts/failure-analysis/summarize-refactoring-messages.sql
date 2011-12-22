--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

\c false

\i functions.sql

DROP TABLE "PUBLIC"."ECLIPSE_REFACTORING_IDS" IF EXISTS;

CREATE TABLE "PUBLIC"."ECLIPSE_REFACTORING_IDS" (
  "ID" VARCHAR(100)
);

INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.change.method.signature');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.convert.anonymous');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.extract.class');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.extract.constant');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.extract.interface');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.extract.method');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.extract.superclass');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.extract.temp');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.infer.typearguments');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.inline');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.inline.constant');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.inline.method');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.inline.temp');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.introduce.factory');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.introduce.indirection');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.introduce.parameter');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.introduce.parameter.object');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.move');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.move.inner');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.move.method');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.move.static');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.promote.temp');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.pull.up');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.push.down');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.compilationunit');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.enum.constant');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.field');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.java.project');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.local.variable');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.method');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.package');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.source.folder');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.type');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.type.parameter');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.rename.unknown.java.element');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.self.encapsulate');
INSERT INTO "PUBLIC"."ECLIPSE_REFACTORING_IDS" ("ID") VALUES ('org.eclipse.jdt.ui.use.supertype');

DROP TABLE "PUBLIC"."MESSAGE_PATTERNS" IF EXISTS;

CREATE TABLE "PUBLIC"."MESSAGE_PATTERNS" (
  "ID" VARCHAR(10),
  "PATTERN" VARCHAR(1000),
  "KIND" VARCHAR(100)
);

INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP00', '', 'NO_REPORT');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP01', '_OK%', 'OK');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP02', 'This refactoring cannot be performed correctly due to syntax errors in the compilation unit. To perform this operation you will need to fix the errors.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP03', '%FATALERROR%FATALERROR: This refactoring cannot be performed correctly due to syntax errors in the compilation unit. To perform this operation you will need to fix the errors.%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP04', '%ERROR: Found potential matches. Please review changes on the preview page.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP05', '%ERROR: Duplicate local variable%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP06', '%ERROR: Removed parameter % is used in method % declared in type %.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP07', '%ERROR: % cannot be resolved to a variable%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP08', '%WARNING: This name is discouraged. According to convention, names of local variables should start with a lowercase letter.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP09', '%WARNING: This name is discouraged. According to convention, names of methods should start with a lowercase letter.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP10', '%WARNING: The field name is discouraged. According to convention, field names should start with a lowercase letter.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP11', '%WARNING: By convention, Java type names usually start with an uppercase letter%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP12', 'Select a method declaration, a method invocation, a static final field or a local variable that you want to inline.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP13', '%ERROR: Cannot inline method. Return statement in method declaration interrupts execution flow.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP14', '%FATALERROR%FATALERROR: Inlining is only possible on simple functions (consisting of a single return statement), or functions used in an assignment.%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP15', '%ERROR: Inlining is only possible on simple functions (consisting of a single return statement), or functions used in an assignment.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP16', '%ERROR: Selected statements contain a return statement but not all possible execution flows end in a return. Semantics may not be preserved if you proceed%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP17', '%ERROR: New method % overrides an existing method in type %', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP18', 'The end of the selection contains characters that do not belong to a statement%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP19', 'The beginning of the selection contains characters that do not belong to a statement%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP20', 'Ambiguous return value: Selected block contains more than one assignment to local variables.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP21', 'Selection begins inside a comment%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP22', 'The selection does not cover a set of statements or an expression. Extend selection to a valid range using the ''Expand Selection To'' actions from the ''Edit'' menu.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP23', 'Selection contains branch statement but corresponding branch target (or the complete loop body) is not selected%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP24', 'Can only extract a single expression or a set of statements.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP25', '%ERROR: % cannot be resolved%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP26', '%ERROR: Type mismatch: cannot convert from % to %', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP27', '%ERROR: The method % is undefined for the type%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP28', '%ERROR: Cannot refer to a non-final variable % inside an inner class defined in a different method%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP29', '%WARNING: A variable with name % is already defined in the visible scope%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP30', '%WARNING: The selected expression is assigned. Extracting may change the program''s semantics%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP31', 'An expression has to be selected to activate this refactoring. Names used in declarations are not expressions.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP32', 'An expression used in a method or in an initializer must be selected to activate this refactoring.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP33', '%ERROR: % or a type in its hierarchy defines a method % with the same number of parameters and the same parameter type names.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP34', '%WARNING: % or a type in its hierarchy defines a method % with the same number of parameters, but different parameter type names.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP35', '%ERROR: Hierarchy declares a method % with the same number of parameters and the same parameter type names.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP36', '%WARNING: Code modification may not be accurate as affected resource % has compile errors.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP37', '%ERROR: Problem in %. Another name will shadow access to the renamed element%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP38', '%ERROR: % has syntax errors. Content of that file will not be updated%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP39', '%ERROR: Type named % already exists in package%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP40', '%ERROR: Type named % is imported (single-type-import) in % (a compilation unit must not import and declare a type with the same name)%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP41', '%WARNING: Type % contains a main method - some applications (such as scripts) may not work after refactoring.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP42', '%FATALERROR%FATALERROR: A local variable declaration or reference must be selected to activate this refactoring%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP43', '%FATALERROR%FATALERROR: % is not a valid Java identifier%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP44', '%ERROR: Syntax error on token %, % expected%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP45', '%ERROR: Name collision with name%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP46', '%ERROR: The method % in the type % is not applicable for the arguments%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP47', '%ERROR: Syntax error, insert % to complete %', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP48', '%ERROR: Another type named % is referenced in %', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP49', '%FATALERROR%FATALERROR: This is an invalid name for a file or folder%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP50', '%WARNING: The visibility of method % will be changed to default.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP51', '%WARNING: The visibility of method % will be changed to public.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP51', '%WARNING: The visibility of field % will be changed to public.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP53', '%WARNING: The visibility of field % will be changed to default.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP54', '%WARNING: The visibility of field % will be changed to protected.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP55', '%WARNING: The method invocations to % cannot be updated, since the original method is used polymorphically.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP56', '%WARNING: Inaccurate matches have been found in resource %. Please review changes on the preview page.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP57', 'Pull up is not available on this type. Type has no super types%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP58', 'To activate this refactoring, please select the name of a non-binary instance method or field.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP59', 'An expression must be selected to activate this refactoring.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP60', 'Cannot extract new method from selection. Only statements from a method body can be extracted.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP61', 'Cannot extract the left-hand side of an assignment.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP62', '%WARNING: Moving final fields will result in compilation errors if they are not initialized on creation or in constructors%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP63', 'Cannot extract single null literals.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP64', 'The body of the method % cannot be analyzed because of compilation errors in that method. To perform the operation you will need to fix the errors.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP65', 'The compilation unit containing this method declaration has syntax errors. To perform the operation you will need to fix the errors.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP66', 'Cannot inline abstract methods.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP67', '%ERROR: Type % overrides method to be inlined.%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP68', '%FATALERROR%FATALERROR: A file or folder cannot be moved to its own parent.%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP69', '%FATALERROR%FATALERROR: The selected element cannot be the destination for this operation%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP70', '%INFO: Java references will not be updated.%', 'INFO');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP71', '%FATALERROR%FATALERROR: Resource % is out of sync with file system.%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP72', '%This method cannot be moved, since no possible targets have been found.%Only a class which is reachable from within this method can be a valid target. The target must therefore be the declaring class of a parameter or field type. In addition the target must be writable.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP73', 'Select a declaration or a reference to a local variable.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP74', '%ERROR: Field % declared in type % has a different type than its moved counterpart%', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP75', '%ERROR: Pushed down member % is referenced by %', 'ERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP76', 'Push Down is not allowed on type %, since it does not have subclasses to which members could be pushed down.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP77', '%WARNING: Package % already exists in this project in folder%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP78', '%FATALERROR%FATALERROR: Compilation unit % already exists%', 'FATALERROR');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP79', '%WARNING: By convention, Java type names usually don''t contain the $ character%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP80', '%Operation unavailable on the current selection.%Select a Java project, source folder, resource, package, compilation unit, type, field, method, parameter or a local variable%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP81', '%Operation unavailable on the current selection.%Select a Java project, source folder, resource, package or a compilation unit, or a non-binary type, field, method, parameter, local variable, or type variable.%', 'UNAVAILABLE');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP82', '%WARNING: % contains compiler errors. This may affect field access update.%', 'WARNING');
INSERT INTO "PUBLIC"."MESSAGE_PATTERNS" ("ID", "PATTERN", "KIND") VALUES ('MP83', 'Only local variables declared in methods can be converted to fields.%', 'UNAVAILABLE');

* *DSV_COL_DELIM=|
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=MessagePatterns.csv

\x SELECT "T"."ID" AS "ID", ('"' || "T"."PATTERN" || '"') AS "PATTERN", "T"."KIND" AS "KIND" FROM "PUBLIC"."MESSAGE_PATTERNS" "T" ORDER BY "T"."ID"

DROP TABLE "PUBLIC"."REFACTORING_MESSAGES" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_MESSAGES" (
  "MESSAGE_PATTERN_ID" VARCHAR(1000),
  "KIND" VARCHAR(100),
  "REACTION" VARCHAR(100),
  "REFACTORING_ID" VARCHAR(100),
  "USERNAME" VARCHAR(100),
  "WORKSPACE_ID" VARCHAR(100000),
  "VERSION" VARCHAR(100),
  "TIMESTAMP" BIGINT/*,
  "MESSAGE" VARCHAR(100000)*/
);

INSERT INTO "PUBLIC"."REFACTORING_MESSAGES" (
  "MESSAGE_PATTERN_ID",
  "KIND",
  "REACTION",
  "REFACTORING_ID",
  "USERNAME",
  "WORKSPACE_ID",
  "VERSION",
  "TIMESTAMP"/*,
  "MESSAGE"*/
)
SELECT "MESSAGE_PATTERN_ID", "KIND", "REACTION", "REFACTORING_ID", "USERNAME", "WORKSPACE_ID", "VERSION", "TIMESTAMP"/*, "MESSAGE"*/
FROM
(SELECT
"T2"."ID" AS "MESSAGE_PATTERN_ID",
"T2"."KIND" AS "KIND",
"T1"."refactoring kind" AS "REACTION",
"T1"."id" AS "REFACTORING_ID",
"T1"."username" AS "USERNAME",
"T1"."workspace ID" AS "WORKSPACE_ID",
"T1"."codingspectator version" AS "VERSION",
"T1"."timestamp" AS "TIMESTAMP"/*,
"T1"."status" AS "MESSAGE"*/,
"T1"."recorder" AS "RECORDER"
FROM "PUBLIC"."ALL_DATA" "T1" INNER JOIN "PUBLIC"."MESSAGE_PATTERNS" "T2" ON "T1"."status" LIKE "T2"."PATTERN"
WHERE IS_JAVA_REFACTORING("T1"."id") AND "T1"."username" LIKE 'cs-___' AND "T1"."recorder" = 'CODINGSPECTATOR' 
ORDER BY "MESSAGE_PATTERN_ID", "KIND", "REFACTORING_ID", "USERNAME", "WORKSPACE_ID", "VERSION", "TIMESTAMP"/*, "MESSAGE"*/);

--* *DSV_COL_DELIM=||
--* *DSV_ROW_DELIM=\n\n\n
* *DSV_COL_DELIM=,
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=RefactoringMessages.csv

\x SELECT * FROM "PUBLIC"."REFACTORING_MESSAGES"

DROP TABLE "PUBLIC"."REFACTORING_PATTERN_ID" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_PATTERN_ID" (
  "MESSAGE_PATTERN_ID" VARCHAR(1000),
  "REFACTORING_ID" VARCHAR(100)
);

INSERT INTO "PUBLIC"."REFACTORING_PATTERN_ID" (
  "MESSAGE_PATTERN_ID",
  "REFACTORING_ID"
)
SELECT
"MESSAGE_PATTERN_ID", "REFACTORING_ID"
FROM
(SELECT
"T"."MESSAGE_PATTERN_ID" AS "MESSAGE_PATTERN_ID",
"T"."REFACTORING_ID" AS "REFACTORING_ID",
COUNT(*)
FROM "PUBLIC"."REFACTORING_MESSAGES" "T"
GROUP BY "T"."MESSAGE_PATTERN_ID", "T"."REFACTORING_ID"
HAVING COUNT(*) > 1);

* *DSV_COL_DELIM=,
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=RefactoringPatternID.csv

\x SELECT * FROM "PUBLIC"."REFACTORING_PATTERN_ID"

DROP TABLE "PUBLIC"."UNMATCHED_MESSAGE_PATTERNS" IF EXISTS;

CREATE TABLE "PUBLIC"."UNMATCHED_MESSAGE_PATTERNS" (
  "MESSAGE_PATTERN_ID" VARCHAR(1000)
);

INSERT INTO "PUBLIC"."UNMATCHED_MESSAGE_PATTERNS" (
  "MESSAGE_PATTERN_ID"
)
SELECT "T1"."ID" AS "MESSAGE_PATTERN_ID"
FROM "PUBLIC"."MESSAGE_PATTERNS" "T1"
WHERE NOT EXISTS (
SELECT "T2"."status"
FROM "PUBLIC"."ALL_DATA" "T2"
WHERE "T2"."status" LIKE "T1"."PATTERN" AND IS_JAVA_REFACTORING("T2"."id") AND "T2"."username" LIKE 'cs-___' AND "T2"."recorder" = 'CODINGSPECTATOR' 
);

* *DSV_COL_DELIM=,
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=UnmatchedMessagePatterns.csv

\x SELECT * FROM "PUBLIC"."UNMATCHED_MESSAGE_PATTERNS"

DROP TABLE "PUBLIC"."UNMATCHED_MESSAGES" IF EXISTS;

CREATE TABLE "PUBLIC"."UNMATCHED_MESSAGES" (
  "MESSAGE" VARCHAR(100000)
);

INSERT INTO "PUBLIC"."UNMATCHED_MESSAGES" (
  "MESSAGE"
)
SELECT "T1"."status" AS "MESSAGE"
FROM "PUBLIC"."ALL_DATA" "T1"
WHERE IS_JAVA_REFACTORING("T1"."id") AND "T1"."username" LIKE 'cs-___' AND "T1"."recorder" = 'CODINGSPECTATOR' AND
NOT EXISTS (
SELECT "T2"."PATTERN"
FROM "PUBLIC"."MESSAGE_PATTERNS" "T2"
WHERE "T1"."status" LIKE "T2"."PATTERN"
);

* *DSV_COL_DELIM=,
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=UnmatchedMessages.csv

\x SELECT * FROM "PUBLIC"."UNMATCHED_MESSAGES"

DROP TABLE "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_MESSAGE_PATTERN" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_MESSAGE_PATTERN" (
  "MESSAGE_PATTERN_ID" VARCHAR(10),
  "KIND" VARCHAR(100),
  "MESSAGE_PATTERN" VARCHAR(1000),
  "NUMBER_OF_OCCURRENCES" INT,
  "NUMBER_OF_AFFECTED_PARTICIPANTS" INT,
  "NUMBER_OF_PERFORMED" INT,
  "NUMBER_OF_CANCELED" INT
);

INSERT INTO "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_MESSAGE_PATTERN" (
  "MESSAGE_PATTERN_ID",
  "KIND",
  "MESSAGE_PATTERN",
  "NUMBER_OF_OCCURRENCES",
  "NUMBER_OF_AFFECTED_PARTICIPANTS",
  "NUMBER_OF_PERFORMED",
  "NUMBER_OF_CANCELED"
)
SELECT
"T1"."MESSAGE_PATTERN_ID" AS "MESSAGE_PATTERN_ID",
"T1"."KIND" AS "KIND",
(SELECT TOP 1 ('"' || "T2"."PATTERN" || '"')
FROM "PUBLIC"."MESSAGE_PATTERNS" "T2"
WHERE "T1"."MESSAGE_PATTERN_ID" = "T2"."ID") AS "MESSAGE_PATTERN",
COUNT(*) AS "NUMBER_OF_OCCURRENCES",
COUNT(DISTINCT("T1"."USERNAME")) AS "NUMBER_OF_AFFECTED_PARTICIPANTS",
COUNT(NULLIF("T1"."REACTION" <> 'PERFORMED', TRUE)) AS "NUMBER_OF_PERFORMED",
COUNT(NULLIF("T1"."REACTION" <> 'CANCELED' AND "T1"."REACTION" <> 'CANCELLED', TRUE)) AS "NUMBER_OF_CANCELED"
FROM "PUBLIC"."REFACTORING_MESSAGES" "T1"
GROUP BY "MESSAGE_PATTERN_ID", "KIND"
ORDER BY "NUMBER_OF_OCCURRENCES" DESC;

* *DSV_COL_DELIM=|
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=RefactoringMessagesSummaryByMessagePattern.csv

\x SELECT * FROM "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_MESSAGE_PATTERN"

DROP TABLE "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_REFACTORING_ID" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_REFACTORING_ID" (
  "REFACTORING_ID" VARCHAR(100),
  "MESSAGE_PATTERN_ID" VARCHAR(10),
  "KIND" VARCHAR(100),
  "MESSAGE_PATTERN" VARCHAR(1000),
  "NUMBER_OF_OCCURRENCES" INT,
  "NUMBER_OF_AFFECTED_PARTICIPANTS" INT,
  "NUMBER_OF_PERFORMED" INT,
  "NUMBER_OF_CANCELED" INT
);

INSERT INTO "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_REFACTORING_ID" (
  "REFACTORING_ID",
  "MESSAGE_PATTERN_ID",
  "KIND",
  "MESSAGE_PATTERN",
  "NUMBER_OF_OCCURRENCES",
  "NUMBER_OF_AFFECTED_PARTICIPANTS",
  "NUMBER_OF_PERFORMED",
  "NUMBER_OF_CANCELED"
)
SELECT
"T1"."REFACTORING_ID" AS "REFACTORING_ID",
"T1"."MESSAGE_PATTERN_ID" AS "MESSAGE_PATTERN_ID",
"T1"."KIND" AS "KIND",
(SELECT TOP 1 ('"' || "T2"."PATTERN" || '"')
FROM "PUBLIC"."MESSAGE_PATTERNS" "T2"
WHERE "T1"."MESSAGE_PATTERN_ID" = "T2"."ID") AS "MESSAGE_PATTERN",
COUNT(*) AS "NUMBER_OF_OCCURRENCES",
COUNT(DISTINCT("T1"."USERNAME")) AS "NUMBER_OF_AFFECTED_PARTICIPANTS",
COUNT(NULLIF("T1"."REACTION" <> 'PERFORMED', TRUE)) AS "NUMBER_OF_PERFORMED",
COUNT(NULLIF("T1"."REACTION" <> 'CANCELED' AND "T1"."REACTION" <> 'CANCELLED', TRUE)) AS "NUMBER_OF_CANCELED"
FROM "PUBLIC"."REFACTORING_MESSAGES" "T1"
GROUP BY "REFACTORING_ID", "MESSAGE_PATTERN_ID", "KIND"
ORDER BY "REFACTORING_ID", "NUMBER_OF_OCCURRENCES" DESC;

* *DSV_COL_DELIM=|
* *DSV_ROW_DELIM=\n
* *DSV_TARGET_FILE=RefactoringMessagesSummaryByRefactoringID.csv

\x SELECT * FROM "PUBLIC"."REFACTORING_MESSAGES_SUMMARY_BY_REFACTORING_ID"

