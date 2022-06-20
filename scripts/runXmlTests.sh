#!/usr/bin/env bash

# This script is meant to be useful for repeated test execution, features are:
#  - Self managed creation of separate execution dirs on each script start
#  - Structured execution of tests with different variables
#  - Own logfile generation and management
#  - Structured copies of Zest plot data in own directory
#
# The script logic is quite simple:
# Iterate different methods to test
#   Inside that iterate different model widths
#     Inside that iterate different model depths
#       Inside that iterate different amounts of random start files
#         Execute given test method with combination of variables
#         Save plot data in structured way

# argument checking
if [ $# -ne 2 ]; then
  echo "Needs exactly 2 arguments: JQF Driver Path and Jar Path"
  exit 1
fi

# argument renaming
DRIVER_PATH="$1"
JAR_PATH="$2"

# file constants
PLOT_DATA_SAVE_DIR="./plot_data"
LOGFILE="./executor.log"
EXEC_DIR="$(date +"%Y-%m-%d_%H-%M-%S")"

# execution related constants
TEST_METHOD=( 'svgSalamanderTest' )
#TEST_METHOD=( 'svgSalamanderTest' 'testBatik' 'testBatikTranscoder' )
#MODEL_WIDTHS=( 1 2 4 8 )
#MODEL_DEPTHS=( 1 2 4 8 )
#INITIAL_FILES=( 1 10 20 40)
#NUMBER_OF_LINKS=( 0 1 2 4 )
#DURATION=30 # minutes

MODEL_WIDTHS=( 1 4 8 )
MODEL_DEPTHS=( 1 4 8 )
DURATION=1 # minutes

# execution variables
CURRENT_METHOD=""
CURRENT_WIDTH=""
CURRENT_DEPTH=""
CURRENT_INIT=""
CURRENT_LINKS=""

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
  cp "$1" "$PLOT_DATA_SAVE_DIR/${CURRENT_METHOD}_w${CURRENT_WIDTH}_d${CURRENT_DEPTH}.csv"
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
    log ""
    log "===== Executing $CURRENT_METHOD with width=$CURRENT_WIDTH depth=$CURRENT_DEPTH ====="
    /usr/bin/env bash -c "$DRIVER_PATH --illegal-access=permit -Xmx4G -jar $JAR_PATH --failDir $FAIL_DIR --workingDir $WORKING_DIR --testDir $TEST_DIR --modelDepth $CURRENT_DEPTH --modelWidth $CURRENT_WIDTH --duration $DURATION $CURRENT_METHOD | tee -a $LOGFILE 2>/dev/null"

    # copy plot data
    #log "Saving Plot data..."
    #savePlotData "$TEST_DIR/plot_data"

    #log "Archiving working directory..."
    #zip -r "$BASEDIR/work.zip" "$WORKING_DIR" && rm -r "$WORKING_DIR"
}


function iterateModelDepth() {
    for depth in "${MODEL_DEPTHS[@]}"; do
      CURRENT_DEPTH="$depth"
        # iterate init files
      executeTest
    done
}

function iterateModelWidths() {
    for width in "${MODEL_WIDTHS[@]}"; do
      CURRENT_WIDTH="$width"
      # iterate model depths
      iterateModelDepth
    done
}

# execution preparation
mkdir -p "$EXEC_DIR" || return 1
#cp ./*.ecore "$EXEC_DIR"
cd "$EXEC_DIR" || return 1

# execution itself
for method in "${TEST_METHOD[@]}"; do
  CURRENT_METHOD="$method"
  # iterate model widths
  iterateModelWidths
done

echo ""
echo "===== DONE ====="