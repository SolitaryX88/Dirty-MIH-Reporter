#!/bin/bash
DIR=$(python -c "import os; print os.path.realpath(\"${0}\")")
scriptdir=$(dirname "$DIR")

sh $scriptdir/start_onscreen.sh;
