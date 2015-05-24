#!/bin/bash


gnome-terminal -e 'watch -n 1 mysql -u root -h localhost -e \"USE andsf_db\; SELECT id, signal, throughput FROM s14_clients_mobility\;USE GUI_db\; SELECT andsf_client_key_id AS ID, FL AS FwLayer, DL AS DrpLayer, FR AS FwdRate, DR as DrpRate FROM GUI_info\; \" '
