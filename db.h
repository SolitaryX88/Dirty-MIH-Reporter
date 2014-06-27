/*
 * db.h
 *
 *  Created on: Jun 25, 2014
 *      Author: C. Mysirlidis
 */

#ifndef DB_H_
#define DB_H_


#define MAX_QUERY 512

void db_init_connect();

void db_full_client_report(double *bw, double *signal_rec, int * client_id, char * net);

void db_update_client_network(int * client_id, char * network) ;

void db_update_client_bw(int * client_id, double *bw);

#endif /* DB_H_ */
