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

#include "etc.h"
#include "tcp_serv.h"
#include "db.h"

int server(void *args) {
	int listenfd = 0, connfd = 0;
	ssize_t r;
	struct sockaddr_in serv_addr;

	char send_buff[MAX_MSG];
	char read_buff[MAX_MSG];

	time_t ticks;

	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	memset(&serv_addr, '0', sizeof(serv_addr));
	memset(send_buff, '\0', sizeof(send_buff));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(SERVER_PORT);

	bind(listenfd, (struct sockaddr*) &serv_addr, sizeof(serv_addr));

	listen(listenfd, 10);

	while (1) {
		connfd = accept(listenfd, (struct sockaddr*) NULL, NULL);

		ticks = time(NULL);
		memset(read_buff, '\0', sizeof(read_buff));

		//snprintf(sendBuff, sizeof(sendBuff), "%.24s\r\n", ctime(&ticks));
		//r = write(connfd, sendBuff, strlen(sendBuff));

		r = read(listenfd, read_buff, sizeof(read_buff));

		if (r < 0) {

			fprintf(stderr, "Error reading socket! \n");
		}

		parse_message(read_buff);

		close(connfd);
		sleep(1);
	}

	return (0);
}

int parse_message(char * msg) {

#ifdef DEBUG
	fprintf(stdout, "Message received: ", %s);
#endif

	const char * s = ":";
	int client_id = -1;
	double bw = -1.0;
	char * w_net;
	char * order = strtok(msg, s);

	if (strcmp("updateBW", order) == 0) {

		client_id = atoi(strtok(msg, s));
		bw = atof(strtok(msg, s));

		if ((client_id > 0 && client_id < 1000) && (bw > 0.0 && bw < 100.0)) {
			db_update_client_bw(&client_id,&bw);
		} else {
			goto err;
		}

	} else if (strcmp("setNet", order) == 0) {
		client_id = atoi(strtok(msg, s));
		w_net = strtok(msg, s);

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
