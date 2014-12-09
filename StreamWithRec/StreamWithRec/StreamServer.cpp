
#include "stdafx.h"
#include "StreamServer.h"
#include "ASRwrapper.h"
#include "Utils.h"

using namespace std;

wstring RunStreamingServer(int port_number)
{
	int iTimeout = 500; //half a second
	int maxBuffersReceived = 1001;
	int maxTimedOut = 14; //7 seconds
	double amplyfingRate = 0.18;

	wstring retText;
	WSADATA w;							/* Used to open windows connection */
	int client_length;					/* Length of client struct */
	int bytes_received;					/* Bytes received from client */
	SOCKET listenSocket = INVALID_SOCKET;							/* Socket descriptor of server */
	struct sockaddr_in client;			/* Information about the client */
	int iResult = 0;

	struct addrinfo *paddrInformation = NULL;
	struct addrinfo hints;

	char buffer[BUFFER_SIZE];			/* Where to store received data */

	ULARGE_INTEGER UZERO;
	UZERO.LowPart = 0;
	UZERO.HighPart = 0;
	LARGE_INTEGER ZERO;
	ZERO.LowPart = 0;
	ZERO.HighPart = 0;



	/* Open windows connection */
	if (WSAStartup(0x0101, &w) != 0)
	{
		fprintf(stderr, "Could not open Windows connection.\n");
		exit(0);
	}


	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_DGRAM;
	hints.ai_protocol = IPPROTO_UDP;
	hints.ai_flags = AI_PASSIVE;

	char szPortNum[MAX_PORT_LENGTH];
	_itoa_s(port_number, szPortNum, MAX_PORT_LENGTH, 10);
	// Resolve the server address and port
	iResult = getaddrinfo(NULL, szPortNum, &hints, &paddrInformation);
	if (iResult != 0) {
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		return retText;
	}

	/* Open a datagram socket */
	listenSocket = socket(paddrInformation->ai_family, paddrInformation->ai_socktype, paddrInformation->ai_protocol);
	if (listenSocket == INVALID_SOCKET)
	{
		fprintf(stderr, "Could not create socket.\n");
		WSACleanup();
		return retText;
	}


	/* Bind address to socket */
	iResult = bind(listenSocket, paddrInformation->ai_addr, (int)paddrInformation->ai_addrlen);
	if (iResult == SOCKET_ERROR) 
	{
		printf("bind failed with error: %d\n", WSAGetLastError());
		freeaddrinfo(paddrInformation);
		closesocket(listenSocket);
		WSACleanup();
		return retText;
	}

	printf("UDP Connection opened.\n");

	//add a timeout to the socket
	setsockopt(listenSocket, SOL_SOCKET, SO_RCVTIMEO, (const char *)&iTimeout, sizeof(iTimeout));
	
	HRESULT hr;
	CComPtr<IStream> pMemStream;
	HGLOBAL hGlobal = GlobalAlloc(GMEM_MOVEABLE, sizeof(pMemStream));
	hr = ::CreateStreamOnHGlobal(hGlobal, true, &pMemStream);
	

	char* fullBuffer = (char*)malloc(BUFFER_SIZE * maxBuffersReceived + WAV_HEADER_SIZE);
	memset(fullBuffer, 0, BUFFER_SIZE * maxBuffersReceived + WAV_HEADER_SIZE);

	int timedOut = 0;
	int buffersReceived = 0;
	int buffOffset = WAV_HEADER_SIZE;
	/* get data from clients */
	while (buffersReceived < maxBuffersReceived)
	{
		memset(buffer, 0, BUFFER_SIZE);
		client_length = (int)sizeof(struct sockaddr_in);

		/* Receive bytes from client */
		bytes_received = recvfrom(listenSocket, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&client, &client_length);
		int lastError = 0;
		if (bytes_received == SOCKET_ERROR)
		{
			lastError = WSAGetLastError();
			if (lastError == WSAETIMEDOUT)
			{
				bytes_received = 0;
				if (buffersReceived == 0)
				{
					timedOut++;
					if (timedOut >= maxTimedOut)
						break;
					continue;
				}
			}
		}
		wcout << L"buffersReceived=" << buffersReceived << L". Received:" << bytes_received << endl;
		//if (i < 80) //DEBUG: in order to reduce begining noise - move wait to client.
		//{//DEBUG
		//	i++;//DEBUG
		//	continue;//DEBUG
		//}//DEBUG

		memcpy(fullBuffer + buffOffset, buffer, bytes_received);
		buffOffset += bytes_received;
		if (bytes_received < 0)
		{
			fprintf(stderr, "Could not receive datagram.\n Error:%d", lastError);
			break;
		}
		//ULONG cbWritten;
		//pMemStream->Write(buffer, bytes_received*sizeof(char), &cbWritten);
		//wcout << L"i=" << i << L". Written" << cbWritten << endl;
		buffersReceived++;
		if (buffersReceived == maxBuffersReceived || bytes_received == 0)//&& buffOffset > 44) // no need since if i==0 we continue above.
		{

			
			//add a header for saved file
			WriteWavHeader(fullBuffer, 44100, false);
			CloseWav(fullBuffer, false, buffOffset);
			// It turns out that by reducing the amplitude, SAPI does so much better (no idea why!)
			ReduceAmplitude(fullBuffer + WAV_HEADER_SIZE, buffOffset - WAV_HEADER_SIZE, amplyfingRate);
			WriteToFile(fullBuffer, "c:\\InMind\\temp\\fromClient.wav", buffOffset); //TODO: save files in a better place!
			//PlaySoundA(fullBuffer, NULL, SND_MEMORY | SND_SYNC);

			pMemStream->SetSize(UZERO);// deleting old stream
			ULONG cbWritten;
			//recognition does not require WAV header
			
			pMemStream->Write(fullBuffer + WAV_HEADER_SIZE, (buffOffset - WAV_HEADER_SIZE)*sizeof(char), &cbWritten);
			pMemStream->Seek(ZERO, STREAM_SEEK_SET, NULL);
			retText=DecodeFromMem(pMemStream);
			pMemStream->Seek(ZERO, STREAM_SEEK_SET, NULL);
			break;

			//i = 0;
			//buffOffset = WAV_HEADER_SIZE;
			//memset(fullBuffer, 0, BUFFER_SIZE * maxItterationsBeforeDecode + WAV_HEADER_SIZE);
		}
		//	sendto(sd, (char *)&current_time, (int)sizeof(current_time), 0, (struct sockaddr *)&client, client_length)/* Send data back */
	}
	free(fullBuffer);
	closesocket(listenSocket);
	WSACleanup();
	return retText;
}

wstring DecodeFromMem(IStream * pMemStream)
{
	wstring speechRes;

	CASRwrapper asrEngine;
	wstring sPathToFile = L"";// C:\\InMind\\temp\\fromClient.wav";// L"c:\\InMind\\temp\\fromClient.wav";//L"C:\\InMind\\temp\\Downtown.wav";
	HRESULT hr = asrEngine.InitSpeech(sPathToFile, pMemStream);
	if (SUCCEEDED(hr))
	{
		//TODO: must be done in a thread!!!! can't block the server!!!
		asrEngine.Listen();
		HANDLE handleEvent = asrEngine.GetNotifyHandle();
		HANDLE handles[1];
		handles[0] = handleEvent;
		DWORD maxWait = 3000;
		DWORD waitRes = WaitForMultipleObjects(1, handles, FALSE, maxWait);
		if (waitRes == WAIT_OBJECT_0)
		{
			asrEngine.GetText(speechRes);
		}
		else if (waitRes == 0x102)
		{
			speechRes.assign(L"Did not hear anything");
		}
		asrEngine.StopListenning();
		wcout << speechRes << endl;
		//Sleep(100000);
		//system("PAUSE");
	}
	else
		wcout << "ERROR!" << endl;
	return speechRes;
}

