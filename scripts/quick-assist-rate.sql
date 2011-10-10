--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

/* 
Ensure that analysis-queries.sql and summarize-usage-times.sql are run first
before running this file. For the total performed, I am using CodingTracker data
instead of CodingSpectator data because we are interested in total counts.
*/

* *DSV_COL_DELIM=,
* *DSV_TARGET_FILE=QuickAssistSupportedRefactoringsRate.csv

\x SELECT "P"."username" AS USERNAME, COUNT(NULLIF(IS_CODINGTRACKER_PERFORMED("P"."recorder", "P"."refactoring kind"), FALSE)) AS "PERFORMED", (SELECT "T"."USAGE_TIME_IN_HOURS" FROM "PUBLIC"."USAGE_TIME_PER_USER" AS "T" WHERE "T"."USERNAME" = "P"."username") AS "HOURS", CASE (SELECT "T"."USAGE_TIME_IN_HOURS" FROM "PUBLIC"."USAGE_TIME_PER_USER" AS "T" WHERE "T"."USERNAME" = "P"."username") WHEN 0 THEN NULL ELSE (CONVERT(COUNT(NULLIF(IS_CODINGTRACKER_PERFORMED("P"."recorder", "P"."refactoring kind"), FALSE)), SQL_FLOAT) / (SELECT "T"."USAGE_TIME_IN_HOURS" FROM "PUBLIC"."USAGE_TIME_PER_USER" AS "T" WHERE "T"."USERNAME" = "P"."username")) END AS "REFACTORING RATE" FROM "PUBLIC"."ALL_DATA" AS "P" WHERE IS_JAVA_REFACTORING("P"."id") AND "P"."username" LIKE 'cs-___' AND ("P"."id" = 'org.eclipse.jdt.ui.promote.temp' OR "P"."id" = 'org.eclipse.jdt.ui.extract.constant' OR "P"."id" = 'org.eclipse.jdt.ui.extract.temp' OR "P"."id" = 'org.eclipse.jdt.ui.extract.method' OR "P"."id" = 'org.eclipse.jdt.ui.inline.temp' OR "P"."id" = 'org.eclipse.jdt.ui.convert.anonymous' OR "P"."id" = 'org.eclipse.jdt.ui.promote.temp' OR "P"."id" LIKE '%rename%' ) GROUP BY "P"."username" ORDER BY "P"."username";