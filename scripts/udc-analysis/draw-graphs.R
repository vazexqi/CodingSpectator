#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

library(ggplot2)

codingspectator_svn_folder <- Sys.getenv("CODINGSPECTATOR_SVN_FOLDER")
udc_distributions_folder <- paste(codingspectator_svn_folder, "Experiment", "UDCData", "TimestampedUDCData", "Distributions", sep = "/")

users_distinct_refactorings_csv <- paste(udc_distributions_folder, "users-distinct-refactorings.csv", sep = "/")
table <- read.table(file = users_distinct_refactorings_csv, header = TRUE, sep = ",")
number_of_distinct_refactorings <- table$DISTINCT_REFACTORINGS
users <- table$USERS
total_number_of_refactoring_users <- sum(table$USERS)

users_distinct_refactorings_png <- paste(udc_distributions_folder, "distribution-of-distinct-refactorings.png", sep = "/")
png(filename = users_distinct_refactorings_png, width = 1400, height = 1400, res = 100)
data <- data.frame(number_of_distinct_refactorings, users)
ggplot(data, stat = "identity", aes(x = number_of_distinct_refactorings, y = users)) +
geom_point() +
scale_x_continuous(name = "Number of Distinct Refactorings", breaks = number_of_distinct_refactorings) +
scale_y_log10("Number of Programmers (log-scaled)") +
geom_text(aes(vjust = -1, label = sprintf("%.4f%%", users / total_number_of_refactoring_users * 100)), size = 3) +
opts(title = "Distribution of Distinct Refactorings")
dev.off()

users_distinct_refactorings_cumulative_png <- paste(udc_distributions_folder, "cumulative-distribution-of-distinct-refactorings.png", sep = "/")
png(filename = users_distinct_refactorings_cumulative_png, width = 600, height = 600, res = 100)
cumulative_percentage_users <- cumsum(table$USERS) / total_number_of_refactoring_users * 100
data <- data.frame(number_of_distinct_refactorings, cumulative_percentage_users)
ggplot(data, stat = "identity", aes(x = number_of_distinct_refactorings, y = cumulative_percentage_users)) +
geom_point() +
scale_x_continuous(name = "Maximum Number of Distinct Refactorings", breaks = number_of_distinct_refactorings) +
scale_y_continuous("Programmers (%)") +
geom_path() +
geom_text(aes(x = number_of_distinct_refactorings[1:5], y = cumulative_percentage_users[1:5], hjust = -0.2, vjust = 1, label = sprintf("%.1f%%", cumulative_percentage_users[1:5])), size = 3) +
opts(title = "Cumulative Distribution of Distinct Refactorings")
dev.off()

refactoring_frequencies_csv <- paste(udc_distributions_folder, "refactoring-frequencies.csv", sep = "/")
table <- read.table(file = refactoring_frequencies_csv, header = TRUE, sep = ",", stringsAsFactors = FALSE)
refactorings <- table$REFACTORING[-1]
frequency <- table$FREQUENCY[-1]
sum_frequency <- sum(frequency)
cumulative_percentage_frequency <- cumsum(frequency) / sum_frequency * 100

refactoring_frequencies_png <- paste(udc_distributions_folder, "frequencies-of-refactorings.png", sep = "/")
png(filename = refactoring_frequencies_png, width = 1000, height = 1000, res = 100)
data <- data.frame(refactorings, frequency)
ggplot(data, stat = "identity", aes(x = reorder(x = refactorings, X = frequency, FUN = sum), y = frequency)) +
geom_point() +
coord_flip() +
scale_x_discrete("Refactoring") +
scale_y_log10("Frequency (log-scaled)") +
geom_text(aes(y = 1.5 * frequency, label = sprintf("%.2f%%", frequency / sum_frequency * 100)), size = 3) +
opts(title = "Frequencies of Refactorings")
dev.off()

refactoring_frequencies_png <- paste(udc_distributions_folder, "cumulative-distribution-of-the-frequencies-of-refactorings.png", sep = "/")
png(filename = refactoring_frequencies_png, width = 1000, height = 1000, res = 100)
data <- data.frame(refactorings, cumulative_percentage_frequency)
ggplot(data, stat = "identity", aes(x = reorder(x = refactorings, X = cumulative_percentage_frequency, FUN = sum), y = cumulative_percentage_frequency)) +
geom_point() +
coord_flip() +
scale_x_discrete("Refactoring") +
scale_y_continuous("Frequency (%)") +
geom_text(aes(label = sprintf("%.2f%%", cumulative_percentage_frequency), vjust = -1), size = 3) +
opts(title = "Cumulative Distribution of the Frequencies of Refactorings")
dev.off()

