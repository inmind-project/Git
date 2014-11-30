#include "stdafx.h"
#include "ASRwrapper.h"


CASRwrapper::CASRwrapper()
{
	::CoInitialize(NULL);
	//OnInitSpeech();
}



CASRwrapper::~CASRwrapper()
{
	if (m_cpDictationGrammar != NULL)
	{
		//Release the grammar using ISpRecoGrammar
		m_cpDictationGrammar.Release();
	}
	::CoUninitialize();
}

HRESULT CASRwrapper::InitSpeech(std::wstring sPathToFile)
{
	HRESULT hr = S_OK;

	hr = cpRecoEngine.CoCreateInstance(CLSID_SpInprocRecognizer);

	if (SUCCEEDED(hr))
	{
		hr = cpRecoEngine->CreateRecoContext(&m_cpRecoCtxt);
	}

	if (SUCCEEDED(hr))
	{
		WPARAM wparam = NULL;
		LPARAM lparam = NULL;
		hr = m_cpRecoCtxt->SetNotifyWin32Event();
		//hr = m_cpRecoCtxt->SetNotifyCallbackFunction(SpRecCallback,wparam,lparam);
		//	hr = m_cpRecoCtxt->SetNotifyWindowMessage(m_hWnd, WM_RECOEVENT, 0, 0);
	}

	if (SUCCEEDED(hr))
	{
		// This specifies which of the recognition events are going 
		//to trigger notifications.Here, all we are interested in 
		//is the beginning and ends of sounds, as well as
		// when the engine has recognized something
		//using ISpRecoContext
		const ULONGLONG ullInterest = SPFEI(SPEI_RECOGNITION);
		hr = m_cpRecoCtxt->SetInterest(ullInterest, ullInterest);
	}

	if (SUCCEEDED(hr))
	{
		// Specifies that the grammar we want is a dictation grammar.
		// Initializes the grammar (m_cpDictationGrammar)
		// using ISpRecoContext
		hr = m_cpRecoCtxt->CreateGrammar(GID_DICTATION, &m_cpDictationGrammar);
	}

	if (SUCCEEDED(hr))
	{
		//Load the dictation tool for the grammar specified
		//using ISpRecoGrammar
		hr = m_cpDictationGrammar->LoadDictation(NULL, SPLO_STATIC);
	}

	if (!sPathToFile.empty())
	{
		CComPtr<ISpStream> cpInputStream;

		// Create basic SAPI stream object
		// NOTE: The helper SpBindToFile can be used to perform the following operations
		hr = cpInputStream.CoCreateInstance(CLSID_SpStream);
		// Check hr

		CSpStreamFormat sInputFormat;
		// generate WaveFormatEx structure, assuming the wav format is 22kHz, 16-bit, Stereo
		hr = sInputFormat.AssignFormat(SPSF_22kHz16BitMono);
		// Check hr

		//   for read-only access, since it will only be access by the SR engine
		hr = cpInputStream->BindToFile(sPathToFile.c_str(),
			SPFM_OPEN_READONLY,
			&(sInputFormat.FormatId()),
			sInputFormat.WaveFormatExPtr(),
			SPFEI_ALL_EVENTS);
		// Check hr

		// connect wav input to recognizer
		// SAPI will negotiate mismatched engine/input audio formats using system audio codecs, so second parameter is not important - use default of TRUE
		hr = cpRecoEngine->SetInput(cpInputStream, TRUE);
		// Check hr

	}
	else
	{
		// create default audio object
		CComPtr<ISpAudio> cpAudio;
		hr = SpCreateDefaultObjectFromCategoryId(SPCAT_AUDIOIN, &cpAudio);

		// set the input for the engine
		hr = cpRecoEngine->SetInput(cpAudio, TRUE);
		hr = cpRecoEngine->SetRecoState(SPRST_ACTIVE);
	}

	if (FAILED(hr))
	{
		//Release the grammar using ISpRecoGrammar
		m_cpDictationGrammar.Release();
	}

	return hr;
}

//Speech Initialization is done here
HRESULT CASRwrapper::Listen()
{

	//After loading the Dictation set the dictation state to 
	//active using ISpRecoGrammar 
	HRESULT hr = S_FALSE;
	if (m_cpDictationGrammar != NULL)
		hr = m_cpDictationGrammar->SetDictationState(SPRS_ACTIVE);

	return hr;
}

//Speech Initialization is done here
HRESULT CASRwrapper::StopListenning()
{

	HRESULT hr = S_FALSE;
	if (m_cpDictationGrammar != NULL)
		hr = m_cpDictationGrammar->SetDictationState(SPRS_INACTIVE);

	return hr;
}

HANDLE CASRwrapper::GetNotifyHandle()
{
	HANDLE hndl = NULL;
	if (m_cpRecoCtxt != NULL)
		hndl = m_cpRecoCtxt->GetNotifyEventHandle();
	return hndl;
}


void CASRwrapper::GetText(std::wstring& speechRes)
{
	//HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&m_pVoice);
	//hr = m_pVoice->Speak(speechRes, 0, NULL);
	//m_pVoice->Release();
	//m_pVoice = NULL;

	const ULONG maxEvents = 10;
	SPEVENT events[maxEvents];

	ULONG eventCount;
	HRESULT hr;
	hr = m_cpRecoCtxt->GetEvents(maxEvents, events, &eventCount);

	// Warning hr equal S_FALSE if everything is OK 
	// but eventCount < requestedEventCount
	if (!(hr == S_OK || hr == S_FALSE)) {
		;
	}

	ISpRecoResult* recoResult;
	recoResult = reinterpret_cast<ISpRecoResult*>(events[0].lParam);

	wchar_t* text;
	hr = recoResult->GetText(SP_GETWHOLEPHRASE, SP_GETWHOLEPHRASE, FALSE, &text, NULL);
	speechRes.assign(text);
	CoTaskMemFree(text);
}