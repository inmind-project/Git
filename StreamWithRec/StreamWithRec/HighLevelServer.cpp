// HighLevelServer.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "HighLevelServer.h"
#include "StreamServer.h"

#define DEFAULT_AUDIO_PORT 50005
using namespace std;

int HighLevelServer(int port_number)
{
	string defaultMessageSeperator = ";";
	string defaultMessageEnd = "\n";

	WSADATA wsaData;
	int iResult;

	SOCKET ListenSocket = INVALID_SOCKET;
	SOCKET ClientSocket = INVALID_SOCKET;

	struct addrinfo *paddrInformation = NULL;
	struct addrinfo hints;

	int iSendResult;
	char recvbuf[DEFAULT_BUFLEN];
	int recvbuflen = DEFAULT_BUFLEN;

	// Initialize Winsock
	iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != 0) {
		printf("WSAStartup failed with error: %d\n", iResult);
		return 1;
	}

	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	char szPortNum[MAX_PORT_LENGTH];
	_itoa_s(port_number, szPortNum, MAX_PORT_LENGTH, 10);
	// Resolve the server address and port
	iResult = getaddrinfo(NULL, szPortNum, &hints, &paddrInformation);
	if (iResult != 0) {
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		return 1;
	}

	// Create a SOCKET for connecting to server
	ListenSocket = socket(paddrInformation->ai_family, paddrInformation->ai_socktype, paddrInformation->ai_protocol);
	if (ListenSocket == INVALID_SOCKET) {
		printf("socket failed with error: %ld\n", WSAGetLastError());
		freeaddrinfo(paddrInformation);
		WSACleanup();
		return 1;
	}

	// Setup the TCP listening socket
	iResult = bind(ListenSocket, paddrInformation->ai_addr, (int)paddrInformation->ai_addrlen);
	if (iResult == SOCKET_ERROR) {
		printf("bind failed with error: %d\n", WSAGetLastError());
		freeaddrinfo(paddrInformation);
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	freeaddrinfo(paddrInformation);

	while (1)
	{
		iResult = listen(ListenSocket, SOMAXCONN);
		if (iResult == SOCKET_ERROR) {
			printf("listen failed with error: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}

		/* Print out server information */
		printf("Server running on port %d\n", port_number);

		// Accept a client socket
		ClientSocket = accept(ListenSocket, NULL, NULL);
		if (ClientSocket == INVALID_SOCKET) {
			printf("accept failed with error: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;
		}


		//Receive until the peer shuts down the connection

		//do {

		//	iResult = recv(ClientSocket, recvbuf, recvbuflen, 0);
		//	if (iResult > 0) {
		//		printf("Bytes received: %d\n", iResult);

		//		// Echo the buffer back to the sender
		//		iSendResult = send(ClientSocket, recvbuf, iResult, 0);
		//		if (iSendResult == SOCKET_ERROR) {
		//			printf("send failed with error: %d\n", WSAGetLastError());
		//			closesocket(ClientSocket);
		//			WSACleanup();
		//			return 1;
		//		}
		//		printf("Bytes sent: %d\n", iSendResult);
		//	}
		//	else if (iResult == 0)
		//		printf("Connection closing...\n");
		//	else  {
		//		printf("recv failed with error: %d\n", WSAGetLastError());
		//		closesocket(ClientSocket);
		//		WSACleanup();
		//		return 1;
		//	}

		//} while (iResult > 0);


		std::string portMessage = "UDP" + defaultMessageSeperator + std::to_string(DEFAULT_AUDIO_PORT) + defaultMessageEnd; //TODO: better use xml format
		iSendResult = send(ClientSocket, portMessage.c_str(), portMessage.length(), 0);

		if (iSendResult == portMessage.length())
		{
			std::wstring wtextFromAudio = RunStreamingServer(DEFAULT_AUDIO_PORT);

			std::string textFromAudio(wtextFromAudio.begin(), wtextFromAudio.end()); //converting from wstring to string

			string textMessage = "You said: " + textFromAudio + defaultMessageEnd; //TODO: better use xml format
			iSendResult = send(ClientSocket, textMessage.c_str(), textMessage.length(), 0);
			printf("Sent: %s\n", textFromAudio.c_str());
		}

		//Sleep(1000);

		// shutdown the connection since we're done
		iResult = shutdown(ClientSocket, SD_SEND);
		if (iResult == SOCKET_ERROR) {
			printf("shutdown failed with error: %d\n", WSAGetLastError());
			closesocket(ClientSocket);
			WSACleanup();
			return 1;
		}

		// cleanup
		closesocket(ClientSocket);
	}
	// No longer need server socket
	closesocket(ListenSocket); //could close after listening if didn't expect a new client.
	WSACleanup();

	return 0;
}

