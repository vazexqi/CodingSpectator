#!/usr/bin/Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
library("arules")
transactions = read.transactions(file = "udc-refactoring-transactions.txt", format = "basket", sep = ",")
frequencies = itemFrequency(transactions)
cat(frequencies)
#WRITE(frequencies, file = "tx.csv", sep = ",")
rules = apriori(transactions, parameter = list(minlen=2, sup = 0.001, conf = 0.001))
summary(rules)
inspect(rules)
WRITE(rules, file = "refactoring-association-rules.csv", sep = ",", quote = TRUE, col.names = NA)

