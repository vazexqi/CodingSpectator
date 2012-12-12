#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

# See http://yatani.jp/HCIstats/WilcoxonSigned and
# http://www.bmj.com/about-bmj/resources-readers/publications/statistics-square-one/13-study-design-and-choosing-statisti

suppressPackageStartupMessages(library("coin"))

wizard <- c(17, 8, 18, 19, 37, 19, 9, 12, 16, 13)
compositional <- c(9, 5, 8, 19, 11, 18, 8, 11, 18, 10)

cat("wizard = ", wizard, "\n")
cat("length(wizard) = ", length(wizard), "\n")
cat("median(wizard) = ", median(wizard), "\n")
cat("\n")
cat("compositional = ", compositional, "\n")
cat("length(compositional) = ", length(compositional), "\n")
cat("median(compositional) = ", median(compositional), "\n")

wilcox_result <- wilcox.test(wizard, compositional, paired = TRUE, alternative = "two.sided")
wilcox_result
W <- wilcox_result$statistic

wilcoxsign_result <- wilcoxsign_test(wizard ~ compositional, distribution = "exact", alternative = "two.sided")
wilcoxsign_result
Z <- wilcoxsign_result@statistic@teststatistic
p <- pvalue(wilcoxsign_result)
r <- abs(Z) / sqrt(length(wizard) + length(compositional))
cat("W = ", W, "\n")
cat("Z = ", Z, "\n")
cat("p-value = ", p, "\n")
cat("Effect size (r) = ", r, "\n")
cat(sprintf("(W = %.2f, Z = %.2f, p = %.2f < 0.05, r = %.2f)", W, Z, p, r), "\n")
