#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

# See http://yatani.jp/HCIstats/WilcoxonSigned and
# http://www.bmj.com/about-bmj/resources-readers/publications/statistics-square-one/13-study-design-and-choosing-statisti

suppressPackageStartupMessages(library("coin"))

run_wilcoxon <- function(a, b) {
  cat("a = ", a, "\n")
  cat("length(a) = ", length(a), "\n")
  cat("median(a) = ", median(a), "\n")
  cat("\n")
  cat("b = ", b, "\n")
  cat("length(b) = ", length(b), "\n")
  cat("median(b) = ", median(b), "\n")
  cat("\n")

  wilcox_result <- wilcox.test(a, b, paired = TRUE, alternative = "two.sided")
  wilcox_result
  W <- wilcox_result$statistic
 
  wilcoxsign_result <- wilcoxsign_test(a ~ b, distribution = "exact", alternative = "two.sided")
  wilcoxsign_result
  Z <- wilcoxsign_result@statistic@teststatistic
  p <- pvalue(wilcoxsign_result)
  r <- abs(Z) / sqrt(length(a) + length(b))

  cat("W = ", W, "\n")
  cat("Z = ", Z, "\n")
  cat("p-value = ", p, "\n")
  cat("Effect size (r) = ", r, "\n")
  cat("\n")
  cat(sprintf("(W = %.2f, Z = %.2f, p = %.2f < 0.05, r = %.2f)", W, Z, p, r), "\n")
}


cat("Comparison of task completion times:\n")
wizard_task_completion_times <- c(17, 8, 18, 19, 37, 19, 9, 12, 16, 13)
compositional_task_completion_times <- c(9, 5, 8, 19, 11, 18, 8, 11, 18, 10)
run_wilcoxon(wizard_task_completion_times, compositional_task_completion_times)
