/*
 * db.c
 *
 *  Created on: Jun 25, 2014
 *      Author: C. Mysirlidis
 */

#include "etc.h"
#include "db.h"

#include <mysql/mysql.h>


MYSQL *db_conn;
MYSQL_RES *result;

int query_len;

const char* db_user = "mane";
const char* db_pass = "epc";
const char* db = "andsf_db";
const char* db_ip = "192.168.1.134";


//TODO P-thread of database;

void db_init_connect() {

	/*Obtain a connection handler*/
	db_conn = mysql_init(NULL);

	if (db_conn == NULL) {
		fprintf(stderr, "(%s) Error %u: %s\n",__FUNCTION__, mysql_errno(db_conn), mysql_error(db_conn));
		exit(EXIT_FAILURE);
	}

	/*establish a connection to the database.
	 1. connection handler
	 2. host name
	 3. user name
	 4. password parameters to the function.
	 5. database name
	 6. port    number
	 7. unix socket
	 8. client flag. */

	if (!mysql_real_connect(db_conn, db_ip, db_user, db_pass, db, 0, NULL, 0)) {
		fprintf(stderr, "(%s) mysql_real_connect error %u: %s\n", __FUNCTION__, mysql_errno(db_conn),
		      mysql_error(db_conn));
		exit(EXIT_FAILURE);
	}

}

const char* sql_update_full_client_stat =
      "UPDATE s14_clients_mobility SET `wireless_type` = '%s', `throughput` = %f, `signal` = %f, `last_update` = CURRENT_TIMESTAMP() WHERE `id` = %d; ";

void db_full_client_report(double *bw, double * signal_rec, int * client_id, char * net) {

	char db_query[MAX_QUERY];

	snprintf(db_query, MAX_QUERY, sql_update_full_client_stat, net, *bw, *signal_rec, *client_id);

#ifndef DEBUG_DB

	if (mysql_query(db_conn, db_query)) {
		fprintf(stderr, "(%s) MySQL error %u: %s (2)", __FUNCTION__, mysql_errno(db_conn), mysql_error(db_conn));
		exit(EXIT_FAILURE); //wait for the program to exit
	}

#else
	fprintf(stdout, "(%s) Query req: %s" ,__FUNCTION__, db_query );
#endif

}

const char* sql_update_client_bw =
      "UPDATE s14_clients_mobility SET `throughput` = %f, `last_update` = CURRENT_TIMESTAMP() WHERE `id` = %d; ";

void db_update_client_bw(int * client_id, double *bw) {

	char db_query[MAX_QUERY];

	snprintf(db_query, MAX_QUERY, sql_update_client_bw, *bw, *client_id);

#ifndef DEBUG_DB

	if (mysql_query(db_conn, db_query)) {
		fprintf(stderr, "(%s) MySQL error %u: %s (2)", __FUNCTION__, mysql_errno(db_conn), mysql_error(db_conn));
		exit(EXIT_FAILURE); //wait for the program to exit
	}

#else
	fprintf(stdout, "(%s) Query req: %s" ,__FUNCTION__, db_query );
#endif
}

const char* sql_update_client_net =
      "UPDATE s14_clients_mobility SET `wireless_type` = '%s', `last_update` = CURRENT_TIMESTAMP() WHERE `id` = %d; ";

void db_update_client_network(int * client_id, char * network) {

	char db_query[MAX_QUERY];

	snprintf(db_query, MAX_QUERY, sql_update_client_bw, network, *client_id);

#ifndef DEBUG_DB

	if (mysql_query(db_conn, db_query)) {
		fprintf(stderr, "(%s) MySQL error %u: %s (2)", __FUNCTION__, mysql_errno(db_conn), mysql_error(db_conn));
		exit(EXIT_FAILURE); //wait for the program to exit
	}

#else

	fprintf(stdout, "(%s) Query req: %s" ,__FUNCTION__, db_query );

#endif

}

