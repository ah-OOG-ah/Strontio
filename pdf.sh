#!/bin/bash
if [ $# -eq 0 ]; then
  ARGS="$(cd run && echo lab*.csv)"
else
  for arg in "$@"; do
    ARGS="$ARGS$(realpath "$arg") "
  done
fi

echo "Running ./gradlew run -Dorg.gradle.logging.level=quiet --args=$ARGS"
./gradlew run -Dorg.gradle.logging.level=quiet --args="$ARGS"

cd run/out || exit
for tex in ../*.tex; do
  pdflatex -interaction=nonstopmode "$tex" > /dev/null
done
