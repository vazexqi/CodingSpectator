#!/usr/bin/Rscript --vanilla
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

logcon <- file("R.log", open = "wt")
sink(file = logcon, type = "output")
sink(file = logcon, type = "message")
library(package = "arules")
transactions = read.transactions(file = file("stdin"), format = "basket", sep = ",")
#frequencies = itemFrequency(transactions)
#cat(frequencies)
#WRITE(frequencies, file = "tx.csv", sep = ",")
rules = apriori(transactions, parameter = list(minlen=1, sup = 0.001, conf = 0.001))
summary(rules)
inspect(rules)
sink(file = NULL, type = "output")
sink(file = NULL, type = "message")
close(logcon)
WRITE(rules, file = "", sep = ",", quote = TRUE, col.names = NA)

