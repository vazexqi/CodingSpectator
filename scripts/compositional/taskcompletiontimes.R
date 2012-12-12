#!/usr/bin/env Rscript
# This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

# See http://yatani.jp/HCIstats/WilcoxonSigned and
# http://www.bmj.com/about-bmj/resources-readers/publications/statistics-square-one/13-study-design-and-choosing-statisti

suppressPackageStartupMessages(library("coin"))
wizard <- c(17, 8, 18, 19, 37, 19, 9, 12, 16, 13)
compositional <- c(9, 5, 8, 19, 11, 18, 8, 11, 18, 10)
median(wizard)
median(compositional)
wilcoxsign_test(wizard ~ compositional, distribution = "exact", alternative = "greater")
