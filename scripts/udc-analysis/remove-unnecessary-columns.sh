#!/bin/bash
#This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

#Project the input CSV file into its first, sixth, and seventh columns.

awk -F"," '{ print $1 "," $6 "," $7 }' $1

