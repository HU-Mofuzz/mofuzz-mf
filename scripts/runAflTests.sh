#!/usr/bin/env bash

# This script is meant to be useful for repeated test execution, features are:
#  - Self managed creation of separate execution dirs on each script start
#  - Structured execution of tests with different variables
#  - Own logfile generation and management
#  - Structured copies of AFL plot data in own directory
#
# The script logic is quite simple:
# Iterate different methods to test
#   Inside that iterate different model widths
#     Inside that iterate different model depths
#       Inside that iterate different amounts of random start files
#         Execute given test method with combination of variables
#         Save plot data in structured way

# argument checking
if [ $# -ne 3 ]; then
  echo "Needs exactly 3 arguments: AFL Driver Path and Jar Path and AFL Dir"
  exit 1
fi

# argument renaming
DRIVER_PATH="$1"
JAR_PATH="$2"
export AFL_DIR="$3"

# file constants
PLOT_DATA_SAVE_DIR="./plot_data"
LOGFILE="jqf.log"
EXEC_DIR="$(date +"%Y-%m-%d_%H-%M-%S")"

# execution related constants
TEST_METHOD=( 'svgSalamanderTest' 'testBatikTranscoder' )
MODEL_WIDTHS=( 1 5 10 )
MODEL_DEPTHS=( 1 5 10 )
INITIAL_FILES=( 10 100 1000 )
DURATION=35 # minutes

# execution variables
CURRENT_METHOD=""
CURRENT_WIDTH=""
CURRENT_DEPTH=""
CURRENT_INIT=""

function log() {
  echo "$1" | tee -a "$LOGFILE"
}

# method declaration
function savePlotData() {
  # if plot data directory does not exist
  if [ ! -d "$PLOT_DATA_SAVE_DIR" ]; then
    mkdir -p "$PLOT_DATA_SAVE_DIR"
  fi
  # copy passed executions plot data and rename it meaningful
  cp "$1" "$PLOT_DATA_SAVE_DIR/${CURRENT_METHOD}_w${CURRENT_WIDTH}_d${CURRENT_DEPTH}_i${CURRENT_INIT}.csv"
}

function executeTest() {
    # generate dir names
    BASEDIR="./${CURRENT_METHOD}/w${CURRENT_WIDTH}_d${CURRENT_DEPTH}_i${CURRENT_INIT}"
    FAIL_DIR="$BASEDIR/fail"
    WORKING_DIR="$BASEDIR/work"
    TEST_DIR="$BASEDIR/test"

    # create execution dirs
    mkdir -p "$BASEDIR" "$FAIL_DIR" "$WORKING_DIR" "$TEST_DIR"

    #core execution
    echo ""
    echo "===== Executing $CURRENT_METHOD with width=$CURRENT_WIDTH depth=$CURRENT_DEPTH initFiles=$CURRENT_INIT ====="
    jvmArgs=(--failDir "$FAIL_DIR" --workingDir "$WORKING_DIR" --testDir "$TEST_DIR" --initialFiles "$CURRENT_INIT" --modelDepth "$CURRENT_DEPTH" --modelWidth "$CURRENT_WIDTH" --duration "$DURATION" "$CURRENT_METHOD")
    echo "${jvmArgs[*]}" > ./.parameters
    sed -i "s/\s/\\n/g" ./.parameters
    JVM_OPTS="-Dde.hub.mufuzz.mf.args=@$(pwd)/.parameters"
    echo $JVM_OPTS
    export JVM_OPTS
    /usr/bin/env bash -c "timeout ${DURATION}m $DRIVER_PATH -v -o $TEST_DIR -t 100000 -c $JAR_PATH de.hub.mse.emf.multifile.AFLTest withResultHandling"

    # copy plot data
    echo "Saving Plot data..."
    savePlotData "$TEST_DIR/plot_data"

    #save logfile
    mv ./jqf.log "$BASEDIR/$LOGFILE"

    # archive working dir
    echo "Archiving working directory..."
    zip -r "$BASEDIR/work.zip" "$WORKING_DIR" && rm -r "$WORKING_DIR"
    exit 0;
}

function iterateInitFiles() {
  for initSize in "${INITIAL_FILES[@]}"; do
    CURRENT_INIT="$initSize"
    # execute test
    executeTest
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

# execution preparation
mkdir -p "$EXEC_DIR" || return 1
cp ./*.ecore "$EXEC_DIR"
cd "$EXEC_DIR" || return 1

# execution itself
for method in "${TEST_METHOD[@]}"; do
  CURRENT_METHOD="$method"
  iterateModelWidths
done

echo ""
echo "===== DONE ====="
