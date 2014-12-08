
#ifndef HIGH_LEVEL_SERVER_H
#define HIGH_LEVEL_SERVER_H

// Need to link with Ws2_32.lib
#pragma comment (lib, "Ws2_32.lib")
// #pragma comment (lib, "Mswsock.lib")

#define DEFAULT_BUFLEN 512

int HighLevelServer(int port_number);

#endif