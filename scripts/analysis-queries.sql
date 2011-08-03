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

