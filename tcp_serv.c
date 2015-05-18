/*
 * tcp_serv_client.c
 *
 *  Created on: Jun 25, 2014
 *      Author: c. Mysirlidis
 */

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <time.h>
#include <fcntl.h>

#include "etc.h"
#include "tcp_serv.h"
#include "db.h"

pthread_t server_thread;

void * srv_tcp_server() {

	int listenfd = 0, connfd = 0;
	struct sockaddr_in serv_addr;

	char send_buff[MAX_MSG];
	char read_buff[MAX_MSG];
	int keep = 0;
	time_t ticks;

	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	memset(&serv_addr, '0', sizeof(serv_addr));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(SERVER_PORT);

	bind(listenfd, (struct sockaddr*) &serv_addr, sizeof(serv_addr));

	listen(listenfd, 10);

	while (1) {

		connfd = accept(listenfd, (struct sockaddr*) NULL, NULL);
		keep = 1;
		while (keep) {
			usleep(10*1000);
			memset(read_buff, '\0', sizeof(read_buff));
			memset(send_buff, '\0', sizeof(send_buff));

			if (read(connfd, read_buff, sizeof(read_buff)) < 0) {
				fprintf(stderr, "(%s) Error reading socket! Resetting virtual client! \n", __FUNCTION__);
				//"updateBW:50:0.02:")
				srv_parse_message("updateBW:50:0.02:");
				keep = 0;
				break;
			}

			if (strcmp(read_buff, "quit\n") == 0) {

				strcpy(send_buff, "quit");

				if (write(connfd, send_buff, strlen(send_buff)) < 0) {
					fprintf(stderr, "(%s) Error writing msg: %s socket! \n", __FUNCTION__, send_buff);
					keep = 0;
					break;
				}

			} else {

				ticks = time(NULL);
				snprintf(send_buff, sizeof(send_buff), "OK @ %.24s\r\n", ctime(&ticks));

				if (write(connfd, send_buff, strlen(send_buff)) < 0) {
					fprintf(stderr, "(%s) Error writing msg: %s socket! \n", __FUNCTION__, send_buff);
					keep = 0;
					break;
				}
			}

#if defined(DEBUG_SKT)
			fprintf(stdout, "Received msg: ' %s ' \n", read_buff);
#else
			srv_parse_message(read_buff);
#endif

		}
		sleep(1);
		close(connfd);
	}

	return(NULL);
}

int srv_init() {

	int err;

	err = pthread_create(&(server_thread), NULL, &srv_tcp_server, NULL);
	if (err != 0)
		fprintf(stderr, "(%s) Error creating server thread :[%s]\n", __FUNCTION__, strerror(err));
	else
		printf("Server thread created successfully! \n");

	//pthread_join(server_thread, (void**) NULL );

	return (SUCCESS);
}

int srv_parse_message(char * msg) {

#ifdef DEBUG_SKT
	fprintf(stdout, "Message received: %s", msg);
#endif

	const char * s = ":";
	int client_id = -1;
	double bw = -1.0;
	char * w_net;
	char * order = strtok(msg, s);

	if(order==NULL)
		goto err;

	if (strcmp("updateBW", order) == 0) {

		client_id = atoi(strtok(NULL, s));
		bw = atof(strtok(NULL, s));

		if ((client_id > 0 && client_id < 1000) && (bw > 0.0 && bw < 100.0)) {
			db_update_client_bw(&client_id, &bw);
		} else {
			goto err;
		}

	} else if (strcmp("setNet", order) == 0) {
		client_id = atoi(strtok(NULL, s));
		w_net = strtok(NULL, s);

		if ((client_id > 0 && client_id < 1000) && (strcmp(w_net, "WiFi") == 0 || strcmp(w_net, "LTE") == 0)) {
			db_update_client_network(&client_id, w_net);
		} else {
			goto err;
		}

	} else {
		goto err;
	}

	return (SUCCESS);

	err: fprintf(stderr, "(%s) Message: %s could not be parsed! \n", __FUNCTION__, msg);
	return (ERROR);

}
