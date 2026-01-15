#!/bin/bash

./gradlew run --args="$(cd run && echo *.csv)"

cd run/out || exit
for tex in *.tex; do
  pdflatex -interaction=nonstopmode "$tex" > /dev/null
done
