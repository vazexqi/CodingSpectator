--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

DROP TABLE "PUBLIC"."PER_REFACTORING_ID" IF EXISTS;

CREATE TABLE "PUBLIC"."PER_REFACTORING_ID" (

  "REFACTORING_ID" VARCHAR(100),

  "ECLIPSE_PERFORMED_COUNT" INT,

--  "UDC_PERFORMED_COUNT" INT,

  "CODINGTRACKER_PERFORMED_COUNT" INT,

  "CODINGTRACKER_UNDONE_COUNT" INT,

  "CODINGTRACKER_REDONE_COUNT" INT,

  "CODINGSPECTATOR_PERFORMED_COUNT" INT,

  "CODINGSPECTATOR_CANCELED_COUNT" INT,

  "CODINGSPECTATOR_UNAVAILABLE_COUNT" INT,

  "INVOKED_BY_QUICKASSIST_COUNT" INT,

  "INVOKED_THROUGH_STRUCTURED_SELECTION_COUNT" INT,

  "NAVIGATION_HISTORY_COUNT" INT,

  "PREVIEW_COUNT" INT,

  "CODINGSPECTATOR_PERFORMED_WITH_CONFIGURATION_COUNT" INT,

  "CODINGSPECTATOR_CANCELED_WITH_CONFIGURATION_COUNT" INT,

  "CODINGSPECTATOR_PERFORMED_OR_CANCELED_WITH_CONFIGURATION_COUNT" INT,

  "AVG_PERFORMED_CONFIGURATION_DURATION_IN_MILLI_SEC" BIGINT,

  "AVG_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC" BIGINT,

  "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC" BIGINT,

  "OK_STATUS_COUNT" INT,

  "WARNING_STATUS_COUNT" INT,

  "ERROR_STATUS_COUNT" INT,

  "FATALERROR_STATUS_COUNT" INT,

  "OTHER_STATUS_COUNT" INT,

  "EMPTY_STATUS_COUNT" INT,

  "PERFORMED_WARNING_STATUS_COUNT" INT,

  "PERFORMED_ERROR_STATUS_COUNT" INT,

   -- Probabilities based on CodingTracker
  "P_UNDONE_GIVEN_PERFORMED" NUMERIC(3, 2),

   -- Probabilities based on CodingSpectator
  "P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA" NUMERIC(3, 2),

  "P_UNDONE_OR_CANCELED_GIVEN_AVAILABLE" NUMERIC(3, 2),

  "P_UNAVAILABLE_GIVEN_UNAVAILABLE_OR_CANCELED_OR_PERFORMED" NUMERIC(3, 2),

  "P_WARNING_GIVEN_PERFORMED_OR_CANCELED" NUMERIC(3, 2),

  "P_ERROR_GIVEN_PERFORMED_OR_CANCELED" NUMERIC(3, 2),

  "P_FATAL_GIVEN_PERFORMED_OR_CANCELED" NUMERIC(3, 2),

  "P_UNAVAILABLE_OR_WARNING_OR_ERROR_OR_FATAL_GIVEN_CS_REFACTORINGS" NUMERIC(3,
2),

  "P_CANCELED_GIVEN_WARNING_OR_ERROR_OR_FATAL" NUMERIC(3, 2),

  "P_PERFORMED_GIVEN_WARNING" NUMERIC(3, 2),

  "P_PERFORMED_GIVEN_ERROR" NUMERIC(3, 2),

  "P_PERFORMED_GIVEN_WARNING_OR_ERROR" NUMERIC(3, 2)

);

INSERT INTO "PUBLIC"."PER_REFACTORING_ID" (

  "REFACTORING_ID",

  "ECLIPSE_PERFORMED_COUNT",

--  "UDC_PERFORMED_COUNT",

  "CODINGTRACKER_PERFORMED_COUNT",

  "CODINGTRACKER_UNDONE_COUNT",

  "CODINGTRACKER_REDONE_COUNT",

  "CODINGSPECTATOR_PERFORMED_COUNT",

  "CODINGSPECTATOR_CANCELED_COUNT",

  "CODINGSPECTATOR_UNAVAILABLE_COUNT",

  "INVOKED_BY_QUICKASSIST_COUNT",

  "INVOKED_THROUGH_STRUCTURED_SELECTION_COUNT",

  "NAVIGATION_HISTORY_COUNT",

  "PREVIEW_COUNT",

  "CODINGSPECTATOR_PERFORMED_WITH_CONFIGURATION_COUNT",

  "CODINGSPECTATOR_CANCELED_WITH_CONFIGURATION_COUNT",

  "CODINGSPECTATOR_PERFORMED_OR_CANCELED_WITH_CONFIGURATION_COUNT",

  "AVG_PERFORMED_CONFIGURATION_DURATION_IN_MILLI_SEC",

  "AVG_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC",

  "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC",

  "OK_STATUS_COUNT",

  "WARNING_STATUS_COUNT",

  "ERROR_STATUS_COUNT",

  "FATALERROR_STATUS_COUNT",

  "OTHER_STATUS_COUNT",

  "EMPTY_STATUS_COUNT",

  "PERFORMED_WARNING_STATUS_COUNT",

  "PERFORMED_ERROR_STATUS_COUNT",

  "P_UNDONE_GIVEN_PERFORMED",

  "P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA",

  "P_UNDONE_OR_CANCELED_GIVEN_AVAILABLE",

  "P_UNAVAILABLE_GIVEN_UNAVAILABLE_OR_CANCELED_OR_PERFORMED",

  "P_WARNING_GIVEN_PERFORMED_OR_CANCELED",

  "P_ERROR_GIVEN_PERFORMED_OR_CANCELED",

  "P_FATAL_GIVEN_PERFORMED_OR_CANCELED",

  "P_UNAVAILABLE_OR_WARNING_OR_ERROR_OR_FATAL_GIVEN_CS_REFACTORINGS",

  "P_CANCELED_GIVEN_WARNING_OR_ERROR_OR_FATAL",

  "P_PERFORMED_GIVEN_WARNING",

  "P_PERFORMED_GIVEN_ERROR",

  "P_PERFORMED_GIVEN_WARNING_OR_ERROR"

)

SELECT

JAVA_REFACTORING_ID("AD"."id") AS "REFACTORING_ID",

COUNT(NULLIF(IS_ECLIPSE_PERFORMED("AD"."recorder", "AD"."refactoring kind"),
FALSE)) AS "ECLIPSE_PERFORMED_COUNT",

/*
(SELECT SUM("PUBLIC"."UDC_DATA"."EXECUTECOUNT")

FROM "PUBLIC"."UDC_DATA"

WHERE "PUBLIC"."UDC_DATA"."COMMAND" = (SELECT
"PUBLIC"."UDC_ECLIPSE_MAPPING"."UDCID"

FROM "PUBLIC"."UDC_ECLIPSE_MAPPING"

WHERE "PUBLIC"."UDC_ECLIPSE_MAPPING"."ECLIPSEID" =
JAVA_REFACTORING_ID("AD"."id"))

GROUP BY "PUBLIC"."UDC_DATA"."COMMAND") AS "UDC_PERFORMED_COUNT",
*/

COUNT(NULLIF(

IS_CODINGTRACKER_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
AS "CODINGTRACKER_PERFORMED_COUNT",

COUNT(NULLIF(

IS_CODINGTRACKER_UNDONE("AD"."recorder", "AD"."refactoring kind"), FALSE)) AS
"CODINGTRACKER_UNDONE_COUNT",

COUNT(NULLIF(IS_CODINGTRACKER_REDONE("AD"."recorder", "AD"."refactoring kind"),
FALSE)) AS "CODINGTRACKER_REDONE_COUNT",

COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
AS "CODINGSPECTATOR_PERFORMED_COUNT",

COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
AS "CODINGSPECTATOR_CANCELED_COUNT",

COUNT(NULLIF(

IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder", "AD"."refactoring kind"),
FALSE)) AS "CODINGSPECTATOR_UNAVAILABLE_COUNT",

COUNT(NULLIF("AD"."invoked-by-quickassist" <> 'true', TRUE)) AS
"INVOKED_BY_QUICKASSIST_COUNT",

COUNT(NULLIF("AD"."invoked-through-structured-selection" <> 'true', TRUE)) AS
"INVOKED_THROUGH_STRUCTURED_SELECTION_COUNT",

COUNT(NULLIF("AD"."navigation-history", '')) AS "NAVIGATION_HISTORY_COUNT",

COUNT(NULLIF("AD"."navigation-history" NOT LIKE '%Previe%w%', TRUE)) AS
"PREVIEW_COUNT",

COUNT(NULLIF("AD"."recorder" = 'CODINGSPECTATOR' AND "AD"."refactoring kind" =
'PERFORMED' AND "AD"."navigation duration" <> '', FALSE)) AS
"CODINGSPECTATOR_PERFORMED_WITH_CONFIGURATION_COUNT",

COUNT(NULLIF("AD"."recorder" = 'CODINGSPECTATOR' AND "AD"."refactoring kind" =
'CANCELLED' AND "AD"."navigation duration" <> '', FALSE)) AS
"CODINGSPECTATOR_CANCELEDED_WITH_CONFIGURATION_COUNT",

COUNT(NULLIF(("AD"."recorder" = 'CODINGSPECTATOR' AND "AD"."refactoring kind" =
'PERFORMED' AND "AD"."navigation duration" <> '') OR ("AD"."recorder" =
'CODINGSPECTATOR' AND "AD"."refactoring kind" = 'CANCELLED' AND
"AD"."navigation duration" <> ''), FALSE)) AS
"CODINGSPECTATOR_PERFORMED_OR_CANCELED_WITH_CONFIGURATION_COUNT",

AVG(

  CASE

  WHEN NOT ("AD"."recorder" = 'CODINGSPECTATOR' AND "AD"."refactoring kind" =
'PERFORMED' AND "AD"."navigation duration" <> '') THEN NULL

  ELSE CAST("AD"."navigation duration" AS BIGINT) END

) AS "AVG_PERFORMED_CONFIGURATION_DURATION_IN_MILLI_SEC",

AVG(

  CASE

  WHEN NOT ("AD"."recorder" = 'CODINGSPECTATOR' AND "AD"."refactoring kind" =
'CANCELLED' AND "AD"."navigation duration" <> '') THEN NULL

  ELSE CAST("AD"."navigation duration" AS BIGINT) END

) AS "AVG_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC",

AVG(

  CASE

  WHEN NOT (("AD"."recorder" = 'CODINGSPECTATOR' AND "AD"."refactoring kind" =
'PERFORMED' AND "AD"."navigation duration" <> '') OR ("AD"."recorder" =
'CODINGSPECTATOR' AND "AD"."refactoring kind" = 'CANCELLED' AND
"AD"."navigation duration" <> '')) THEN NULL

  ELSE CAST("AD"."navigation duration" AS BIGINT) END

) AS "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC",

COUNT(CASE "AD"."status" WHEN LIKE '_OK%' THEN 1 ELSE NULL END) AS
"OK_STATUS_COUNT",

COUNT(CASE "AD"."status" WHEN LIKE '_WARNING%' THEN 1 ELSE NULL END) AS
"WARNING_STATUS_COUNT",

COUNT(CASE "AD"."status" WHEN LIKE '_ERROR%' THEN 1 ELSE NULL END) AS
"ERROR_STATUS_COUNT",

COUNT(CASE "AD"."status" WHEN LIKE '_FATALERROR%' THEN 1 ELSE NULL END) AS
"FATALERROR_STATUS_COUNT",

COUNT(CASE "AD"."status"

  WHEN LIKE '_OK%' THEN NULL

  WHEN LIKE '_WARNING%' THEN NULL

  WHEN LIKE '_ERROR%' THEN NULL

  WHEN LIKE '_FATALERROR%' THEN NULL

  WHEN LIKE '' THEN NULL

  ELSE 1 END) AS "OTHER_STATUS_COUNT",

COUNT(CASE "AD"."status" WHEN '' THEN 1 ELSE NULL END) AS "EMPTY_STATUS_COUNT",

COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS
"PERFORMED_WARNING_STATUS_COUNT",

COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS
"PERFORMED_ERROR_STATUS_COUNT",

CASE COUNT(NULLIF(

IS_CODINGTRACKER_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))

WHEN 0 THEN NULL

ELSE CAST(COUNT(NULLIF(IS_CODINGTRACKER_UNDONE("AD"."recorder",
"AD"."refactoring kind"), FALSE)) AS REAL) / COUNT(NULLIF(

IS_CODINGTRACKER_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
END AS "P_UNDONE_GIVEN_PERFORMED",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) + COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind") AND
"AD"."invoked-by-quickassist" <> 'true', FALSE)))

WHEN 0 THEN NULL

ELSE CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) AS REAL) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind") AND
"AD"."invoked-by-quickassist" <> 'true', FALSE))) END AS
"P_CANCELED_GIVEN_CANCELED_OR_PERFORMED_WITHOUT_QA",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) + COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"),
FALSE)))

WHEN 0 THEN NULL

ELSE (CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) AS REAL) + (SELECT "T"."UNDONE_COUNT"FROM
"PUBLIC"."UNDONE_CODINGSPECTATOR_COUNT" AS "T" WHERE "T"."REFACTORING_ID" =
"AD"."id")) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"),
FALSE))) END AS "P_UNDONE_OR_CANCELED_GIVEN_AVAILABLE",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) + COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder",
"AD"."refactoring kind"), FALSE)))

WHEN 0 THEN NULL

ELSE CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder",
"AD"."refactoring kind"), FALSE)) AS REAL) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder", "AD"."refactoring kind"),
FALSE))) END AS "P_UNAVAILABLE_GIVEN_UNAVAILABLE_OR_CANCELED_OR_PERFORMED",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) + COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE)))

WHEN 0 THEN NULL

ELSE
(CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL)) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE)))
END AS "P_WARNING_GIVEN_PERFORMED_OR_CANCELED",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) + COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE)))

WHEN 0 THEN NULL

ELSE
(CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL)) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE)))
END AS "P_ERROR_GIVEN_PERFORMED_OR_CANCELED",

CASE (COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE)))

WHEN 0 THEN NULL

ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE)))
END AS "P_FATAL_GIVEN_PERFORMED_OR_CANCELED",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED("AD"."recorder",
"AD"."refactoring kind"), FALSE)) + COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder",
"AD"."refactoring kind"), FALSE)))

WHEN 0 THEN NULL

ELSE ( CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder",
"AD"."refactoring kind"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL)) / (COUNT(NULLIF(

IS_CODINGSPECTATOR_PERFORMED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(

IS_CODINGSPECTATOR_CANCELED("AD"."recorder", "AD"."refactoring kind"), FALSE))
+ COUNT(NULLIF(IS_CODINGSPECTATOR_UNAVAILABLE("AD"."recorder",
"AD"."refactoring kind"), FALSE))) END AS
"P_UNAVAILABLE_OR_WARNING_OR_ERROR_OR_FATAL_GIVEN_CS_REFACTORINGS",

CASE (COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)))

WHEN 0 THEN NULL

ELSE
(CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) +
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL)) /
(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) +
COUNT(NULLIF(IS_CODINGSPECTATOR_CANCELED_FATAL_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE))) END AS
"P_CANCELED_GIVEN_WARNING_OR_ERROR_OR_FATAL",

CASE COUNT(CASE "AD"."status" WHEN LIKE '_WARNING%' THEN 1 ELSE NULL END)

WHEN 0 THEN NULL

ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / COUNT(CASE
"AD"."status" WHEN LIKE '_WARNING%' THEN 1 ELSE NULL END) END AS
"P_PERFORMED_GIVEN_WARNING",

CASE COUNT(CASE "AD"."status" WHEN LIKE '_ERROR%' THEN 1 ELSE NULL END)

WHEN 0 THEN NULL

ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / COUNT(CASE
"AD"."status" WHEN LIKE '_ERROR%' THEN 1 ELSE NULL END) END AS
"P_PERFORMED_GIVEN_ERROR",

CASE COUNT(CASE "AD"."status" WHEN LIKE '_WARNING%' THEN 1 WHEN LIKE '_ERROR%'
THEN 1 ELSE NULL END)

WHEN 0 THEN NULL

ELSE
CAST(COUNT(NULLIF(IS_CODINGSPECTATOR_PERFORMED_WARNING_STATUS("AD"."recorder",
"AD"."refactoring kind", "AD"."status") OR
IS_CODINGSPECTATOR_PERFORMED_ERROR_STATUS("AD"."recorder",

"AD"."refactoring kind", "AD"."status"), FALSE)) AS REAL) / COUNT(CASE
"AD"."status" WHEN LIKE '_WARNING%' THEN 1 WHEN LIKE '_ERROR%' THEN 1 ELSE NULL
END) END AS "P_PERFORMED_GIVEN_WARNING_OR_ERROR"

FROM "PUBLIC"."ALL_DATA" "AD"

WHERE IS_JAVA_REFACTORING("AD"."id") AND "AD"."username" LIKE 'cs-___'

GROUP BY JAVA_REFACTORING_ID("AD"."id")

ORDER BY JAVA_REFACTORING_ID("AD"."id");

* *DSV_COL_DELIM =,

* *DSV_ROW_DELIM =\n

* *DSV_TARGET_FILE =PerRefactoringID.csv

\x SELECT * FROM "PUBLIC"."PER_REFACTORING_ID"


