
#include "stdafx.h"
#include "StreamServer.h"
#include "ASRwrapper.h"
using namespace std;

#define WAV_HEADER_SIZE (44)

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

	int maxItterationsBeforeDecode = 1001;

	ULARGE_INTEGER UZERO;
	UZERO.LowPart = 0;
	UZERO.HighPart = 0;
	LARGE_INTEGER ZERO;
	ZERO.LowPart = 0;
	ZERO.HighPart = 0;

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
	sd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
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

	//add a timeout to the socket
	int iTimeout = 500; //half a second
	setsockopt(sd, SOL_SOCKET, SO_RCVTIMEO, (const char *)&iTimeout, sizeof(iTimeout));
	
	HRESULT hr;
	CComPtr<IStream> pMemStream;
	HGLOBAL hGlobal = GlobalAlloc(GMEM_MOVEABLE, sizeof(pMemStream));
	hr = ::CreateStreamOnHGlobal(hGlobal, true, &pMemStream);
	

	char* fullBuffer = (char*)malloc(BUFFER_SIZE * maxItterationsBeforeDecode + WAV_HEADER_SIZE);
	memset(fullBuffer, 0, BUFFER_SIZE * maxItterationsBeforeDecode + WAV_HEADER_SIZE);

	int i = 0;
	int buffOffset = WAV_HEADER_SIZE;
	/* Loop and get data from clients */
	while (1)
	{
		memset(buffer, 0, BUFFER_SIZE);
		client_length = (int)sizeof(struct sockaddr_in);

		/* Receive bytes from client */
		bytes_received = recvfrom(sd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&client, &client_length);
		int lastError = 0;
		if (bytes_received == SOCKET_ERROR)
		{
			lastError = WSAGetLastError();
			if (lastError == WSAETIMEDOUT)
			{
				bytes_received = 0;
				if (i==0)
					continue;
			}
		}
		wcout << L"i=" << i << L". Received:" << bytes_received << endl;
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
			closesocket(sd);
			WSACleanup();
			exit(0);
		}
		//ULONG cbWritten;
		//pMemStream->Write(buffer, bytes_received*sizeof(char), &cbWritten);
		//wcout << L"i=" << i << L". Written" << cbWritten << endl;
		i++;
		if (i == maxItterationsBeforeDecode || bytes_received == 0)//&& buffOffset > 44) // no need since if i==0 we continue above.
		{

			
			//add a header //seems to work with no header as well.
			WriteWavHeader(fullBuffer, 44100, false);
			CloseWav(fullBuffer, false, buffOffset);
			WriteToFile(fullBuffer, "c:\\InMind\\temp\\fromClient.wav", buffOffset); //TODO: save files in a better place!
			//PlaySoundA(fullBuffer, NULL, SND_MEMORY | SND_SYNC);

			pMemStream->SetSize(UZERO);// deleting old stream
			ULONG cbWritten;
			//recognition does not require WAV header
			pMemStream->Write(fullBuffer + WAV_HEADER_SIZE, (buffOffset - WAV_HEADER_SIZE)*sizeof(char), &cbWritten);
			pMemStream->Seek(ZERO, STREAM_SEEK_SET, NULL);
			DecodeFromMem(pMemStream);
			pMemStream->Seek(ZERO, STREAM_SEEK_SET, NULL);
			i = 0;
			buffOffset = WAV_HEADER_SIZE;
			memset(fullBuffer, 0, BUFFER_SIZE * maxItterationsBeforeDecode + WAV_HEADER_SIZE);
		}
		//	sendto(sd, (char *)&current_time, (int)sizeof(current_time), 0, (struct sockaddr *)&client, client_length)/* Send data back */
	}
	free(fullBuffer);
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
	std::wstring sPathToFile = L"";// C:\\InMind\\temp\\fromClient.wav";// L"c:\\InMind\\temp\\fromClient.wav";//L"C:\\InMind\\temp\\Downtown.wav";
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
		std::wstring speechRes;
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
	return 0;
}


// returns -1 if fails. Works on both FILE* and char*. offset is used only in char*.
int WriteFileOrCharArr(const char* st, int bytes, void* writeTo, bool isFile, int offset)
{
	int ret = 1;
	if (isFile)
		ret = fwrite(st, sizeof(char), bytes, (FILE*)writeTo);
	else
	{
		if (bytes == 1)
			*((char*)writeTo + offset) = *st;
		else
		{
			//ret = _snprintf(((char*)writeTo + offset), bytes, "%s", st);
			memcpy(((char*)writeTo + offset), st,bytes);
		}
	}
	return ret;
}


//------------------------------------------------------------------------
// Wav related functions - Used by AudioRecorder and AudioServer
// to write wav header files. Copied from trunk version.
//------------------------------------------------------------------------
// G: write a number to a file, given an endianness
static void PutNum(long num, void *writeTo, int endianness, int bytes, bool isFile, int offset)
{
	int i;
	/*unsigned*/ char c;
	if (!endianness)
		i = 0;
	else
		i = bytes - 1;
	while (bytes--)
	{
		c = (num >> (i << 3)) & 0xff;
		if (WriteFileOrCharArr(&c, 1, writeTo, isFile, offset) == -1)
		{
			perror("Could not write to output.");
			exit(1);
		}
		offset++;
		if (endianness)
			i--;
		else
			i++;
	}
}


// G: writes a wav header to f (inspired from http://www.techband.in/2011/04/write-wav-header-in-cc.html)
// For explanations on the fields, visit https://ccrma.stanford.edu/courses/422/projects/WaveFormat/
// SampleRate defaults to 8000
void WriteWavHeader(void *writeTo, int SampleRate, bool isFile)
{
	int bitsPerSample = 16;
	int writtenAccumulated = 0;
	if (writeTo != NULL){
		/* quick and dirty */
		WriteFileOrCharArr("RIFF", 4, writeTo, isFile, writtenAccumulated);               /*  0-3 */
		writtenAccumulated += 4;
		PutNum(3203383023, writeTo, 0, 4, isFile, writtenAccumulated);        /*  4-7 */ // 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size) === bytes + 36
		writtenAccumulated += 4;
		WriteFileOrCharArr("WAVEfmt ", 8, writeTo, isFile, writtenAccumulated);           /*  8-15 */
		writtenAccumulated += 8;
		PutNum(16, writeTo, 0, 4, isFile, writtenAccumulated);                /* 16-19 */
		writtenAccumulated += 4;
		PutNum(1, writeTo, 0, 2, isFile, writtenAccumulated);                 /* 20-21 */ //PCM=1
		writtenAccumulated += 2;
		PutNum(1, writeTo, 0, 2, isFile, writtenAccumulated);                 /* 22-23 */ //NumChannels=Mono=1
		writtenAccumulated += 2;
		PutNum(SampleRate, writeTo, 0, 4, isFile, writtenAccumulated);             /* 24-27 */ // our sampling rate is 8000
		writtenAccumulated += 4;
		PutNum(SampleRate * 1 * bitsPerSample/8 /*Amos: fixed*/, writeTo, 0, 4, isFile, writtenAccumulated);         /* 28-31 */ //ByteRate == SampleRate * NumChannels * BitsPerSample/8
		writtenAccumulated += 4;
		PutNum(1 * bitsPerSample/8, writeTo, 0, 2, isFile, writtenAccumulated);                 /* 32-33 */ // block align == NumChannels * BitsPerSample/8
		writtenAccumulated += 2;
		PutNum(bitsPerSample, writeTo, 0, 2, isFile, writtenAccumulated);                /* 34-35 */
		writtenAccumulated += 2;
		WriteFileOrCharArr("data", 4, writeTo, isFile, writtenAccumulated); /* 36-39 */
		writtenAccumulated += 4;
		// We write 'beefbeef'. This has to be replaced by the real value once we know
		// the size of the file.  see CloseWav()
		PutNum(3203383023, writeTo, 1, 4, isFile, writtenAccumulated);
		writtenAccumulated += 4;
	}
}

// G: closes a wave file (reads the size of file, and write it)
// totLengthBytes is required only for char*
void CloseWav(void *writeTo, bool isFile, int totLengthBytes)
{
	if (writeTo != NULL)
	{
		// We write the number of bytes in the wave header
		if (isFile)
		{
			FILE* f = (FILE*)writeTo;
			fseek(f, 0, SEEK_END);
			long totLengthBytes = ftell(f);
		}
		if (isFile)
			fseek((FILE*)writeTo, 4, SEEK_SET);
		PutNum(totLengthBytes - 8, writeTo, 0, 4, isFile, 4);  // We write ChunkSize
		if (isFile)
			fseek((FILE*)writeTo, 40, SEEK_SET);
		PutNum(totLengthBytes - WAV_HEADER_SIZE, writeTo, 0, 4, isFile, 40);  // We write SubchunkSize2
		if (isFile)
			fclose((FILE*)writeTo);
	}
}


void WriteToFile(char* data, char* filePath, int dataBytes)
{
	FILE* f = fopen(filePath, "wb");
	fwrite(data, sizeof(char), dataBytes, f);
	fclose(f);

}