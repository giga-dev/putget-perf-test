#!/usr/bin/env bash
. ./start-env.sh
java -cp "${GS_HOME}/lib/required/*":"../target/*" -DtestCycles=1000 -DnumberOfThreads=1 com.gigaspaces.app.Test $*