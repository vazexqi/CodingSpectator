#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

suppressPackageStartupMessages(library("optparse"))
optionList <- list(
  make_option(c("-r", "--rules"), type = "character", default = "rules.csv", help = "Name of the output file containing the inferred association rules [default %default]", metavar="filename"),
  make_option(c("-i", "--itemsets"), type = "character", default = "itemsets.csv", help = "Name of the output file containing the frequent itemsets [default %default]", metavar="filename")
)
opt <- parse_args(OptionParser(option_list = optionList))
logcon <- file("R.log", open = "wt")
sink(file = logcon, type = "output")
sink(file = logcon, type = "message")
library(package = "arules")
transactions = read.transactions(file = file("stdin"), format = "basket", sep = ",")
rules = apriori(transactions, parameter = list(minlen=1, sup = 0.001, conf = 0.001))
itemsets <- unique(generatingItemsets(rules))
itemsets.df <- as(itemsets, "data.frame")
frequentItemsets <- itemsets.df[with(itemsets.df, order(-support,items)),]
names(frequentItemsets)[1] <- "itemset"
sink(file = NULL, type = "output")
sink(file = NULL, type = "message")
close(logcon)
WRITE(rules, file = opt$rules, sep = ",", quote = TRUE, col.names = NA)
write.table(frequentItemsets, file = opt$itemsets, sep = ",", row.names = FALSE)

