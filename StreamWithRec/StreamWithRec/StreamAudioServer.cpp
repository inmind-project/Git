// StreamAudioServer.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "StreamServer.h"
#include "HighLevelServer.h"
#define DEFAULT_TCP_PORT 4444

int _tmain(int argc, _TCHAR* argv[])
{
	int port_number;			/* Port number to use */

	/* Interpret command line */
	if (argc == 2)
	{
		/* Use local address */
		if (swscanf_s(argv[1], L"%d", &port_number) != 1)
		{
			usage();
		}
	}
	else
	{
		port_number = DEFAULT_TCP_PORT;
	}

	HighLevelServer(port_number);

	return 0;
}


void usage(void)
{
	fprintf(stderr, "[port]\n");
	exit(0);
}
