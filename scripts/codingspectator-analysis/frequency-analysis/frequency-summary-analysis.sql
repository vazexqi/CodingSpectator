--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

DROP TABLE "PUBLIC"."PER_REFACTORING_ID_SUMMARY" IF EXISTS;

CREATE TABLE "PUBLIC"."PER_REFACTORING_ID_SUMMARY" (

  "REFACTORING_HUMAN_READABLE_NAME" VARCHAR(100),

  "COMPLEXITY" VARCHAR(5),

  "CODINGSPECTATOR_PERFORMED_COUNT" INT,

  "CODINGTRACKER_PERFORMED_COUNT" INT,

  "CODINGSPECTATOR_CANCELED_COUNT" INT,

--  "CODINGTRACKER_UNDONE_COUNT" INT,

--  "CODINGTRACKER_REDONE_COUNT" INT,

--  "CODINGSPECTATOR_UNAVAILABLE_COUNT" INT,

  "WARNING_STATUS_COUNT" INT,

  "ERROR_STATUS_COUNT" INT,

  "FATALERROR_STATUS_COUNT" INT,

  "INVOKED_BY_QUICKASSIST_COUNT" INT,

  "PREVIEW_COUNT" INT,

  "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_SEC" NUMERIC(5, 1),

  "AVG_AFFECTED_LINES" NUMERIC(5, 2),

  "AVG_AFFECTED_FILES" NUMERIC(5, 2),

--  "P_UNDONE_GIVEN_PERFORMED" NUMERIC(3, 2),

--  "P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA" NUMERIC(3, 2),

  "P_PERFORMED_GIVEN_WARNING" NUMERIC(3, 2),

  "P_PERFORMED_GIVEN_ERROR" NUMERIC(3, 2),

  "P_PERFORMED_GIVEN_WARNING_OR_ERROR" NUMERIC(3, 2)

);

INSERT INTO "PUBLIC"."PER_REFACTORING_ID_SUMMARY" (

  "REFACTORING_HUMAN_READABLE_NAME",

  "COMPLEXITY",

  "CODINGSPECTATOR_PERFORMED_COUNT",

  "CODINGTRACKER_PERFORMED_COUNT",

  "CODINGSPECTATOR_CANCELED_COUNT",

--  "CODINGTRACKER_UNDONE_COUNT",

--  "CODINGTRACKER_REDONE_COUNT",

--  "CODINGSPECTATOR_UNAVAILABLE_COUNT",

  "WARNING_STATUS_COUNT",

  "ERROR_STATUS_COUNT",

  "FATALERROR_STATUS_COUNT",

  "INVOKED_BY_QUICKASSIST_COUNT",

  "PREVIEW_COUNT",

  "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_SEC",

  "AVG_AFFECTED_LINES",

  "AVG_AFFECTED_FILES",

--  "P_UNDONE_GIVEN_PERFORMED",

--  "P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA",

  "P_PERFORMED_GIVEN_WARNING",

  "P_PERFORMED_GIVEN_ERROR",

  "P_PERFORMED_GIVEN_WARNING_OR_ERROR"

) SELECT

(SELECT "H"."HUMAN_READABLE_NAME"

FROM "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" AS "H"

WHERE "H"."REFACTORING_ID" = JAVA_REFACTORING_ID("R"."REFACTORING_ID")) AS
"REFACTORING_HUMAN_READABLE_NAME",

(SELECT "RC"."COMPLEXITY"

FROM "PUBLIC"."REFACTORING_COMPLEXITY" AS "RC"

WHERE "RC"."REFACTORING_ID" = "R"."REFACTORING_ID") AS "COMPLEXITY",

"R"."CODINGSPECTATOR_PERFORMED_COUNT" AS "CODINGSPECTATOR_PERFORMED_COUNT",

"R"."CODINGTRACKER_PERFORMED_COUNT" AS "CODINGTRACKER_PERFORMED_COUNT",

"R"."CODINGSPECTATOR_CANCELED_COUNT" AS "CODINGSPECTATOR_CANCELED_COUNT",

--"R"."CODINGTRACKER_UNDONE_COUNT" AS "CODINGTRACKER_UNDONE_COUNT",

--"R"."CODINGTRACKER_REDONE_COUNT" AS "CODINGTRACKER_REDONE_COUNT",

--"R"."CODINGSPECTATOR_UNAVAILABLE_COUNT" AS "CODINGSPECTATOR_UNAVAILABLE_COUNT",

"R"."WARNING_STATUS_COUNT" AS "WARNING_STATUS_COUNT",

"R"."ERROR_STATUS_COUNT" AS "ERROR_STATUS_COUNT",

"R"."FATALERROR_STATUS_COUNT" AS "FATALERROR_STATUS_COUNT",

"R"."INVOKED_BY_QUICKASSIST_COUNT" AS "INVOKED_BY_QUICKASSIST_COUNT",

"R"."PREVIEW_COUNT" AS "PREVIEW_COUNT",

("R"."AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC" / 1000.0)
AS "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_SEC",

"A"."AVG_AFFECTED_LINES" AS "AVG_AFFECTED_LINES",

"A"."AVG_AFFECTED_FILES" AS "AVG_AFFECTED_FILES",

--"R"."P_UNDONE_GIVEN_PERFORMED" AS "P_UNDONE_GIVEN_PERFORMED",

--"R"."P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA" AS
--"P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA",

"R"."P_PERFORMED_GIVEN_WARNING" AS "P_PERFORMED_GIVEN_WARNING",

"R"."P_PERFORMED_GIVEN_ERROR" AS "P_PERFORMED_GIVEN_ERROR",

"R"."P_PERFORMED_GIVEN_WARNING_OR_ERROR" AS
"P_PERFORMED_GIVEN_WARNING_OR_ERROR"

--"R"."P_UNAVAILABLE_GIVEN_UNAVAILABLE_OR_CANCELED_OR_PERFORMED" AS
--"P_Unavailable",

--"R"."P_WARNING_GIVEN_PERFORMED_OR_CANCELED" AS "P_Warning",

--"R"."P_ERROR_GIVEN_PERFORMED_OR_CANCELED" AS "P_Error",

--"R"."P_FATAL_GIVEN_PERFORMED_OR_CANCELED" AS "P_Fatal_or_Error",

--"R"."P_UNAVAILABLE_OR_WARNING_OR_ERROR_OR_FATAL_GIVEN_CS_REFACTORINGS" AS
--"P_Unavailable_or_Warning_or_Error_or_Fatal_Error",

--"R"."P_CANCELED_GIVEN_WARNING_OR_ERROR_OR_FATAL" AS
--"P_Canceled_Given_Warning_or_Error_or_Fatal_Error",

FROM

"PUBLIC"."PER_REFACTORING_ID" "R" LEFT OUTER JOIN

(SELECT

"RCS"."REFACTORING_ID" AS "REFACTORING_ID",

COUNT("RCS"."USERNAME") AS "INVOCATION_COUNT",

CONVERT(AVG(CONVERT("RCS"."AFFECTED_FILES_COUNT", SQL_FLOAT)), NUMERIC(5, 2))
AS "AVG_AFFECTED_FILES",

CONVERT(AVG(CONVERT("RCS"."AFFECTED_LINES_COUNT", SQL_FLOAT)), NUMERIC(5, 2))
AS "AVG_AFFECTED_LINES"

--SUM("RCS"."AFFECTED_FILES_COUNT") AS "SUM_AFFECTED_FILES",

--SUM("RCS"."AFFECTED_LINES_COUNT") AS "SUM_AFFECTED_LINES"

FROM "PUBLIC"."JAVA_REFACTORING_CHANGE_SIZE" "RCS"

WHERE IS_JAVA_REFACTORING("RCS"."REFACTORING_ID")

GROUP BY "RCS"."REFACTORING_ID"

ORDER BY "RCS"."REFACTORING_ID") "A"

ON "R"."REFACTORING_ID" = "A"."REFACTORING_ID"

WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("R"."REFACTORING_ID")

ORDER BY "REFACTORING_HUMAN_READABLE_NAME";

* *DSV_COL_DELIM =,

* *DSV_ROW_DELIM =\n

* *DSV_TARGET_FILE =UsageTableFor2012ICSE.csv

\x SELECT * FROM "PUBLIC"."PER_REFACTORING_ID_SUMMARY"

* *DSV_COL_DELIM =,

* *DSV_ROW_DELIM =\n

* *DSV_TARGET_FILE =ICSE2012UsageTableSummary.csv

DROP TABLE "PUBLIC"."USAGE_SUMMARY" IF EXISTS;

CREATE TABLE "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME" VARCHAR(1000),

  "VALUE_INT" INT,

  "VALUE_NUMERIC" NUMERIC(5, 2)

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_CODINGSPECTATOR_PERFORMED_COUNT',

  (SELECT SUM("CODINGSPECTATOR_PERFORMED_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_CODINGTRACKER_PERFORMED_COUNT',

  (SELECT SUM("CODINGTRACKER_PERFORMED_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_CODINGSPECTATOR_CANCELED_COUNT',

  (SELECT SUM("T"."CODINGSPECTATOR_CANCELED_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_WARNING_STATUS_COUNT',

  (SELECT SUM("WARNING_STATUS_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_ERROR_STATUS_COUNT',

  (SELECT SUM("ERROR_STATUS_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_FATALERROR_STATUS_COUNT',

  (SELECT SUM("FATALERROR_STATUS_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_INVOKED_BY_QUICKASSIST_COUNT',

  (SELECT SUM("INVOKED_BY_QUICKASSIST_COUNT") FROM
"PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'TOTAL_PREVIEW_COUNT',

  (SELECT SUM("PREVIEW_COUNT") FROM "PUBLIC"."PER_REFACTORING_ID_SUMMARY" "T"),

  NULL
  
);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'AVERAGE_CONFIGURATION_TIME_IN_SECONDS',

  NULL,

  (SELECT
  
  CONVERT(AVG(
  
    CASE
  
    WHEN NOT (

(IS_CODINGSPECTATOR_PERFORMED("AD"."recorder","AD"."refactoring kind") AND
"AD"."navigation duration" <> '')

 OR

(IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind") AND
"AD"."navigation duration" <> '')) THEN NULL
  
    ELSE CAST("AD"."navigation duration" AS BIGINT) END
  
  ) / 1000.0, NUMERIC(5, 2))
  
  FROM "PUBLIC"."ALL_DATA" "AD"
  
  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("AD"."id"))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'AVERAGE_NUMBER_OF_AFFECTED_LINES',

  NULL,

  (SELECT

  CONVERT(AVG(CONVERT("T"."AFFECTED_LINES_COUNT", SQL_FLOAT)), NUMERIC(5, 2))
  
  FROM "PUBLIC"."JAVA_REFACTORING_CHANGE_SIZE" "T"
  
  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("T"."REFACTORING_ID"))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'AVERAGE_NUMBER_OF_AFFECTED_FILES',

  NULL,

  (SELECT

  CONVERT(AVG(CONVERT("T"."AFFECTED_FILES_COUNT", SQL_FLOAT)), NUMERIC(5, 2))
  
  FROM "PUBLIC"."JAVA_REFACTORING_CHANGE_SIZE" "T"
  
  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("T"."REFACTORING_ID"))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'P_PERFORMED_GIVEN_WARNING',

  NULL,

  (SELECT CASE COUNT(CASE "AD"."status" WHEN LIKE '_WARNING%' THEN 1 ELSE NULL
END)
  
  WHEN 0 THEN NULL
  
  ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / COUNT(CASE
"AD"."status" WHEN LIKE '_WARNING%' THEN 1 ELSE NULL END) END AS
"P_PERFORMED_GIVEN_WARNING"

  FROM "PUBLIC"."ALL_DATA" "AD"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("AD"."id"))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'P_PERFORMED_GIVEN_ERROR',

  NULL,

  (SELECT CASE COUNT(CASE "AD"."status" WHEN LIKE '_ERROR%' THEN 1 ELSE NULL
END)

  WHEN 0 THEN NULL
  
  ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / COUNT(CASE
"AD"."status" WHEN LIKE '_ERROR%' THEN 1 ELSE NULL END) END AS
"P_PERFORMED_GIVEN_ERROR"

  FROM "PUBLIC"."ALL_DATA" "AD"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("AD"."id"))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'P_PERFORMED_GIVEN_WARNING_OR_ERROR',

  NULL,

  (SELECT CASE COUNT(CASE "AD"."status" WHEN LIKE '_WARNING%' THEN 1 WHEN LIKE
'_ERROR%' THEN 1 ELSE NULL END)
  
  WHEN 0 THEN NULL
  
  ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status") OR
IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
  
  "AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / COUNT(CASE
"AD"."status" WHEN LIKE '_WARNING%' THEN 1 WHEN LIKE '_ERROR%' THEN 1 ELSE NULL
END) END AS "P_PERFORMED_GIVEN_WARNING_OR_ERROR"

  FROM "PUBLIC"."ALL_DATA" "AD"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("AD"."id"))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'PERCENTAGE_OF_QUICK_ASSIST_REFACTORINGS_INVOKED_BY_QUICK_ASSIST',

  NULL,

  CONVERT(100.0 * (SELECT SUM("T"."INVOKED_BY_QUICKASSIST_COUNT")

  FROM "PUBLIC"."PER_REFACTORING_ID" "T"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("T"."REFACTORING_ID")) /

  (SELECT SUM("T"."CODINGSPECTATOR_PERFORMED_COUNT")

  FROM "PUBLIC"."PER_REFACTORING_ID" "T"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("T"."REFACTORING_ID") AND
IS_REFACTORING_ID_IN_QUICK_ASSIST("T"."REFACTORING_ID")), NUMERIC(5, 2))

);

INSERT INTO "PUBLIC"."USAGE_SUMMARY" (

  "VARIABLE_NAME",

  "VALUE_INT",

  "VALUE_NUMERIC"

)

VALUES (

  'PERCENTAGE_OF_NON_RENAME_QUICK_ASSIST_REFACTORINGS_INVOKED_BY_QUICK_ASSIST',

  NULL,

  CONVERT(100.0 * (SELECT SUM("T"."INVOKED_BY_QUICKASSIST_COUNT")

  FROM "PUBLIC"."PER_REFACTORING_ID" "T"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("T"."REFACTORING_ID") AND NOT
IS_JAVA_RENAME_REFACTORING_ID("T"."REFACTORING_ID")) /

  (SELECT SUM("T"."CODINGSPECTATOR_PERFORMED_COUNT")

  FROM "PUBLIC"."PER_REFACTORING_ID" "T"

  WHERE IS_REFACTORING_ID_IN_ICSE2012_PAPER("T"."REFACTORING_ID") AND
IS_REFACTORING_ID_IN_QUICK_ASSIST("T"."REFACTORING_ID") AND NOT
IS_JAVA_RENAME_REFACTORING_ID("T"."REFACTORING_ID")), NUMERIC(5, 2))

);

\x SELECT * FROM "PUBLIC"."USAGE_SUMMARY"

