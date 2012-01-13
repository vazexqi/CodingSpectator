#!/usr/bin/Rscript --vanilla
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

logcon <- file("R.log", open = "wt")
sink(file = logcon, type = "output")
sink(file = logcon, type = "message")
transactions <- read.table(file = file("stdin"), header = FALSE, stringsAsFactors = FALSE) 

# Sort the items of each transaction lexicographically (See http://stackoverflow.com/q/8844490/130224).
transactions[, 1] <- sapply(lapply(strsplit(transactions[, 1], ","), sort), paste, collapse=",")

T <- table(transactions)
T <- cbind(T, T/nrow(transactions))
colnames(T) <- c("frequency", "proportion") 
TDataFrame <- data.frame(T)
TDataFrame <- TDataFrame[TDataFrame$proportion >= 0.001,1:2]
TDataFrame <- TDataFrame[order(TDataFrame[,2], decreasing=TRUE),]
sink(file = NULL, type = "output")
sink(file = NULL, type = "message")
close(logcon)
write.csv(TDataFrame, file = "", quote = TRUE)

