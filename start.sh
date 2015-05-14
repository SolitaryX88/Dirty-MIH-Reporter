#!/bin/sh

scriptdir=`dirname $0`

echo "Clearing previous reporters!"
killall -15 dreport-sim
killall -15 db_net_node_calc.scr

echo "Starting Reporter (TCP/Reporter)!"
$scriptdir/Release/dreport-sim >> /dev/null &

echo "Starting DBase networknodes updater."
$scriptdir/db_net_node_calc.scr >> /dev/null &

#sudo kill -9 $(pidof dreport-sim)


