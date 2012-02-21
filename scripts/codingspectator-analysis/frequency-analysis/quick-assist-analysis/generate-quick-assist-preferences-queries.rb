#!/usr/bin/env ruby
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
#author: Nick Chen

template = <<TEMPLATE
SELECT "P"."username" AS USERNAME, COUNT(CASE "P"."invoked-by-quickassist" WHEN 'true' THEN 1 ELSE NULL END) AS "PERFORMED WITH QA", COUNT(CASE "P"."invoked-by-quickassist" WHEN 'true' THEN NULL ELSE 1 END) AS "PERFORMED WITHOUT QA", (COUNT(CASE "P"."invoked-by-quickassist" WHEN 'true' THEN 1 ELSE NULL END) > COUNT(CASE "P"."invoked-by-quickassist" WHEN 'true' THEN NULL ELSE 1 END)) AS "QA Preferred?" FROM "PUBLIC"."ALL_DATA" AS "P" WHERE "P"."id" = '__REPLACE_ME__' AND IS_CODINGSPECTATOR_PERFORMED("P"."recorder", "P"."refactoring kind") AND "P"."username" LIKE 'cs-___' GROUP BY "P"."username" ORDER BY "P"."username";
TEMPLATE

refactorings = ["org.eclipse.jdt.ui.promote.temp",
  "org.eclipse.jdt.ui.extract.constant",
  "org.eclipse.jdt.ui.extract.temp",
  "org.eclipse.jdt.ui.extract.method",
  "org.eclipse.jdt.ui.inline.temp"]

refactorings.each do |id|
  puts template.gsub("__REPLACE_ME__", id)
  puts 
end