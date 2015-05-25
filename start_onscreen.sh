#!/bin/sh

scriptdir=`dirname $0`

echo "Clearing previous reporters!"
killall -15 dreport-sim
killall -15 db_net_node_calc.scr
mysql -u mane --password="epc" -h localhost -e "USE andsf_db; UPDATE s14_clients_mobility SET throughput = 0.02 WHERE id = 50;"

echo "Starting Reporter (TCP/Reporter)!"
screen -S dreport -d -m $scriptdir/Release/dreport-sim

echo "Starting DBase networknodes updater."
screen -S dbnode -d -m $scriptdir/db_net_node_calc.scr

#sudo kill -9 $(pidof dreport-sim)


