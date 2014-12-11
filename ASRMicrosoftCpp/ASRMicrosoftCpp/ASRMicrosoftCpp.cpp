// ASRMicrosoftCpp.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "ASRwrapper.h"

using namespace std;

int _tmain(int argc, _TCHAR* argv[])
{

	CASRwrapper asrEngine;
	std::wstring sPathToFile = L"C:\\InMind\\temp\\fromClient.wav";
	asrEngine.InitSpeech(sPathToFile);
	while (1)
	{
		asrEngine.Listen();
		HANDLE handleEvent = asrEngine.GetNotifyHandle();
		HANDLE handles[1];
		handles[0] = handleEvent;
		WaitForMultipleObjects(1, handles, FALSE, INFINITE);
		std::wstring speechRes;
		float firstConfidence;
		const int requestedAlternates = 50;
		std::wstring alternates[requestedAlternates];
		float confidences[requestedAlternates];
		asrEngine.GetText(speechRes, &firstConfidence, requestedAlternates, alternates, confidences);
		asrEngine.StopListenning();
		wcout << speechRes << endl;
		//Sleep(100000);
		//system("PAUSE");
	}
	return 0;
}

