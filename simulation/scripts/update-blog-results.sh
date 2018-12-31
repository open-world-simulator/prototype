#!/usr/bin/env bash
set -e

BLOG_RESULTS_DIR="../../docs/sim-results"
SIM_RESULTS_DIR="../output-sim"

echo "Updating blog results with latest simulation"
echo "Deleting $BLOG_RESULTS_DIR"

rm -rf "$BLOG_RESULTS_DIR"
mkdir "$BLOG_RESULTS_DIR"

echo "Copying results from $BLOG_RESULTS_DIR to $SIM_RESULTS_DIR"
cp -R $SIM_RESULTS_DIR/Narnia-0 "$BLOG_RESULTS_DIR"
cp -R $SIM_RESULTS_DIR/Narnia-1 "$BLOG_RESULTS_DIR"
cp -R $SIM_RESULTS_DIR/Narnia-2 "$BLOG_RESULTS_DIR"

