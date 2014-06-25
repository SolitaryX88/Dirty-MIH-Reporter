/*
 * report.cpp
 *
 *  Created on: Apr 24, 2014
 *      Author: C. Mysirlidis
 *      comment: Quick and dirty version of the traffic reporter with tracefile
 */

#include "etc.h"
#include "reporter.h"

//#define DEBUG

#ifndef DEBUG
#include "db.h"
#endif

#define VIRT
#define TEST

#ifdef VIRT

#include "tcp_serv.h"

const char* rcv_path = "/sys/class/net/eth1/statistics/tx_bytes";

#else

const char* rcv_path = "/sys/class/net/eth1/statistics/rx_bytes";

#endif

int network_time = 0;
int trace_exists;
unsigned long long prev_bytes = 0;

FILE * fp;
char network[25] = "WiFi";


int client_id = 46;

int main(int argc, char** argv) {

	if (argc > 1) {
		fp = fopen(argv[1], "r"); // read mode

		if (fp == NULL) {
			fprintf(stderr, "Error while opening the file.\n");
			trace_exists = 0;
		} else
			trace_exists = 1;
	}

	init_bw();

#ifndef DEBUG
	db_init_connect();
#endif

	while (1) {

		sleep(SIGNAL);

		if (trace_exists)
			trace_file();

#ifdef DEBUG
		fprintf(stdout, "%d: The bw: %f and the rnd signal: %f\n", my_t++, get_bandwidth(),
				rnd_signal());
#else

		db_full_client_report(get_bandwidth(), rnd_signal(), &client_id, network);
#endif

	}

	fclose(fp);

	return (EXIT_SUCCESS);
}

void trace_file() {

	int remain = (network_time -= SIGNAL), f;

	if (remain < 1) {

		memset(network, '\0', sizeof(network));

		if ((f = fscanf(fp, "%s\t%d\n", network, &network_time)) == EOF) {
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
			strcpy(network, "WiFi");
		}

		if (network_time < 1 || network_time > 45) {
			fprintf(stderr, "Error in reading the time remaining in network from tracefile, setting 5 sec\n");
		}
	}
}

void init_bw() {

	FILE *f = fopen(rcv_path, "r");
	fscanf(f, "%llu", &prev_bytes);
	fclose(f);

}

double * get_bandwidth() {

	FILE *f = fopen(rcv_path, "r");

	double * bw = (double*) malloc(sizeof(double));
	unsigned long long bytes = 0;

	fscanf(f, "%llu", &bytes);
	fclose(f);

	*bw = ((double) ((bytes - prev_bytes) * 8) / (1024 * 1024));

	if (!*bw)
		*bw = 102e-6;

	prev_bytes = bytes;

	return (bw);

}

double * rnd_signal() {

	srand(time(NULL));
	double * s = (double *) malloc(sizeof(double));
	*s = (double) ((rand() % 600) + 9200) / 100;
	return (s);
}

