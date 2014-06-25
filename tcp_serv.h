/*
 * tcp_serv_client.h
 *
 *  Created on: Jun 25, 2014
 *      Author: c. Mysirlidis
 */

#ifndef TCP_SERV_CLIENT_H_
#define TCP_SERV_CLIENT_H_



#define SERVER_PORT 5100
#define MAX_MSG 1024

/* function readline */
int server(void * args);

int parse_message(char * msg);

int update_client_bw(int client_id, double bw);

int update_client_network(int client_id, char * network) ;


#endif /* TCP_SERV_CLIENT_H_ */
