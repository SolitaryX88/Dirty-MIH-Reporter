#!/bin/sh

echo "Starting Reporter (TCP/Reporter)!"
./Release/dreport-sim &
echo "Starting DBase networknodes updater."
./db_net_node_calc.scr

