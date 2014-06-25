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


void * srv_tcp_server();

int srv_parse_message(char * msg);

int srv_init();

#endif /* TCP_SERV_CLIENT_H_ */
