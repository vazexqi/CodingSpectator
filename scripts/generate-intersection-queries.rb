#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Nick Chen

# This file generate the RAW SQL queries for the file intersection-queries.sql.
# The only reason we need this file is because hsqldb has problems capturing
# proper closured for correlated subqueries.

template = <<TEMPLATE
INSERT INTO "PUBLIC"."UNDONE_CODINGSPECTATOR_COUNT" (
  "REFACTORING_ID",
  "UNDONE_COUNT"
) SELECT "T"."id", (SELECT COUNT(*) FROM (SELECT "CS"."REFACTORING_ID" AS CS_REFACTORING_ID, "CS"."REFACTORING_USERNAME" AS CS_REFACTORING_USERNAME, "CS"."REFACTORING_TIMESTAMP" AS CS_REFACTORING_TIMESTAMP, "CT"."REFACTORING_TIMESTAMP" AS CT_REFACTORING_TIMESTAMP, "CS"."REFACTORING_WORKSPACE" AS CS_REFACTORING_WORKSPACE FROM (SELECT "CT_UNDONE"."id" AS REFACTORING_ID, "CT_UNDONE"."timestamp" AS REFACTORING_TIMESTAMP, "CT_UNDONE"."username" AS REFACTORING_USERNAME, "CT_UNDONE"."workspace ID" REFACTORING_WORKSPACE FROM "PUBLIC"."ALL_DATA" AS "CT_UNDONE" WHERE "CT_UNDONE"."id" = '__REPLACE_ME__' AND "CT_UNDONE"."username" LIKE 'cs-___' AND IS_CODINGTRACKER_UNDONE("CT_UNDONE"."recorder", "CT_UNDONE"."refactoring kind") ORDER BY "CT_UNDONE"."timestamp") AS "CT" INNER JOIN (SELECT "CS_PERFORMED"."id" AS REFACTORING_ID, "CS_PERFORMED"."timestamp" AS REFACTORING_TIMESTAMP, "CS_PERFORMED"."username" AS REFACTORING_USERNAME, "CS_PERFORMED"."workspace ID" REFACTORING_WORKSPACE FROM "PUBLIC"."ALL_DATA" AS "CS_PERFORMED" WHERE "CS_PERFORMED"."id" = '__REPLACE_ME__' AND "CS_PERFORMED"."username" LIKE 'cs-___' AND IS_CODINGSPECTATOR_PERFORMED("CS_PERFORMED"."recorder", "CS_PERFORMED"."refactoring kind") ORDER BY "CS_PERFORMED"."timestamp") AS "CS" ON "CT"."REFACTORING_USERNAME" = "CS"."REFACTORING_USERNAME" AND "CT"."REFACTORING_WORKSPACE" = "CS"."REFACTORING_WORKSPACE" AND ABS("CT"."REFACTORING_TIMESTAMP" - "CS"."REFACTORING_TIMESTAMP") < 1000)) FROM (SELECT DISTINCT "PUBLIC"."ALL_DATA"."id" as "id"
FROM "PUBLIC"."ALL_DATA" WHERE IS_JAVA_REFACTORING("PUBLIC"."ALL_DATA"."id") AND "PUBLIC"."ALL_DATA"."username" LIKE 'cs-___') as "T" WHERE "T"."id" = '__REPLACE_ME__';
TEMPLATE

refactorings = ["org.eclipse.jdt.ui.change.method.signature",
"org.eclipse.jdt.ui.convert.anonymous",
"org.eclipse.jdt.ui.extract.class",
"org.eclipse.jdt.ui.extract.constant",
"org.eclipse.jdt.ui.extract.interface",
"org.eclipse.jdt.ui.extract.method",
"org.eclipse.jdt.ui.extract.superclass",
"org.eclipse.jdt.ui.extract.temp",
"org.eclipse.jdt.ui.infer.typearguments",
"org.eclipse.jdt.ui.inline",
"org.eclipse.jdt.ui.inline.constant",
"org.eclipse.jdt.ui.inline.method",
"org.eclipse.jdt.ui.inline.temp",
"org.eclipse.jdt.ui.introduce.factory",
"org.eclipse.jdt.ui.introduce.indirection",
"org.eclipse.jdt.ui.introduce.parameter",
"org.eclipse.jdt.ui.introduce.parameter.object",
"org.eclipse.jdt.ui.move",
"org.eclipse.jdt.ui.move.inner",
"org.eclipse.jdt.ui.move.method",
"org.eclipse.jdt.ui.move.static",
"org.eclipse.jdt.ui.promote.temp",
"org.eclipse.jdt.ui.pull.up",
"org.eclipse.jdt.ui.push.down",
"org.eclipse.jdt.ui.rename.compilationunit",
"org.eclipse.jdt.ui.rename.enum.constant",
"org.eclipse.jdt.ui.rename.field",
"org.eclipse.jdt.ui.rename.java.project",
"org.eclipse.jdt.ui.rename.local.variable",
"org.eclipse.jdt.ui.rename.method",
"org.eclipse.jdt.ui.rename.package",
"org.eclipse.jdt.ui.rename.source.folder",
"org.eclipse.jdt.ui.rename.type",
"org.eclipse.jdt.ui.rename.type.parameter",
"org.eclipse.jdt.ui.rename.unknown.java.element",
"org.eclipse.jdt.ui.self.encapsulate",
"org.eclipse.jdt.ui.use.supertype"]

refactorings.each do |id|
	puts template.gsub("__REPLACE_ME__", id)
	puts 
end