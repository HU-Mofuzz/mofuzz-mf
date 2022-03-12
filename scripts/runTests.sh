#!/usr/bin/env bash

if [ $# -ne 2 ]; then
  echo "Needs exactly 2 arguments: JQF Driver Path and Jar Path"
  exit 1
fi

DRIVER_PATH="$1"
JAR_PATH="$2"

PLOT_DATA_SAVE_DIR="./plot_data"
LOGFILE="./executor.log"
EXEC_DIR="$(date +"%d-%m-%Y_%H-%M-%S")"

TEST_METHOD=( 'svgSalamanderTest' 'testBatik' 'testBatikTranscoder' )
MODEL_WIDTHS=( 1 5 10 )
MODEL_DEPTHS=( 1 5 10 )
INITIAL_FILES=( 10 100 1000 )
DURATION=30 # minutes

CURRENT_METHOD=""
CURRENT_WIDTH=""
CURRENT_DEPTH=""
CURRENT_INIT=""

function executeTest() {
    BASEDIR="./${CURRENT_METHOD}/w${CURRENT_WIDTH}_d${CURRENT_DEPTH}_i${CURRENT_INIT}"
    FAIL_DIR="$BASEDIR/fail"
    WORKING_DIR="$BASEDIR/work"
    TEST_DIR="$BASEDIR/test"

    mkdir -p "$BASEDIR" "$FAIL_DIR" "$WORKING_DIR" "$TEST_DIR"

    echo "Executing $CURRENT_METHOD with width=$CURRENT_WIDTH depth=$CURRENT_DEPTH initFiles=$CURRENT_INIT"
    /usr/bin/env bash -c "$DRIVER_PATH $JAR_PATH --failDir $FAIL_DIR --workingDir $WORKING_DIR --testDir $TEST_DIR --initialFiles $CURRENT_INIT --modelDepth $CURRENT_DEPTH --modelWidth $CURRENT_WIDTH --duration $DURATION $CURRENT_METHOD | tee $LOGFILE"
}

function savePlotData() {
  if [ ! -d "$PLOT_DATA_SAVE_DIR" ]; then
    mkdir -p "$PLOT_DATA_SAVE_DIR"
  fi
  cp "$1" "$PLOT_DATA_SAVE_DIR/${CURRENT_METHOD}_w${CURRENT_WIDTH}_d${CURRENT_DEPTH}_i${CURRENT_INIT}.csv"
}

function iterateInitFiles() {
  for initSize in "${INITIAL_FILES[@]}"; do
    CURRENT_INIT="$initSize"
    # execute test
    executeTest
    # copy plot data
    savePlotData "$TEST_DIR/plot_data"
  done
}

function iterateModelDepth() {
    for depth in "${MODEL_DEPTHS[@]}"; do
      CURRENT_DEPTH="$depth"
      iterateInitFiles
    done
}

function iterateModelWidths() {
    for width in "${MODEL_WIDTHS[@]}"; do
      CURRENT_WIDTH="$width"
      iterateModelDepth
    done
}

mkdir -p "$EXEC_DIR" || return 1
cd "$EXEC_DIR" || return 1

for method in "${TEST_METHOD[@]}"; do
  CURRENT_METHOD="$method"
  iterateModelWidths
done