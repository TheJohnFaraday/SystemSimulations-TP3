#!/usr/bin/env bash

# Simulation parameters
simulation_time_seconds=15
output_directory=./output
seed=1743645648280

gradle clean build

gradle run --no-build-cache --rerun-tasks --args="\
  --simulation-time=${simulation_time_seconds} \
  --seed=${seed} \
  --output-directory=${output_directory} \
"
