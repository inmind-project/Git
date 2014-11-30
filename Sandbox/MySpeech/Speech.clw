; CLW file contains information for the MFC ClassWizard

[General Info]
Version=1
LastClass=CSpeechDlg
LastTemplate=CDialog
NewFileInclude1=#include "stdafx.h"
NewFileInclude2=#include "Speech.h"

ClassCount=3
Class1=CSpeechApp
Class2=CSpeechDlg
Class3=CAboutDlg

ResourceCount=4
Resource1=IDD_SPEECH_DIALOG
Resource2=IDR_MAINFRAME
Resource3=IDD_ABOUTBOX
Resource4=IDR_TOOLBAR1

[CLS:CSpeechApp]
Type=0
HeaderFile=Speech.h
ImplementationFile=Speech.cpp
Filter=N

[CLS:CSpeechDlg]
Type=0
HeaderFile=SpeechDlg.h
ImplementationFile=SpeechDlg.cpp
Filter=D
BaseClass=CDialog
VirtualFilter=dWC
LastObject=IDC_EDIT_DICT

[CLS:CAboutDlg]
Type=0
HeaderFile=SpeechDlg.h
ImplementationFile=SpeechDlg.cpp
Filter=D

[DLG:IDD_ABOUTBOX]
Type=1
Class=CAboutDlg
ControlCount=6
Control1=IDC_STATIC,static,1342308480
Control2=IDOK,button,1342373889
Control3=IDC_STATIC,static,1342308352
Control4=IDC_STATIC,static,1342308352
Control5=IDC_STATIC,static,1342177283
Control6=IDC_STATIC,static,1342177283

[DLG:IDD_SPEECH_DIALOG]
Type=1
Class=CSpeechDlg
ControlCount=6
Control1=IDCANCEL,button,1342242816
Control2=IDC_EDIT_DICT,edit,1350631552
Control3=IDC_BUTTON_VOICE_TRAINING,button,1342242816
Control4=IDC_BUTTON_MICRO_SETUP,button,1342242816
Control5=IDC_BUTTON_ABOUT,button,1342242816
Control6=IDC_BUTTON_SPEAK,button,1342242816

[TB:IDR_TOOLBAR1]
Type=1
Class=?
Command1=ID_BUTTON32771
CommandCount=1

