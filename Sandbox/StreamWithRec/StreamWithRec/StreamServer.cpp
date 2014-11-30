
#include "stdafx.h"
#include "StreamServer.h"
#include "ASRwrapper.h"
using namespace std;

int RunStreamingServer(int argc, _TCHAR* argv[])
{
	WSADATA w;							/* Used to open windows connection */
	unsigned short port_number;			/* Port number to use */
	int a1, a2, a3, a4;					/* Components of address in xxx.xxx.xxx.xxx form */
	int client_length;					/* Length of client struct */
	int bytes_received;					/* Bytes received from client */
	SOCKET sd;							/* Socket descriptor of server */
	struct sockaddr_in server;			/* Information about the server */
	struct sockaddr_in client;			/* Information about the client */
	char buffer[BUFFER_SIZE];			/* Where to store received data */
	struct hostent *hp;					/* Information about this computer */
	char host_name[256];				/* Name of the server */

	/* Interpret command line */
	if (argc == 2)
	{
		/* Use local address */
		if (swscanf_s(argv[1], L"%u", &port_number) != 1)
		{
			usage();
		}
	}
	else if (argc == 3)
	{
		/* Copy address */
		if (swscanf_s(argv[1], L"%d.%d.%d.%d", &a1, &a2, &a3, &a4) != 4)
		{
			usage();
		}
		if (swscanf_s(argv[2], L"%u", &port_number) != 1)
		{
			usage();
		}
	}
	else
	{
		usage();
	}

	/* Open windows connection */
	if (WSAStartup(0x0101, &w) != 0)
	{
		fprintf(stderr, "Could not open Windows connection.\n");
		exit(0);
	}

	/* Open a datagram socket */
	sd = socket(AF_INET, SOCK_DGRAM, 0);
	if (sd == INVALID_SOCKET)
	{
		fprintf(stderr, "Could not create socket.\n");
		WSACleanup();
		exit(0);
	}

	/* Clear out server struct */
	memset((void *)&server, '\0', sizeof(struct sockaddr_in));

	/* Set family and port */
	server.sin_family = AF_INET;
	server.sin_port = htons(port_number);

	/* Set address automatically if desired */
	if (argc == 2)
	{
		/* Get host name of this computer */
		gethostname(host_name, sizeof(host_name));
		hp = gethostbyname(host_name);

		/* Check for NULL pointer */
		if (hp == NULL)
		{
			fprintf(stderr, "Could not get host name.\n");
			closesocket(sd);
			WSACleanup();
			exit(0);
		}

		/* Assign the address */
		server.sin_addr.S_un.S_un_b.s_b1 = hp->h_addr_list[0][0];
		server.sin_addr.S_un.S_un_b.s_b2 = hp->h_addr_list[0][1];
		server.sin_addr.S_un.S_un_b.s_b3 = hp->h_addr_list[0][2];
		server.sin_addr.S_un.S_un_b.s_b4 = hp->h_addr_list[0][3];
	}
	/* Otherwise assign it manually */
	else
	{
		server.sin_addr.S_un.S_un_b.s_b1 = (unsigned char)a1;
		server.sin_addr.S_un.S_un_b.s_b2 = (unsigned char)a2;
		server.sin_addr.S_un.S_un_b.s_b3 = (unsigned char)a3;
		server.sin_addr.S_un.S_un_b.s_b4 = (unsigned char)a4;
	}

	/* Bind address to socket */
	if (bind(sd, (struct sockaddr *)&server, sizeof(struct sockaddr_in)) == -1)
	{
		fprintf(stderr, "Could not bind name to socket.\n");
		closesocket(sd);
		WSACleanup();
		exit(0);
	}

	/* Print out server information */
	printf("Server running on %u.%u.%u.%u\n", (unsigned char)server.sin_addr.S_un.S_un_b.s_b1,
		(unsigned char)server.sin_addr.S_un.S_un_b.s_b2,
		(unsigned char)server.sin_addr.S_un.S_un_b.s_b3,
		(unsigned char)server.sin_addr.S_un.S_un_b.s_b4);

	printf("Press CTRL + C to quit\n");

	
	HRESULT hr;
	CComPtr<IStream> pMemStream;
	HGLOBAL hGlobal = GlobalAlloc(GMEM_MOVEABLE, sizeof(pMemStream));
	hr = ::CreateStreamOnHGlobal(hGlobal, true, &pMemStream);
	int i = 0;
	/* Loop and get data from clients */
	while (1)
	{

		client_length = (int)sizeof(struct sockaddr_in);

		/* Receive bytes from client */
		bytes_received = recvfrom(sd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&client, &client_length);
		//printf("Received packet!");

		if (bytes_received < 0)
		{
			fprintf(stderr, "Could not receive datagram.\n");
			closesocket(sd);
			WSACleanup();
			exit(0);
		}
		ULONG cbWritten;
		pMemStream->Write(buffer, sizeof(buffer), &cbWritten);
		wcout << L"i=" << i << L". Written" << cbWritten << endl;
		i++;
		if (i == 50)
		{
			LARGE_INTEGER zero;
			zero.LowPart = 0;
			zero.HighPart = 0;
			pMemStream->Seek(zero, STREAM_SEEK_SET, NULL);
			//PlaySound();
			char tmpBuffer[BUFFER_SIZE*50];
			pMemStream->Read(tmpBuffer, sizeof(tmpBuffer), &cbWritten);
			add a header!!!
			//PlaySoundA(tmpBuffer, NULL, SND_MEMORY | SND_SYNC);
			//for (UINT j = 0; j < cbWritten; j++)
			//{
			//	if (tmpBuffer[j] != '\0')
			//		cout << tmpBuffer[j];
			//}
			//DecodeFromMem(pMemStream);
			pMemStream->Seek(zero, STREAM_SEEK_SET, NULL);
			i = 0;
		}

		///* Check for time request */
		//if (strcmp(buffer, "GET TIME\r\n") == 0)
		//{
		//	/* Get current time */
		//	current_time = time(NULL);

		//	/* Send data back */
		//	if (sendto(sd, (char *)&current_time, (int)sizeof(current_time), 0, (struct sockaddr *)&client, client_length) != (int)sizeof(current_time))
		//	{
		//		fprintf(stderr, "Error sending datagram.\n");
		//		closesocket(sd);
		//		WSACleanup();
		//		exit(0);
		//	}
		//}
	}
	closesocket(sd);
	WSACleanup();
	return 0;
}

void usage(void)
{
	fprintf(stderr, "timeserv [server_address] port\n");
	exit(0);
}

int DecodeFromMem(IStream * pMemStream)
{

	CASRwrapper asrEngine;
	std::wstring sPathToFile = L"";// CarnegieMellon.wav";
	asrEngine.InitSpeech(sPathToFile, pMemStream);

		asrEngine.Listen();
		HANDLE handleEvent = asrEngine.GetNotifyHandle();
		HANDLE handles[1];
		handles[0] = handleEvent;
		WaitForMultipleObjects(1, handles, FALSE, INFINITE);
		std::wstring speechRes;
		asrEngine.GetText(speechRes);
		asrEngine.StopListenning();
		wcout << speechRes << endl;
		//Sleep(100000);
		//system("PAUSE");
	return 0;
}