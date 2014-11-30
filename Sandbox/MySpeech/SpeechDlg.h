// SpeechDlg.h : header file
//

#if !defined(AFX_SPEECHDLG_H__F20BA307_BA2F_4A58_AC6A_74FADA90E924__INCLUDED_)
#define AFX_SPEECHDLG_H__F20BA307_BA2F_4A58_AC6A_74FADA90E924__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <sphelper.h>

#define MAX_EDIT_TEXT   1000
#define GID_DICTATION   0           // Dictation grammar has grammar ID 0

#define WM_RECOEVENT    WM_USER+5      // Window message used for recognition events

/////////////////////////////////////////////////////////////////////////////
// CSpeechDlg dialog

class CSpeechDlg : public CDialog
{
// Construction
public:
	BOOL OnInitSpeech();

	//Identifies a locale for national language support. 
	//Locale information is used for international string 
	//comparisons and localized member names. 
	LCID m_lcid;

	//Boolean variables
	BOOL m_bReco;
	BOOL m_bSound;

	//Initialize the Recognizer
	CComPtr<ISpRecognizer> cpRecoEngine;

	//The ISpRecoContext interface enables applications to create 
	//different functional views or contexts of the SR engine
	CComPtr<ISpRecoContext>     m_cpRecoCtxt;

	//The ISpRecoGrammar interface enables applications to manage 
	//the words and phrases that the SR engine will recognize.
	CComPtr<ISpRecoGrammar>		m_cpDictationGrammar;

	ISpVoice* m_pVoice;

	CSpeechDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	//{{AFX_DATA(CSpeechDlg)
	enum { IDD = IDD_SPEECH_DIALOG };
	CString	m_edit;
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CSpeechDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	//{{AFX_MSG(CSpeechDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnButtonVoiceTraining();
	afx_msg void OnButtonMicroSetup();
	afx_msg void OnButtonSpeak();
	afx_msg void OnButtonAbout();
	//}}AFX_MSG
	afx_msg LRESULT OnRecoEvent(WPARAM,LPARAM);
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_SPEECHDLG_H__F20BA307_BA2F_4A58_AC6A_74FADA90E924__INCLUDED_)
