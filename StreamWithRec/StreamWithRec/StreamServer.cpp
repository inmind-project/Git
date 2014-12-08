
#include "stdafx.h"
#include "StreamServer.h"
#include "ASRwrapper.h"
#include <inttypes.h>
using namespace std;

#define WAV_HEADER_SIZE (44)

wstring RunStreamingServer(int port_number)
{
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

	int maxItterationsBeforeDecode = 1001;

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
	int iTimeout = 500; //half a second
	setsockopt(listenSocket, SOL_SOCKET, SO_RCVTIMEO, (const char *)&iTimeout, sizeof(iTimeout));
	
	HRESULT hr;
	CComPtr<IStream> pMemStream;
	HGLOBAL hGlobal = GlobalAlloc(GMEM_MOVEABLE, sizeof(pMemStream));
	hr = ::CreateStreamOnHGlobal(hGlobal, true, &pMemStream);
	

	char* fullBuffer = (char*)malloc(BUFFER_SIZE * maxItterationsBeforeDecode + WAV_HEADER_SIZE);
	memset(fullBuffer, 0, BUFFER_SIZE * maxItterationsBeforeDecode + WAV_HEADER_SIZE);

	int i = 0;
	int buffOffset = WAV_HEADER_SIZE;
	/* get data from clients */
	while (i < maxItterationsBeforeDecode)
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
			closesocket(listenSocket);
			WSACleanup();
			return retText;
		}
		//ULONG cbWritten;
		//pMemStream->Write(buffer, bytes_received*sizeof(char), &cbWritten);
		//wcout << L"i=" << i << L". Written" << cbWritten << endl;
		i++;
		if (i == maxItterationsBeforeDecode || bytes_received == 0)//&& buffOffset > 44) // no need since if i==0 we continue above.
		{

			
			//add a header for saved file
			WriteWavHeader(fullBuffer, 44100, false);
			CloseWav(fullBuffer, false, buffOffset);
			// It turns out that by reducing the amplitude, SAPI does so much better (no idea why!)
			ReduceAmplitude(fullBuffer + WAV_HEADER_SIZE, buffOffset - WAV_HEADER_SIZE, 0.2);
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

///TODO: Move all these to utils file



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

void ReduceAmplitude(char* buffer, int length, double reduceTo)
{
	int16_t* sample = (int16_t*)buffer;
	for (int i = 0; i < length / (sizeof(int16_t) / sizeof(char)); i++)
		sample[i] = sample[i] * reduceTo;
}