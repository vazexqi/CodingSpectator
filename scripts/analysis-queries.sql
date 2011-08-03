CREATE TABLE "PUBLIC"."RefactoringIDs" ("id" varchar(1000));

--Compute the set of all captured refactoring ID's.
INSERT INTO "PUBLIC"."RefactoringIDs" ("id")
SELECT "PUBLIC"."EclipseRefactorings"."id" FROM "PUBLIC"."EclipseRefactorings"
UNION 
SELECT "PUBLIC"."PerformedRefactorings"."id" FROM "PUBLIC"."PerformedRefactorings"
UNION 
SELECT "PUBLIC"."CancelledRefactorings"."id" FROM "PUBLIC"."CancelledRefactorings"
UNION 
SELECT "PUBLIC"."UnavailableRefactorings"."id" FROM "PUBLIC"."UnavailableRefactorings";

--Compute the number of each kind of refactoring invocation per refactoring ID for all users.
SELECT
"PUBLIC"."RefactoringIDs"."id",
"PUBLIC"."EclipseRefactorings"."count" AS "ECLIPSE_COUNT",
"PUBLIC"."PerformedRefactorings"."count" AS "PERFORMED_COUNT",
"PUBLIC"."CancelledRefactorings"."count"  AS "CANCELLED_COUNT",
"PUBLIC"."UnavailableRefactorings"."count" AS "UNAVAILABLE_COUNT" FROM
"PUBLIC"."RefactoringIDs"
FULL OUTER JOIN "PUBLIC"."EclipseRefactorings" ON ("PUBLIC"."RefactoringIDs"."id" = "PUBLIC"."EclipseRefactorings"."id")
FULL OUTER JOIN "PUBLIC"."PerformedRefactorings" ON ("PUBLIC"."RefactoringIDs"."id" = "PUBLIC"."PerformedRefactorings"."id")
FULL OUTER JOIN "PUBLIC"."CancelledRefactorings" ON ("PUBLIC"."RefactoringIDs"."id" = "PUBLIC"."CancelledRefactorings"."id")
FULL OUTER JOIN "PUBLIC"."UnavailableRefactorings" ON ("PUBLIC"."RefactoringIDs"."id" = "PUBLIC"."UnavailableRefactorings"."id");

--Compute the number of each kind of refactoring invocation per user.
SELECT
"PUBLIC"."AllData"."username" AS "USERNAME",
COUNT(CASE "PUBLIC"."AllData"."refactoring kind" WHEN 'ECLIPSE' THEN 1 ELSE NULL END) AS "ECLIPSE_COUNT",
COUNT(CASE "PUBLIC"."AllData"."refactoring kind" WHEN 'PERFORMED' THEN 1 ELSE NULL END) AS "PERFORMED_COUNT",
COUNT(CASE "PUBLIC"."AllData"."refactoring kind" WHEN 'CANCELLED' THEN 1 ELSE NULL END) AS "CANCELLED_COUNT",
COUNT(CASE "PUBLIC"."AllData"."refactoring kind" WHEN 'UNAVAILABLE' THEN 1 ELSE NULL END) AS "UNAVAILABLE_COUNT"
FROM "PUBLIC"."AllData"
GROUP BY "PUBLIC"."AllData"."username" 
ORDER BY "PUBLIC"."AllData"."username"; 

--Compute the number of different kinds of status for every pair of refactoring ID and refactoring invocation kind.
DROP TABLE "PUBLIC"."StatusCounts" IF EXISTS;

CREATE TABLE "PUBLIC"."StatusCounts" ("REFACTORING_ID" VARCHAR(1000), "REFACTORING_KIND" VARCHAR(50), "OK_STATUS_COUNT" INT, "WARNING_STATUS_COUNT" INT, "ERROR_STATUS_COUNT" INT, "FATALERROR_STATUS_COUNT" INT, "OTHER_STATUS_COUNT" INT, "EMPTY_STATUS_COUNT" INT);

INSERT INTO "PUBLIC"."StatusCounts" ("REFACTORING_ID", "REFACTORING_KIND", "OK_STATUS_COUNT", "WARNING_STATUS_COUNT", "ERROR_STATUS_COUNT", "FATALERROR_STATUS_COUNT", "OTHER_STATUS_COUNT", "EMPTY_STATUS_COUNT")
SELECT
"PUBLIC"."AllData"."id" AS "REFACTORING_ID",
"PUBLIC"."AllData"."refactoring kind" AS "REFACTORING_KIND",
COUNT(CASE "PUBLIC"."AllData"."status" WHEN LIKE '_OK%' THEN 1 ELSE NULL END) AS "OK_STATUS_COUNT",
COUNT(CASE "PUBLIC"."AllData"."status" WHEN LIKE '_WARNING%' THEN 1 ELSE NULL END) AS "WARNING_STATUS_COUNT",
COUNT(CASE "PUBLIC"."AllData"."status" WHEN LIKE '_ERROR%' THEN 1 ELSE NULL END) AS "ERROR_STATUS_COUNT",
COUNT(CASE "PUBLIC"."AllData"."status" WHEN LIKE '_FATALERROR%' THEN 1 ELSE NULL END) AS "FATALERROR_STATUS_COUNT",
COUNT(CASE "PUBLIC"."AllData"."status"
  WHEN LIKE '_OK%' THEN NULL
  WHEN LIKE '_WARNING%' THEN NULL
  WHEN LIKE '_ERROR%' THEN NULL
  WHEN LIKE '_FATALERROR%' THEN NULL
  WHEN LIKE '' THEN NULL
  ELSE 1 END) AS "OTHER_STATUS_COUNT",
COUNT(CASE "PUBLIC"."AllData"."status" WHEN '' THEN 1 ELSE NULL END) AS "EMPTY_STATUS_COUNT"
FROM "PUBLIC"."AllData"
WHERE NOT "PUBLIC"."AllData"."id" = ''
GROUP BY "PUBLIC"."AllData"."id", "PUBLIC"."AllData"."refactoring kind" 
ORDER BY "PUBLIC"."AllData"."id", "PUBLIC"."AllData"."refactoring kind";

* *DSV_TARGET_FILE=StatusCountsPerRefactoring.csv

\x "PUBLIC"."StatusCounts"

