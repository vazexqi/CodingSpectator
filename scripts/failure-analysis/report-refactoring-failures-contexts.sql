--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

--This script generates a report of all events that are close to certain kinds of refactoring failures.

\i summarize-refactoring-messages.sql

\c false

* WORKSPACE_ID_MAX_LENGTH=100000
* REFACTORING_ID_UNDER_STUDY='org.eclipse.jdt.ui.extract.method'
* REFACTORING_MESSAGE_PATTERN_ID_UNDER_STUDY='MP21'

DROP INDEX "TIMESTAMP_INDEX" IF EXISTS;

CREATE INDEX "TIMESTAMP_INDEX" ON "PUBLIC"."ALL_DATA" ("timestamp");

DROP TABLE "PUBLIC"."REFACTORINGS_UNDER_STUDY" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORINGS_UNDER_STUDY" (

  "WORKSPACE_ID" VARCHAR(100000),

  "TIMESTAMP" BIGINT

);

INSERT INTO "PUBLIC"."REFACTORINGS_UNDER_STUDY" (

  "WORKSPACE_ID",

  "TIMESTAMP"

)

SELECT

"T"."WORKSPACE_ID" AS "WORKSPACE_ID",

"T"."TIMESTAMP" AS "TIMESTAMP"

FROM "PUBLIC"."REFACTORING_MESSAGES" "T"

WHERE "T"."REFACTORING_ID" = *{REFACTORING_ID_UNDER_STUDY} AND
"T"."MESSAGE_PATTERN_ID" = *{REFACTORING_MESSAGE_PATTERN_ID_UNDER_STUDY};

* *DSV_COL_DELIM=\n

* *DSV_ROW_DELIM=\n\n\n

* *DSV_TARGET_FILE=RefactoringsUnderStudy.csv

\.

SELECT

'id:' || "id",

'recorder:' || "recorder",

'human-readable timestamp:' || "human-readable timestamp",

'timestamp:' || "timestamp",

'username:' || "username",

'workspace ID:' || "workspace ID",

'captured-by-codingspectator:' || "captured-by-codingspectator",

'refactoring kind:' || "refactoring kind",

'code-snippet-with-selection-markers:' ||
"code-snippet-with-selection-markers",

'codingspectator version:' || "codingspectator version",

'codingtracker description:' || "codingtracker description",

'comment:' || "comment",

'description:' || "description",

'status:' || "status",

'invoked-by-quickassist:' || "invoked-by-quickassist",

'invoked-through-structured-selection:' ||
"invoked-through-structured-selection",

'navigation duration:' || "navigation duration",

'navigation-history:' || "navigation-history"

FROM "PUBLIC"."ALL_DATA" "T1"

WHERE

EXISTS (

SELECT "T2"."TIMESTAMP"

FROM "PUBLIC"."REFACTORINGS_UNDER_STUDY" "T2"

WHERE "T2"."WORKSPACE_ID" = "T1"."workspace ID" AND 

ABS("T2"."TIMESTAMP" - "T1"."timestamp") < 5 * 60 * 1000

)

ORDER BY "T1"."username", "T1"."workspace ID", "T1"."timestamp";

.;

\xq :

