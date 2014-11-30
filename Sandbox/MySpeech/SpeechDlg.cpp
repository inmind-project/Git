// SpeechDlg.cpp : implementation file
//

#include "stdafx.h"
#include "Speech.h"
#include "SpeechDlg.h"
#include <sphelper.h>
#include "resource.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	//{{AFX_DATA(CAboutDlg)
	enum { IDD = IDD_ABOUTBOX };
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CAboutDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	//{{AFX_MSG(CAboutDlg)
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
	//{{AFX_DATA_INIT(CAboutDlg)
	//}}AFX_DATA_INIT
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CAboutDlg)
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
	//{{AFX_MSG_MAP(CAboutDlg)
		// No message handlers
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CSpeechDlg dialog

CSpeechDlg::CSpeechDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CSpeechDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CSpeechDlg)
	m_edit = _T("");
	//}}AFX_DATA_INIT
	// Note that LoadIcon does not require a subsequent DestroyIcon in Win32
	m_pVoice=NULL;
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CSpeechDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CSpeechDlg)
	DDX_Text(pDX, IDC_EDIT_DICT, m_edit);
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CSpeechDlg, CDialog)
	//{{AFX_MSG_MAP(CSpeechDlg)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_VOICE_TRAINING, OnButtonVoiceTraining)
	ON_BN_CLICKED(IDC_BUTTON_MICRO_SETUP, OnButtonMicroSetup)
	ON_BN_CLICKED(IDC_BUTTON_SPEAK, OnButtonSpeak)
	ON_BN_CLICKED(IDC_BUTTON_ABOUT, OnButtonAbout)
	//}}AFX_MSG_MAP
	ON_MESSAGE(WM_RECOEVENT,OnRecoEvent)
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CSpeechDlg message handlers

BOOL CSpeechDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon
	
	// TODO: Add extra initialization here
	CToolBar m_wndToolBar;
	if (!m_wndToolBar.CreateEx(this, TBSTYLE_FLAT, WS_CHILD | WS_VISIBLE | CBRS_TOP
	| CBRS_GRIPPER | CBRS_TOOLTIPS | CBRS_FLYBY | CBRS_SIZE_DYNAMIC) ||
	!m_wndToolBar.LoadToolBar(IDR_TOOLBAR1))
	{
		TRACE0("Failed to create toolbar\n");
		MessageBox(L"FAILED");
	}
	//Initialization of Com
	::CoInitialize(NULL);
		
	if(!OnInitSpeech())
		EndDialog(0);

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CSpeechDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CSpeechDlg::OnPaint() 
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CSpeechDlg::OnQueryDragIcon()
{
	return (HCURSOR) m_hIcon;
}

//Speech Initialization is done here
BOOL CSpeechDlg::OnInitSpeech()
{
	HRESULT hr=S_OK;

	hr=cpRecoEngine.CoCreateInstance(CLSID_SpInprocRecognizer);

	if( SUCCEEDED(hr) )
	{
		hr = cpRecoEngine->CreateRecoContext(&m_cpRecoCtxt);
	}

	if( SUCCEEDED(hr) )
	{
		hr=m_cpRecoCtxt->SetNotifyWindowMessage(m_hWnd,WM_RECOEVENT, 0, 0 );
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

	 // create default audio object
    CComPtr<ISpAudio> cpAudio;
    hr = SpCreateDefaultObjectFromCategoryId(SPCAT_AUDIOIN, &cpAudio);

    // set the input for the engine
    hr = cpRecoEngine->SetInput(cpAudio, TRUE);
    hr = cpRecoEngine->SetRecoState( SPRST_ACTIVE );
	
	if (SUCCEEDED(hr))
    {
        // Specifies that the grammar we want is a dictation grammar.
        // Initializes the grammar (m_cpDictationGrammar)
		// using ISpRecoContext
        hr = m_cpRecoCtxt->CreateGrammar( GID_DICTATION, &m_cpDictationGrammar );
    }
    
	if  (SUCCEEDED(hr))
    {
        //Load the dictation tool for the grammar specified
		//using ISpRecoGrammar
		hr = m_cpDictationGrammar->LoadDictation(NULL, SPLO_STATIC);
    }
    
	if (SUCCEEDED(hr))
    {
        //After loading the Dictation set the dictation state to 
		//active using ISpRecoGrammar 
		hr = m_cpDictationGrammar->SetDictationState( SPRS_ACTIVE );
    }
    
	if (FAILED(hr))
    {
        //Release the grammar using ISpRecoGrammar
		m_cpDictationGrammar.Release();
    }

    return (hr == S_OK);
}

LRESULT CSpeechDlg::OnRecoEvent(WPARAM,LPARAM)
{
	USES_CONVERSION;
    CSpEvent event;

    // Process all of the recognition events
    while (event.GetFrom(m_cpRecoCtxt) == S_OK)
    {
        switch (event.eEventId)
        {
            case SPEI_SOUND_START:
                m_bSound = TRUE;
                break;

            case SPEI_SOUND_END:
                if (m_bSound)
                {
                    m_bSound = FALSE;
                    if (!m_bReco)
                    {
                        // The sound has started and ended, 
                        // but the engine has not succeeded in recognizing anything
						const TCHAR szNoise[] = _T("<noise>");
                        m_edit=szNoise;
						UpdateData(FALSE);
						//::SendDlgItemMessage( m_hDlg, IDC_EDIT_DICT, 
						//	EM_REPLACESEL, TRUE, (LPARAM) szNoise );
                    }
                    m_bReco = FALSE;
                }
                break;

            case SPEI_RECOGNITION:
                // There may be multiple recognition results, so get all of them
                {
                    m_bReco = TRUE;
                    static const WCHAR wszUnrecognized[] = L"<Unrecognized>";

                    CSpDynamicString dstrText;
                    if (FAILED(event.RecoResult()->GetText(SP_GETWHOLEPHRASE, SP_GETWHOLEPHRASE, TRUE, 
                                                            &dstrText, NULL)))
                    {
                        dstrText = wszUnrecognized;
                    }

                    // Concatenate a space onto the end of the recognized word
                    dstrText.Append(L" ");
					
					//m_edit=dstrText;
					
					//UpdateData(FALSE);
                    ::SendDlgItemMessage(m_hWnd, IDC_EDIT_DICT, EM_REPLACESEL, TRUE, (LPARAM) W2T(dstrText) );

                }
                break;

        }
    }
	return 0L;
}


void CSpeechDlg::OnButtonVoiceTraining() 
{
	// TODO: Add your control notification handler code here
	// Brings up the SR-engine-specific user training UI
    cpRecoEngine->DisplayUI(m_hWnd, NULL, SPDUI_UserTraining, NULL, 0);
}

void CSpeechDlg::OnButtonMicroSetup() 
{
	// TODO: Add your control notification handler code here
    // Brings up the SR-engine-specific mic training UI
	cpRecoEngine->DisplayUI(m_hWnd, NULL, SPDUI_MicTraining, NULL, 0);
}

void CSpeechDlg::OnButtonSpeak() 
{
	// TODO: Add your control notification handler code here
    HRESULT hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&m_pVoice);
    if( SUCCEEDED( hr ) )
    {
		UpdateData();
		
		//conversion or string to wide character
		wchar_t* wszStr;
		//int len = m_edit.GetLength();
		//wszStr = new wchar_t[len + 50];
		//mbstowcs(wszStr,m_edit,len+1);	

        //hr = m_pVoice->Speak(wszStr, 0, NULL);
		hr = m_pVoice->Speak(m_edit, 0, NULL);
        m_pVoice->Release();
        m_pVoice = NULL;

		m_edit="";
		UpdateData(FALSE);
    }

}

void CSpeechDlg::OnButtonAbout() 
{
	// TODO: Add your control notification handler code here
	CAboutDlg* pDlg=new CAboutDlg;
	pDlg->DoModal();
	delete(pDlg);
}
