#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

codingspectator_svn_folder <- Sys.getenv("CODINGSPECTATOR_SVN_FOLDER")
udc_distributions_folder <- paste(codingspectator_svn_folder, "Experiment", "UDCData", "TimestampedUDCData", "Distributions", sep = "/")

users_distinct_refactorings_csv <- paste(udc_distributions_folder, "users-distinct-refactorings.csv", sep = "/")
table <- read.table(file = users_distinct_refactorings_csv, header = TRUE, sep = ",")
X <- table$DISTINCT_REFACTORINGS
Y <- table$USERS
refactoring_users_count <- sum(table$USERS)

users_distinct_refactorings_png <- paste(udc_distributions_folder, "users-distinct-refactorings.png", sep = "/")
png(filename = users_distinct_refactorings_png)
plot(x = X, y = Y, type = "b", log = "y", xlab = "Number of Distinct Refactorings", ylab = "Number of Programmers (log-scaled)", main = "Distribution of Distinct Refactorings", sub = sprintf("Total number of refactoring users = %d", refactoring_users_count), xaxt = "n", yaxt = "n")
axis(1, at = X, labels = X)
axis(2, at = 10^(0:log10(max(Y))), labels = 10^(0:log10(max(Y))))
abline(v = X, col = 'grey', lwd = 0.5)
abline(h = Y, col = 'grey', lwd = 0.5)
dev.off()

users_distinct_refactorings_cumulative_png <- paste(udc_distributions_folder, "users-distinct-refactorings-cumulative.png", sep = "/")
png(filename = users_distinct_refactorings_cumulative_png)
Z <- cumsum(table$USERS) / sum(table$USERS) * 100
plot(x = X, y = Z, type = "b", xlab = "Maximum Number of Distinct Refactorings", ylab = "Percentage of Programmers", main = "Cumulative Distribution of Distinct Refactorings", sub = sprintf("Total number of refactoring users = %d", refactoring_users_count), xaxt = "n", yaxt = "n")
axis(1, at = X, labels = X)
axis(2, at = Z[c(1:5, 23)], labels = sprintf("%.1f", Z[c(1:5, 23)]), las = 1)
abline(v = X, col = 'grey', lwd = 0.5)
abline(h = Z[c(1:5, 23)], col = 'grey', lwd = 0.5)
dev.off()

refactoring_frequencies_csv <- paste(udc_distributions_folder, "refactoring-frequencies.csv", sep = "/")
table <- read.table(file = refactoring_frequencies_csv, header = TRUE, sep = ",", stringsAsFactors = FALSE)
R <- table$REFACTORING[-1]
F <- table$FREQUENCY[-1]
U <- table$USERS[-1]
refactoring_frequencies_png <- paste(udc_distributions_folder, "refactoring-frequencies.png", sep = "/")
library(Hmisc)
png(filename = refactoring_frequencies_png)
op <- par(mar = c(16, 16, 4, 2) + 0.1, mgp = c(13, 1, 0))
#op <- par(oma = c(16, 4, 4, 2) + 0.1)
midpoints <- barplot(height = F, width = 2, space = 0.2, names.arg = R, log = "y", xlim = c(1, 2.5 * length(R)), xlab = "Refactoring", ylab = "Frequency (log-scaled)", main = "Frequency of Refactorings", sub = sprintf("Total number of initiated refactorings = %d", sum(F)), xaxt = "n", yaxt = "n")
axis(1, at = midpoints, labels = R, las = 2)
#mgp.axis(1, at = midpoints, labels = R, las = 2, mgp = c(13, 1, 0))
axis(2, at = 10^(0:log10(max(F))), labels = sprintf("%.0f", 10^(0:log10(max(F)))), las = 2)
#mgp.axis(2, at = 10^(0:log10(max(F))), labels = sprintf("%.0f", 10^(0:log10(max(F)))), las = 2)
dev.off()
#par(op)

