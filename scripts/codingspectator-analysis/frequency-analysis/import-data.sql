--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

* USAGE_TIME_START=1305435601000 --May 15, 2011

* USAGE_TIME_STOP=1313384401000 --August 15, 2011

\p Deleting out-of-scope data from ALL_DATA

DELETE FROM "PUBLIC"."ALL_DATA" "AD"

WHERE NOT ("AD"."username" LIKE 'cs-___' AND *{USAGE_TIME_START} <
"AD"."timestamp" AND "AD"."timestamp" < *{USAGE_TIME_STOP} AND
IS_REFACTORING_ID_IN_ICSE2012_PAPER("AD"."id"));

\p Creating an index on ALL_DATA

DROP INDEX "EVENT_LOCATOR_INDEX" IF EXISTS;

CREATE INDEX "EVENT_LOCATOR_INDEX" ON "PUBLIC"."ALL_DATA" (

  "username",

  "workspace ID",

  "codingspectator version",

  "timestamp",

  "recorder",

  "refactoring kind"

);

\p Inserting some "pseudo" IDs that correspond with the UDC umbrella IDs for refactorings

INSERT INTO "PUBLIC"."ALL_DATA" ("id") VALUES
('org.eclipse.jdt.ui.rename.all');

INSERT INTO "PUBLIC"."ALL_DATA" ("id") VALUES ('org.eclipse.jdt.ui.move.all');

INSERT INTO "PUBLIC"."ALL_DATA" ("id") VALUES
('org.eclipse.jdt.ui.inline.all');

\p Importing UDC data

DROP TABLE "PUBLIC"."UDC_DATA" IF EXISTS;

CREATE TABLE "PUBLIC"."UDC_DATA" (

  "YEARMONTH" VARCHAR(1000),

  "COMMAND" VARCHAR(1000),

  "BUNDLEID" VARCHAR(1000),

  "BUNDLEVERSION" VARCHAR(1000),

  "EXECUTECOUNT" INT,

  "USERCOUNT" INT

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."UDC_DATA"

\m commands.csv

\p Importing the sizes of refactorings

DROP TABLE "PUBLIC"."REFACTORING_CHANGE_SIZE" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_CHANGE_SIZE" (

  "USERNAME" VARCHAR(100),

  "WORKSPACE_ID" VARCHAR(100000),

  "VERSION" VARCHAR(100),

  "TIMESTAMP" BIGINT,

  "REFACTORING_ID" VARCHAR(100),

  "AFFECTED_FILES_COUNT" INT,

  "AFFECTED_LINES_COUNT" INT

);

* *DSV_TARGET_TABLE ="PUBLIC"."REFACTORING_CHANGE_SIZE"

\m refactoring_change_intensity.csv

\p Importing the mapping between the IDs of refactorings in UDC data and Eclipse refactoring histories

DELETE FROM "PUBLIC"."REFACTORING_CHANGE_SIZE" "T"

WHERE NOT ("T"."USERNAME" LIKE 'cs-___' AND *{USAGE_TIME_START} <
"T"."TIMESTAMP" AND "T"."TIMESTAMP" < *{USAGE_TIME_STOP});

* *DSV_TARGET_TABLE ="PUBLIC"."UDC_ECLIPSE_MAPPING"

DROP TABLE "PUBLIC"."UDC_ECLIPSE_MAPPING" IF EXISTS;

CREATE TABLE "PUBLIC"."UDC_ECLIPSE_MAPPING" (

  "UDCID" VARCHAR(1000),

  "ECLIPSEID" VARCHAR(1000)

);

\m refactoringmapping.csv

\p Importing the mapping between the IDs of refactorings and their human-readable names

DROP TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" (

  "REFACTORING_ID" VARCHAR(100),

  "HUMAN_READABLE_NAME" VARCHAR(100)

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME"

\m refactoring_id_human_name_mapping.csv

\p Importing the complexities of refactorings

DROP TABLE "PUBLIC"."REFACTORING_COMPLEXITY" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_COMPLEXITY" (

  "REFACTORING_ID" VARCHAR(100),

  "COMPLEXITY" VARCHAR(5)

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."REFACTORING_COMPLEXITY"

\m refactoring-complexity.csv

\p Importing usage_time.csv

DROP TABLE "PUBLIC"."USAGE_TIMES" IF EXISTS;

CREATE TABLE "PUBLIC"."USAGE_TIMES" (

  "USERNAME" VARCHAR(100),

  "WORKSPACE_ID" VARCHAR(100),

  "VERSION" VARCHAR(100),

  "USAGE_TIME_IN_MILLI_SECS" BIGINT

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."USAGE_TIMES"

\m usage_time.csv

