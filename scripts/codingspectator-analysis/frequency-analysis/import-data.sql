--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

\p Deleting out-of-scope data from ALL_DATA

* USAGE_TIME_START=1305435601000 --May 15, 2011

* USAGE_TIME_STOP=1313384401000 --August 15, 2011

DELETE FROM "PUBLIC"."ALL_DATA" "AD"

WHERE NOT ("AD"."username" LIKE 'cs-___' AND *{USAGE_TIME_START} <
"AD"."timestamp" AND "AD"."timestamp" < *{USAGE_TIME_STOP});

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

\p Importing UDC data

\m commands.csv

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

\p Importing the sizes of refactorings

\m refactoring_change_intensity.csv

* *DSV_TARGET_TABLE ="PUBLIC"."UDC_ECLIPSE_MAPPING"

DROP TABLE "PUBLIC"."UDC_ECLIPSE_MAPPING" IF EXISTS;

CREATE TABLE "PUBLIC"."UDC_ECLIPSE_MAPPING" (

  "UDCID" VARCHAR(1000),

  "ECLIPSEID" VARCHAR(1000)

);

\p Importing the mapping between the IDs of refactorings in UDC data and Eclipse refactoring histories

\m refactoringmapping.csv

\p Importing the mapping between the IDs of refactorings and their human readable names

DROP TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" (

  "REFACTORING_ID" VARCHAR(100),

  "HUMAN_READABLE_NAME" VARCHAR(100)

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME"

\m refactoring_id_human_name_mapping.csv

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

