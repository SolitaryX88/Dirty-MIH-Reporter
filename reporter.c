/*
 * report.cpp
 *
 *  Created on: Apr 24, 2014
 *      Author: C. Mysirlidis
 *      comment: Quick and dirty version of the traffic reporter with tracefile
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/io.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <inttypes.h>
#include <mysql/mysql.h>
#include <pthread.h>
#include <time.h>
#include <unistd.h>

#define MAX_QUERY 512
#define signal 1

//#define DEBUG

double get_bandwidth();
double rnd_signal();
void init_bw();
void report_to_db(double bw, double signal_rec);
void trace_file();
void connect_to_db();

MYSQL *db_conn;
MYSQL_RES *result;
int query_len;
char db_query[MAX_QUERY];

const char* db_user = "mane";
const char* db_pass = "epc";
const char* db = "andsf_db";
const char* db_ip = "192.168.1.134";

const char* sql_update_client_stat = "UPDATE s14_clients_mobility SET `wireless_type` = '%s', `throughput` = %f, `signal` = %f, `last_update` = CURRENT_TIMESTAMP() WHERE `id` = %d; ";

const int client_id = 46;

const char* rcv_path = "/sys/class/net/eth0/statistics/rx_bytes";

int network_time = 0;
int trace_exists ;
unsigned long long prev_bytes = 0;

FILE * fp;
char network[25] = "WiFi";

int main(int argc, char** argv) {

	int my_t = 0;

	if (argc > 1) {
		fp = fopen(argv[1], "r"); // read mode

		if (fp == NULL) {
			fprintf(stderr, "Error while opening the file.\n");
			trace_exists = 0;
		}else
			trace_exists = 1;
	}

	init_bw();

#ifndef DEBUG
	connect_to_db();
#endif

	while (1) {

		sleep(signal);

		if (trace_exists)
			trace_file();

#ifdef DEBUG
		fprintf(stdout, "%d: The bw: %f and the rnd signal: %f\n", my_t++,  get_bandwidth(),
				rnd_signal());
#else
		report_to_db(get_bandwidth(), rnd_signal());
#endif

	}

	fclose(fp);

	return (EXIT_SUCCESS);
}

void trace_file() {

	int remain = (network_time -= signal), f;

	if ( remain < 1) {

		memset(network, '\0',sizeof(network) );

		if ((f = fscanf(fp, "%s\t%d\n", network, &network_time)) == EOF){
			rewind(fp);
#ifdef DEBUG
			fprintf(stderr, "EOF!\n");
#endif
			trace_file();
		}

#ifdef DEBUG
		fprintf(stdout, "Changing Network to: %s\n", network);
#endif

		if (!strncmp(network, "WiFi", sizeof("WiFi")) == 0 && !strncmp(network, "LTE", sizeof("LTE")) == 0
				&& !strncmp(network, "WiFi-2", sizeof("WiFi-2")) == 0
				&& !strncmp(network, "WiFi/LTE", sizeof("WiFi/LTE")) == 0) {

			fprintf(stderr, "Error in reading the network(%s) from tracefile, setting WiFi\n", network);
			strcpy(network,"WiFi");
		}

		if(network_time < 1 || network_time > 45){
			fprintf(stderr, "Error in reading the time remaining in network from tracefile, setting 5 sec\n");
		}
	}
}

void init_bw() {

	FILE *f = fopen(rcv_path, "r");
	fscanf(f, "%llu", &prev_bytes);
	fclose(f);

}

double get_bandwidth() {

	FILE *f = fopen(rcv_path, "r");

	double bw;
	unsigned long long bytes = 0;

	fscanf(f, "%llu", &bytes);
	fclose(f);

	bw = ((double) ((bytes - prev_bytes) * 8) / (1024 * 1024));

	if (!bw)
		bw = 102e-6;

	prev_bytes = bytes;

	return (bw);

}

double rnd_signal() {

	srand(time(NULL));

	return ((double) ((rand() % 600) + 9200) / 100);
}


void report_to_db(double bw, double signal_rec){

	snprintf(db_query, MAX_QUERY, sql_update_client_stat, network, bw,  signal_rec, client_id);

	if (mysql_query(db_conn, db_query)) {
		fprintf(stderr, "Mysql_query error %u: %s (2)", mysql_errno(db_conn), mysql_error(db_conn));
		exit(EXIT_FAILURE); //wait for the program to exit
	}

}

void connect_to_db() {

	/*Obtain a connection handler*/
	db_conn = mysql_init(NULL);

	if (db_conn == NULL) {
		fprintf(stderr, "(DB) Error %u: %s\n", mysql_errno(db_conn), mysql_error(db_conn));
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
		fprintf(stderr, "(DB) mysql_real_connect error %u: %s\n", mysql_errno(db_conn), mysql_error(db_conn));
		exit(EXIT_FAILURE);
	}


}

