#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

library(ggplot2)

codingspectator_svn_folder <- Sys.getenv("CODINGSPECTATOR_SVN_FOLDER")
udc_distributions_folder <- paste(codingspectator_svn_folder, "Experiment", "UDCData", "TimestampedUDCData", "Distributions", sep = "/")

csv_file_name <- paste(udc_distributions_folder, "users-distinct-refactorings.csv", sep = "/")
table <- read.table(file = csv_file_name, header = TRUE, sep = ",")
number_of_distinct_refactorings <- table$DISTINCT_REFACTORINGS
users <- table$USERS
total_number_of_refactoring_users <- sum(table$USERS)

png_file_name <- paste(udc_distributions_folder, "distribution-of-distinct-refactorings.png", sep = "/")
png(filename = png_file_name, width = 1400, height = 1400, res = 100)
data <- data.frame(number_of_distinct_refactorings, users)
ggplot(data, stat = "identity", aes(x = number_of_distinct_refactorings, y = users)) +
geom_point() +
scale_x_continuous(name = "Number of Distinct Refactorings", breaks = number_of_distinct_refactorings) +
scale_y_log10("Number of Programmers (log-scaled)") +
geom_text(aes(vjust = -1, label = sprintf("%.4f%%", users / total_number_of_refactoring_users * 100)), size = 3) +
opts(title = "Distribution of Distinct Refactorings")
dev.off()

png_file_name <- paste(udc_distributions_folder, "cumulative-distribution-of-distinct-refactorings.png", sep = "/")
png(filename = png_file_name, width = 600, height = 600, res = 100)
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

csv_file_name <- paste(udc_distributions_folder, "refactoring-frequencies.csv", sep = "/")
table <- read.table(file = csv_file_name, header = TRUE, sep = ",", stringsAsFactors = FALSE)
refactorings <- table$REFACTORING[-1]
frequency <- table$FREQUENCY[-1]
sum_frequency <- sum(frequency)
cumulative_percentage_frequency <- cumsum(frequency) / sum_frequency * 100

png_file_name <- paste(udc_distributions_folder, "frequencies-of-refactorings.png", sep = "/")
png(filename = png_file_name, width = 1000, height = 1000, res = 100)
data <- data.frame(refactorings, frequency)
ggplot(data, stat = "identity", aes(x = reorder(x = refactorings, X = frequency, FUN = sum), y = frequency)) +
geom_point() +
coord_flip() +
scale_x_discrete("Refactoring") +
scale_y_log10("Frequency (log-scaled)") +
geom_text(aes(y = 1.5 * frequency, label = sprintf("%.2f%%", frequency / sum_frequency * 100)), size = 3) +
opts(title = "Frequencies of Refactorings")
dev.off()

png_file_name <- paste(udc_distributions_folder, "cumulative-distribution-of-the-frequencies-of-refactorings.png", sep = "/")
png(filename = png_file_name, width = 1000, height = 1000, res = 100)
data <- data.frame(refactorings, cumulative_percentage_frequency)
ggplot(data, stat = "identity", aes(x = reorder(x = refactorings, X = cumulative_percentage_frequency, FUN = sum), y = cumulative_percentage_frequency)) +
geom_point() +
coord_flip() +
scale_x_discrete("Refactoring") +
scale_y_continuous("Frequency (%)") +
geom_text(aes(label = sprintf("%.2f%%", cumulative_percentage_frequency), vjust = -1), size = 3) +
opts(title = "Cumulative Distribution of the Frequencies of Refactorings")
dev.off()

csv_file_name <- paste(udc_distributions_folder, "user-refactoring-frequencies.csv", sep = "/")
table <- read.table(file = csv_file_name, header = TRUE, sep = ",", stringsAsFactors = FALSE)
refactorings <- table$ALL_REFACTORINGS

png_file_name <- paste(udc_distributions_folder, "users-all-refactorings.png", sep = "/")
png(filename = png_file_name, width = 1000, height = 1000, res = 100)
data <- data.frame(refactorings)
binwidth <- 50
breaks <- seq(0 - binwidth / 2, max(refactorings) + binwidth / 2, by = binwidth)
ticks <- breaks + binwidth / 2
ggplot(data, stat = "identity", aes(x = refactorings)) +
stat_bin(aes(y = ..count..), breaks = breaks, geom = "point", position = "identity") +
scale_x_continuous("Number of Refactorings", breaks = ticks) +
scale_y_log10("Number of Users (log-scaled)") +
opts(title = sprintf("Distribution of the Number of Users of Each Number of Refactorings\nBin Width = %d", binwidth))
dev.off()

