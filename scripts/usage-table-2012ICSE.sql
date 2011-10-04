DROP TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" (
  REFACTORING_ID VARCHAR(100),
  HUMAN_READABLE_NAME VARCHAR(100)
);

* *DSV_COL_SPLITTER = ,
* *DSV_TARGET_TABLE = "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME"

\m refactoring_id_human_name_mapping.csv

* *DSV_COL_DELIM=,
* *DSV_TARGET_FILE=UsageTableFor2012ICSE.csv

-- It seems that "Infer Generic Type" was never used so we special case it here
INSERT INTO "PUBLIC"."PER_REFACTORING_ID" ("REFACTORING_ID", "CODINGTRACKER_PERFORMED_COUNT", "CODINGTRACKER_UNDONE_COUNT", "CODINGTRACKER_REDONE_COUNT", "CODINGSPECTATOR_PERFORMED_COUNT", "CODINGSPECTATOR_CANCELED_COUNT", "CODINGSPECTATOR_UNAVAILABLE_COUNT", "INVOKED_BY_QUICKASSIST_COUNT", "PREVIEW_COUNT", "AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC")
VALUES ('org.eclipse.jdt.ui.change.type', 0, 0, 0, 0, 0, 0, 0, 0, 0);

\x SELECT (SELECT "H"."HUMAN_READABLE_NAME" FROM "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" AS "H" WHERE "H"."REFACTORING_ID" = "R"."REFACTORING_ID") AS "Refactoring Tool", "R"."CODINGTRACKER_PERFORMED_COUNT" AS "CT_Performed", "R"."CODINGTRACKER_UNDONE_COUNT" AS "CT_Undo", "CODINGTRACKER_REDONE_COUNT" AS "CT_Redo", "R"."CODINGSPECTATOR_PERFORMED_COUNT" AS "CS_Performed", "R"."CODINGSPECTATOR_CANCELED_COUNT" AS "CS_Canceled", "R"."CODINGSPECTATOR_UNAVAILABLE_COUNT" AS "CS_Unavailable", "R"."INVOKED_BY_QUICKASSIST_COUNT" AS "CS_Quick Assist", "R"."PREVIEW_COUNT" AS "CS_Preview", "R"."AVG_PERFORMED_OR_CANCELED_CONFIGURATION_DURATION_IN_MILLI_SEC" AS "CS_Average Configuration Time", "A"."AVG_AFFECTED_LINES_FLOAT" AS "CT_Average Affected Lines", "A"."AVG_AFFECTED_FILES_FLOAT" AS "CT_Average Affected Files", "R"."P_UNDONE_GIVEN_PERFORMED" AS "P Undo Performed", "R"."P_CANCELED_GIVEN_CANCELED_PERFORMED_NOT_QA" AS "P Canceled Canceled Performed" , "R"."P_PERFORMED_GIVEN_WARNING_OR_ERROR_STATUS" AS "P Performed Warning Error" , "R"."P_UNAVAILABLE_GIVEN_UNAVAILABLE_CANCELED_PERFORMED" AS "P Unavailable", "R"."P_WARNING_GIVEN_PERFORMED_CANCELED" AS "P Warning", "R"."P_ERROR_GIVEN_PERFORMED_CANCELED" AS "P Error", "R"."P_FATAL_GIVEN_PERFORMED_CANCELED" AS "P Fatal Error", "R"."P_UNAVAILABLE_WARNING_ERROR_FATAL_GIVEN_CS_REFACTORINGS" AS "P Unavailable Warning Error Fatal Error", "R"."P_CANCELED_GIVEN_WARNING_ERROR_FATAL" AS "P Canceled Given Warning Error Fatal Error" FROM "PUBLIC"."PER_REFACTORING_ID" "R" LEFT OUTER JOIN (SELECT "PUBLIC"."REFACTORING_CHANGE_SIZE"."REFACTORING_ID" AS "REFACTORING_ID", COUNT("PUBLIC"."REFACTORING_CHANGE_SIZE"."USERNAME") AS "INVOCATION_COUNT", AVG(CONVERT("PUBLIC"."REFACTORING_CHANGE_SIZE"."AFFECTED_FILES_COUNT",SQL_FLOAT)) AS "AVG_AFFECTED_FILES_FLOAT", AVG(CONVERT("PUBLIC"."REFACTORING_CHANGE_SIZE"."AFFECTED_LINES_COUNT",SQL_FLOAT)) AS "AVG_AFFECTED_LINES_FLOAT", SUM("PUBLIC"."REFACTORING_CHANGE_SIZE"."AFFECTED_FILES_COUNT") AS "SUM_AFFECTED_FILES", SUM("PUBLIC"."REFACTORING_CHANGE_SIZE"."AFFECTED_LINES_COUNT") AS "SUM_AFFECTED_LINES" FROM "PUBLIC"."REFACTORING_CHANGE_SIZE" WHERE IS_JAVA_REFACTORING("PUBLIC"."REFACTORING_CHANGE_SIZE"."REFACTORING_ID") GROUP BY "PUBLIC"."REFACTORING_CHANGE_SIZE"."REFACTORING_ID" ORDER BY "PUBLIC"."REFACTORING_CHANGE_SIZE"."REFACTORING_ID") "A" ON "R"."REFACTORING_ID" = "A"."REFACTORING_ID" WHERE "R"."REFACTORING_ID" <> 'org.eclipse.jdt.ui.rename.java.project' AND "R"."REFACTORING_ID" <> 'org.eclipse.jdt.ui.rename.source.folder';

