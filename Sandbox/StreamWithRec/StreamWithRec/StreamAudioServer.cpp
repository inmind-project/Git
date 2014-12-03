// StreamAudioServer.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "StreamServer.h"

int _tmain(int argc, _TCHAR* argv[])
{
	//PlaySoundA("C:\\InMind\\temp\\fromClient.wav", NULL, SND_FILENAME | SND_SYNC);

	RunStreamingServer(argc, argv);
	//DecodeFromMem(NULL);

	return 0;
}
