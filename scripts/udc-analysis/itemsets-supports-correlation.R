#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
# This script computes the Pearson correlation between the supports of the common itemsets of two given CSV files.

suppressPackageStartupMessages(library(optparse))
suppressPackageStartupMessages(library(ggplot2))
suppressPackageStartupMessages(library(Hmisc))

# Parse command line arguments.
optionList <- list(
  make_option(c("-a", "--itemsets_supports_csv1"), type = "character", default = "udc-frequent-refactoring-itemsets-t-10.csv", help = "Name of the first CSV file of itemsets and supports [default %default]", metavar="filename"),
  make_option(c("-b", "--itemsets_supports_csv2"), type = "character", default = "udc-frequent-refactoring-itemsets-t-20.csv", help = "Name of the second CSV file of itemsets and supports [default %default]", metavar="filename")
)
opt <- parse_args(OptionParser(option_list = optionList))

# See # http://cran.r-project.org/doc/manuals/R-data.html#Variations-on-read_002etable # for read.table
itemsets_supports1 <-  read.table(opt$itemsets_supports_csv1, header = TRUE, sep = ",", stringsAsFactors = FALSE)
itemsets_supports2 <- read.table(opt$itemsets_supports_csv2, header = TRUE, sep = ",", stringsAsFactors = FALSE)

# Order the data framews by the itemset column.
itemsets_supports1_ordered_by_itemset <- itemsets_supports1[with(itemsets_supports1, order(itemset)), ]
itemsets_supports2_ordered_by_itemset <- itemsets_supports2[with(itemsets_supports2, order(itemset)), ]

# Compute the common itemsets of the two data frames.
common_itemsets <- intersect(itemsets_supports1_ordered_by_itemset$itemset, itemsets_supports2_ordered_by_itemset$itemset)

# Extract the support values of the common itemsets from the two data frames.
common_itemsets_supports1 <- subset(itemsets_supports1_ordered_by_itemset, itemset %in% common_itemsets)$support
common_itemsets_supports2 <- subset(itemsets_supports2_ordered_by_itemset, itemset %in% common_itemsets)$support

common_itemsets_supports <- data.frame(common_itemsets, common_itemsets_supports1, common_itemsets_supports2)
colnames(common_itemsets_supports) <- c("itemset", "all_support", "expert_support")

p <- ggplot(data = common_itemsets_supports, stat = identity, aes(x = all_support, y = expert_support))

p <- p + geom_point()

# Compute the Pearson correlation of the support levels of the common itemsets of the two data frames.
cor_test_result <- cor.test(x = common_itemsets_supports$all_support, y = common_itemsets_supports$expert_support, alternative = "two.sided", type = "pearson")

cat("Number of itemsets of the first table (S1) =", length(itemsets_supports1$itemset), "\n")
cat("Number of itemsets of the second table (S2) =", length(itemsets_supports2$itemset), "\n")
cat("Number of common itemsets =", length(common_itemsets), "\n")
cat(sprintf("|S2 - S1| = %d\n", length(setdiff(itemsets_supports2$itemset, itemsets_supports1$itemset))))
cat(sprintf("|S1 - S2| = %d\n", length(setdiff(itemsets_supports1$itemset, itemsets_supports2$itemset))))
cat(sprintf("|S1 ^ S2| = %d\n", length(intersect(itemsets_supports1$itemset, itemsets_supports2$itemset))))
cat(sprintf("|S2| / |S1| = %.0f\n", length(itemsets_supports2$itemset) / length(itemsets_supports1$itemset)))
 
show(cor_test_result)

# See http://yatani.jp/HCIstats/Correlation#Report for reporting correlation results.
cat(sprintf("Pearson's r(%d) = %.2f, p < %.2f\n", cor_test_result$parameter, cor_test_result$estimate, cor_test_result$p.value))

