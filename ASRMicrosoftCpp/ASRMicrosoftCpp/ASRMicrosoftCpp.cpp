// ASRMicrosoftCpp.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "ASRwrapper.h"

using namespace std;

int _tmain(int argc, _TCHAR* argv[])
{

	CASRwrapper asrEngine;
	std::wstring sPathToFile = L"C:\\InMind\\temp\\fromClient5.wav";
	asrEngine.InitSpeech(sPathToFile);
	while (1)
	{
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
	}
	return 0;
}

