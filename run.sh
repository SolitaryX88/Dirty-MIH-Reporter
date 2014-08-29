#!/bin/sh
echo "Clearing previous reporters!"
killall -15 dreport-sim

echo "Starting Reporter (TCP/Reporter)!"
./Release/dreport-sim &

echo "Starting DBase networknodes updater."
./db_net_node_calc.scr

#sudo kill -9 $(pidof dreport-sim)

