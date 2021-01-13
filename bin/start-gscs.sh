#!/bin/sh -x
. ./start-env.sh
sh ${GS_HOME}/bin/gs.sh --server ${GS_MANAGER_SERVERS} container create \
	--memory=1g --count=1 \
	--vm-option=-Duse_map_server=true \
	--vm-option=-Dcom.gs.ops-ui.enabled=false \
	--vm-option=-Dcom.gs.transport_protocol.lrmi.max-threads=1 \
	--vm-option=-Dcom.gs.transport_protocol.lrmi.max-conn-pool=1 \
	--vm-option=-Dcom.gs.lrmi.nio.selector.handler.client.threads=1 \
	--vm-option=-Dcom.gs.transport_protocol.lrmi.selector.threads=1 \
	--vm-option=-XX:+UseSerialGC \
	${GS_MANAGER_SERVERS}