CREATE TABLE "PUBLIC"."RefactoringIDs" ("id" varchar(1000));

INSERT INTO "PUBLIC"."RefactoringIDs" ("id")
SELECT "PUBLIC"."EclipseRefactorings"."id" FROM "PUBLIC"."EclipseRefactorings"
UNION 
SELECT "PUBLIC"."PerformedRefactorings"."id" FROM "PUBLIC"."PerformedRefactorings"
UNION 
SELECT "PUBLIC"."CancelledRefactorings"."id" FROM "PUBLIC"."CancelledRefactorings"
UNION 
SELECT "PUBLIC"."UnavailableRefactorings"."id" FROM "PUBLIC"."UnavailableRefactorings";

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

