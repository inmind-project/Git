// StreamAudioServer.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "StreamServer.h"

int _tmain(int argc, _TCHAR* argv[])
{
	//PlaySoundA("C:\\InMind\\temp\\downtown.wav", NULL, SND_FILENAME | SND_SYNC);

	RunStreamingServer(argc, argv);

	return 0;
}
