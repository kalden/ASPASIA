#!/bin/bash

# SETTINGS FILE LOCATION TAKEN FROM ARGUMENTS
# FIRST ARG SHOULD BE THE ANALYSIS BEING RUN (-rle) AND THE SECOND THE PATH TO THE SETTINGS XML FILE
SETTINGS=$2

# Potential flags r for robustness, l for latin-hypercube, e for efast, s for SBML Intervention

while getopts ":rles" opt; do
  case $opt in
    r)
	java -jar ASPASIA.jar "r" $SETTINGS
      ;;
    l)
	java -jar ASPASIA.jar "l" $SETTINGS
      ;;
    e)
	java -jar ASPASIA.jar "e" $SETTINGS
      ;;
    s)
	java -jar ASPASIA.jar "s" $SETTINGS
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac


done
