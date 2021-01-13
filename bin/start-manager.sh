#!/bin/sh -x
source start-env.sh
rm nohup.out
nohup sh ${GS_HOME}/bin/gs.sh host run-agent --webui=false --auto
