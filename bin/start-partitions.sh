#!/bin/sh -x
source start-env.sh
sh ${GS_HOME}/bin/gs.sh --server ${GS_MANAGER_SERVERS} space deploy rgtest --partitions 1