#!/usr/bin/env bash

# Simulation parameters
simulation_time_seconds=15
output_directory=./output
seed=1743645648280
event_density=10

gradle clean build

gradle run --no-build-cache --rerun-tasks --args="\
  --simulation-time=${simulation_time_seconds} \
  --seed=${seed} \
  --output-directory=${output_directory} \
  --event-density=${event_density} \
"
