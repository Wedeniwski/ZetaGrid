; CLW file contains information for the MFC ClassWizard

[General Info]
Version=1
LastClass=CInstallDlg
LastTemplate=CPropertyPage
NewFileInclude1=#include "stdafx.h"
NewFileInclude2=#include "saver.h"
LastPage=0

ClassCount=22
Class1=AboutDlg
Class2=CControlCenterDlg
Class3=CDrawWnd
Class4=GeneralPage
Class5=CInstallDlg
Class6=CJavaError
Class7=KonfigurationDlg
Class8=OptionPage
Class9=PortDlg
Class10=ProcessPage
Class11=CProgressDlg
Class12=CSaverApp
Class13=CSaverWnd
Class14=CState
Class15=CSystemTray
Class16=CTestDlg
Class17=WorkUnitPage

ResourceCount=20
Resource1=IDD_PROPPAGE_LOGGING (English (U.S.))
Resource2=IDD_WAITFRAME
Resource3=IDD_DIALOG_CONTROL_CENTER
Resource4=IDD_ABOUTBOX
Resource5=IDD_PROPPAGE_CONNECTION (English (U.S.))
Resource6=IDD_PROPPAGE_OPTION (English (U.S.))
Resource7=IDD_STATE
Resource8=IDD_PROPPAGE_GENERAL (English (U.S.))
Resource9=IDD_DIALOG_PORT
Resource10=IDD_SAVER_DIALOG
Resource11=IDR_MENU_CONTROL
Resource12=IDD_DIALOG_JAVA_ERROR
Resource13=IDD_PROPPAGE_WORK_UNIT (English (U.S.))
Resource14=IDD_PROGRESS
Resource15=IDD_PROPPAGE_PROCESS (English (U.S.))
Class18=ConnectionPage
Resource16=IDD_DIALOG_LICENSE
Class19=CPowerPage
Resource17=IDD_PROPPAGE_SECURITY (English (U.S.))
Class20=LoggingPage
Resource18=IDD_DIALOG_INSTALL
Class21=CLicenseDlg
Resource19=IDD_PROPPAGE_POWER (English (U.S.))
Class22=CSecurityPage
Resource20=IDR_POPUP_MENU

[CLS:AboutDlg]
Type=0
BaseClass=CDialog
HeaderFile=AboutDlg.h
ImplementationFile=AboutDlg.cpp
LastObject=1

[CLS:CControlCenterDlg]
Type=0
BaseClass=CDialog
HeaderFile=ControlCenterDlg.h
ImplementationFile=ControlCenterDlg.cpp
Filter=D
VirtualFilter=dWC
LastObject=IDD_ACTIONS_STOP_COMPUTATION

[CLS:CDrawWnd]
Type=0
BaseClass=CWnd
HeaderFile=drawwnd.h
ImplementationFile=DRAWWND.CPP

[CLS:GeneralPage]
Type=0
BaseClass=CPropertyPage
HeaderFile=GeneralPage.h
ImplementationFile=GeneralPage.cpp
LastObject=GeneralPage
Filter=D
VirtualFilter=idWC

[CLS:CInstallDlg]
Type=0
BaseClass=CDialog
HeaderFile=InstallDlg.h
ImplementationFile=InstallDlg.cpp
Filter=D
VirtualFilter=dWC
LastObject=CInstallDlg

[CLS:CJavaError]
Type=0
BaseClass=CPropertyPage
HeaderFile=JavaError.h
ImplementationFile=JavaError.cpp
Filter=D
VirtualFilter=idWC
LastObject=IDC_CPU_USAGE_SU3

[CLS:KonfigurationDlg]
Type=0
BaseClass=CPropertySheet
HeaderFile=KonfigurationDlg.h
ImplementationFile=KonfigurationDlg.cpp
LastObject=IDABOUT
Filter=W
VirtualFilter=hWC

[CLS:OptionPage]
Type=0
BaseClass=CPropertyPage
HeaderFile=OptionPage.h
ImplementationFile=OptionPage.cpp
Filter=D
VirtualFilter=idWC
LastObject=OptionPage

[CLS:PortDlg]
Type=0
BaseClass=CDialog
HeaderFile=PortDlg.h
ImplementationFile=PortDlg.cpp

[CLS:CProgressDlg]
Type=0
BaseClass=CDialog
HeaderFile=ProgressDlg.h
ImplementationFile=ProgressDlg.cpp

[CLS:CSaverApp]
Type=0
BaseClass=CWinApp
HeaderFile=SAVER.H
ImplementationFile=SAVER.CPP

[CLS:CSaverWnd]
Type=0
BaseClass=CDrawWnd
HeaderFile=saverwnd.h
ImplementationFile=SAVERWND.CPP

[CLS:CState]
Type=0
BaseClass=CDialog
HeaderFile=State.h
ImplementationFile=State.cpp

[CLS:CSystemTray]
Type=0
BaseClass=CWnd
HeaderFile=SystemTray.h
ImplementationFile=SystemTray.cpp

[CLS:CTestDlg]
Type=0
BaseClass=CDialog
HeaderFile=TestDlg.h
ImplementationFile=TestDlg.cpp
LastObject=ID_CLOSE

[CLS:WorkUnitPage]
Type=0
BaseClass=CPropertyPage
HeaderFile=WorkUnitPage.h
ImplementationFile=WorkUnitPage.cpp
Filter=D
VirtualFilter=idWC
LastObject=ID_CLOSE

[DLG:IDD_ABOUTBOX]
Type=1
Class=AboutDlg
ControlCount=5
Control1=65535,static,1342177283
Control2=65535,static,1342308352
Control3=65535,static,1342308352
Control4=1,button,1342373889
Control5=65535,static,1342308352

[DLG:IDD_DIALOG_CONTROL_CENTER]
Type=1
Class=CControlCenterDlg
ControlCount=12
Control1=IDC_BUTTON_HIDE,button,1073807361
Control2=IDC_BUTTON_TRANSFER_WU,button,1342242816
Control3=IDC_STATIC_ACTIVE_THREADS,static,1342308352
Control4=IDC_STATIC_THREAD_ID,static,1073873408
Control5=IDC_COMBO_THREAD_ID,combobox,1075904771
Control6=IDC_STATIC_LOCAL_PACKAGES,static,1342308352
Control7=IDC_STATIC_PERFORMANCE,static,1342308352
Control8=IDC_STATIC_CURRENT_STATUS,static,1342308352
Control9=IDC_STATIC_PROGRESS,static,1342308352
Control10=IDC_STATIC_PERCENT,static,1342308866
Control11=IDC_PROGRESS_ACTIVE,msctls_progress32,1350696961
Control12=IDC_STATIC_REMAINING,static,1342308352

[DLG:IDD_PROPPAGE_GENERAL]
Type=1
Class=GeneralPage

[DLG:IDD_DIALOG_INSTALL]
Type=1
Class=CInstallDlg
ControlCount=12
Control1=IDC_STATIC,button,1342177287
Control2=IDC_RADIO_MODE_SCREEN_SAVER,button,1342242825
Control3=IDC_RADIO_MODE_SERVICE,button,1342242825
Control4=IDC_RADIO_MODE_COMMAND_LINE,button,1342177289
Control5=IDC_CHECK_SHORTCUT,button,1342242819
Control6=IDC_CHECK_START_CLIENT,button,1342242819
Control7=IDOK,button,1342242817
Control8=IDCANCEL,button,1342242816
Control9=IDC_BUTTON_HELP,button,1342242816
Control10=IDC_CHECK_START_CONTROL_CENTER,button,1342242819
Control11=IDC_CHECK_CMD_CONTROL_CENTER_STARTUP,button,1342242819
Control12=IDC_CHECK_SERVICE_CONTROL_CENTER_STARTUP,button,1342242819

[DLG:IDD_DIALOG_JAVA_ERROR]
Type=1
Class=CJavaError
ControlCount=4
Control1=IDOK,button,1342242817
Control2=IDC_STATIC_ERROR3,static,1342312448
Control3=IDC_STATIC_ERROR2,static,1342308352
Control4=IDC_STATIC_ERROR,static,1342308352

[DLG:IDD_SAVER_DIALOG]
Type=1
Class=KonfigurationDlg
ControlCount=5
Control1=IDOK,button,1342242817
Control2=IDCANCEL,button,1342242816
Control3=IDC_BUTTON_TEST,button,1342242816
Control4=IDABOUT,button,1342242816
Control5=IDC_BUTTON_HELP,button,1342242816

[DLG:IDD_PROPPAGE_OPTION]
Type=1
Class=OptionPage

[DLG:IDD_DIALOG_PORT]
Type=1
Class=PortDlg
ControlCount=6
Control1=IDC_STATIC,static,1342308864
Control2=IDC_EDIT_PORT_NUMBER,edit,1350639744
Control3=IDC_STATIC,static,1342308864
Control4=IDC_STATIC,static,1342308864
Control5=IDOK,button,1342242817
Control6=IDC_STATIC,static,1342308352

[DLG:IDD_PROPPAGE_PROCESS]
Type=1
Class=ProcessPage

[DLG:IDD_PROGRESS]
Type=1
Class=CProgressDlg
ControlCount=2
Control1=IDC_PROGRESS1,msctls_progress32,1350565888
Control2=IDC_STATIC,static,1342308352

[DLG:IDD_STATE]
Type=1
Class=CState
ControlCount=9
Control1=IDC_STATIC_CURRENT_STATUS,static,1342308352
Control2=IDC_STATIC_PROGRESS,static,1342308352
Control3=IDC_STATIC_PERCENT,static,1342308866
Control4=IDC_PROGRESS_ACTIVE,msctls_progress32,1350696961
Control5=IDC_STATIC_LOCAL_PACKAGES,static,1342308352
Control6=IDC_STATIC_CLOCK_LARGE,static,1342308865
Control7=IDC_STATIC_PERFORMANCE,static,1342308352
Control8=IDC_STATIC_REMAINING,static,1342308352
Control9=IDC_STATIC,static,1342177294

[DLG:IDD_WAITFRAME]
Type=1
Class=CTestDlg
ControlCount=1
Control1=IDC_STATUS,static,1342312961

[DLG:IDD_PROPPAGE_WORK_UNIT]
Type=1
Class=WorkUnitPage

[DLG:IDD_PROPPAGE_PROCESS (English (U.S.))]
Type=1
Class=ProcessPage
ControlCount=14
Control1=IDC_STATIC_PRIORITY,static,1342308864
Control2=IDC_COMBO_PRIO,combobox,1344339971
Control3=IDC_STATIC,static,1342308864
Control4=IDC_COMBO_RESOURCES,combobox,1344340227
Control5=IDC_STATIC,static,1342308864
Control6=IDC_STATIC,static,1342308864
Control7=IDC_SLIDER_CPU_USAGE,msctls_trackbar32,1342242841
Control8=IDC_STATIC,static,1342308864
Control9=IDC_STATIC,static,1342308864
Control10=IDC_EDIT_PROZESSOREN,edit,1350639744
Control11=IDC_TRANSFER_DETECT,edit,1350639744
Control12=IDC_STATIC_TRANSFER_DETECT,static,1342308864
Control13=IDC_STATIC,static,1342308352
Control14=IDC_STATIC,static,1342308352

[DLG:IDD_PROPPAGE_WORK_UNIT (English (U.S.))]
Type=1
Class=?
ControlCount=11
Control1=IDC_STATIC,static,1342308864
Control2=IDC_COMBO_TASK_NAME,combobox,1344340227
Control3=IDC_STATIC,static,1342308864
Control4=IDC_COMBO_PACKAGE_SIZE,combobox,1344339971
Control5=IDC_STATIC,static,1342308864
Control6=IDC_BUTTON_NOTE_SIZE_OF_WORK_UNIT,button,1342242816
Control7=IDC_STATIC,static,1342308864
Control8=IDC_EDIT_PACKAGES,edit,1350639744
Control9=IDC_STATIC,static,1342308864
Control10=IDC_BUTTON_NOTE_NUMBER_OF_WORK_UNITS,button,1342242816
Control11=IDC_COMPLETE_ALL_LOCAL_WU,button,1342242819

[DLG:IDD_PROPPAGE_GENERAL (English (U.S.))]
Type=1
Class=?
ControlCount=13
Control1=IDC_STATIC,static,1342308864
Control2=IDC_EDIT_NAME,edit,1350631552
Control3=IDC_BUTTON_NOTE_NAME,button,1342242816
Control4=IDC_STATIC,static,1342308864
Control5=IDC_EDIT_EMAIL,edit,1350631552
Control6=IDC_BUTTON_NOTE_EMAIL,button,1342242816
Control7=IDC_CHECK_MESSAGES,button,1342242819
Control8=IDC_CHECK_TEAM,button,1342242819
Control9=IDC_EDIT_TEAMNAME,edit,1484849280
Control10=IDC_BUTTON_NOTE_TEAMNAME,button,1342242816
Control11=IDC_STATIC,static,1342308352
Control12=IDC_VALUES_CHANGED_AT_SERVER,button,1342242819
Control13=IDC_BUTTON_CHANGE_AT_SERVER,button,1342242816

[DLG:IDD_PROPPAGE_OPTION (English (U.S.))]
Type=1
Class=?
ControlCount=13
Control1=IDC_STATIC,static,1342308864
Control2=IDC_EDIT_JAVA_VM,edit,1350631552
Control3=IDC_BUTTON_VM_CHOOSE,button,1342242816
Control4=IDC_STATIC_JAVA_LIB,static,1342308864
Control5=IDC_EDIT_JAVA_LIB,edit,1350631552
Control6=IDC_BUTTON_LIB_CHOOSE,button,1342242816
Control7=IDC_STATIC,static,1476526592
Control8=IDC_EDIT_ROOT_PATH,edit,1484849280
Control9=IDC_BUTTON_ROOT_BROWSE,button,1476460544
Control10=IDC_STATIC,button,1342177287
Control11=IDC_CHECK_DISPLAY_ST,button,1342242819
Control12=IDC_STATIC,button,1342177287
Control13=IDC_CHECK_SYSTEM_TRAY,button,1342242819

[MNU:IDR_MENU_CONTROL]
Type=1
Class=?
Command1=ID_CONFIG_CONFIG
Command2=ID_SERVER_CONFIG
Command3=IDD_ACTIONS_TRANSFER_WORK_UNITS
Command4=IDD_ACTIONS_RESTART_COMPUTATION
Command5=IDD_ACTIONS_STOP_COMPUTATION
Command6=ID_WEB_HOME
Command7=ID_WEB_NEWS
Command8=ID_HELP_FORUM
Command9=ID_WEB_STATISTIC
Command10=ID_WEB_STATISTIC_DETAIL
Command11=ID_WEB_TEAMMEMBERS
Command12=ID_LOG_VIEW_INFORMATION_LOG
Command13=ID_HELP_FAQ
Command14=ID_HELP_ABOUT
CommandCount=14

[MNU:IDR_POPUP_MENU]
Type=1
Class=?
Command1=ID_CONFIG_CONFIG
Command2=ID_SERVER_CONFIG
Command3=ID_WEB_HOME
Command4=ID_WEB_NEWS
Command5=ID_HELP_FORUM
Command6=ID_WEB_STATISTIC
Command7=ID_WEB_STATISTIC_DETAIL
Command8=ID_WEB_TEAMMEMBERS
Command9=ID_HELP_FAQ
Command10=ID_RESTART_COMPUTATION
Command11=ID_STOP_COMPUTATION
Command12=ID_CLOSE
Command13=ID_VIEW
CommandCount=13

[DLG:IDD_PROPPAGE_CONNECTION (English (U.S.))]
Type=1
Class=ConnectionPage
ControlCount=12
Control1=IDC_CHECK_USE_PROXY,button,1342242819
Control2=IDC_STATIC_PROXY_ADDRESS,static,1476526592
Control3=IDC_EDIT_PROXY_ADDRESS,edit,1484849280
Control4=IDC_STATIC_PROXY_PORT,static,1476526592
Control5=IDC_EDIT_PROXY_PORT,edit,1484857472
Control6=IDC_CHECK_PROXY_AUTHENTICATION,button,1476460547
Control7=IDC_STATIC_USERNAME,static,1476526592
Control8=IDC_EDIT_USERNAME,edit,1484849280
Control9=IDC_STATIC_PASSWORD,static,1476526592
Control10=IDC_EDIT_PASSWORD,edit,1484849312
Control11=IDC_STATIC,static,1342308352
Control12=IDC_CHECK_CONNECTION_TO_SERVER,button,1342242819

[CLS:ConnectionPage]
Type=0
HeaderFile=ConnectionPage.h
ImplementationFile=ConnectionPage.cpp
BaseClass=CPropertyPage
Filter=D
LastObject=ConnectionPage
VirtualFilter=idWC

[CLS:ProcessPage]
Type=0
HeaderFile=processpage.h
ImplementationFile=processpage.cpp
BaseClass=CPropertyPage
Filter=D
VirtualFilter=idWC
LastObject=IDD_ACTIONS_STOP_COMPUTATION

[DLG:IDD_PROPPAGE_POWER (English (U.S.))]
Type=1
Class=CPowerPage
ControlCount=102
Control1=IDC_STATIC,static,1342308353
Control2=IDC_STATIC,static,1342308353
Control3=IDC_STATIC,static,1342308352
Control4=IDC_STATIC,static,1342308353
Control5=IDC_STATIC,static,1342308353
Control6=IDC_STATIC,static,1342308352
Control7=IDC_STATIC,static,1342308353
Control8=IDC_STATIC,static,1342308353
Control9=IDC_STATIC,static,1342308352
Control10=IDC_STATIC,static,1342308864
Control11=IDC_DATETIMEPICKER_MO1,SysDateTimePick32,1342242873
Control12=IDC_DATETIMEPICKER_MO2,SysDateTimePick32,1342242873
Control13=IDC_CPU_USAGE_MO1,edit,1350639744
Control14=IDC_STATIC,static,1342308864
Control15=IDC_DATETIMEPICKER_MO3,SysDateTimePick32,1342242873
Control16=IDC_DATETIMEPICKER_MO4,SysDateTimePick32,1342242873
Control17=IDC_CPU_USAGE_MO2,edit,1350639744
Control18=IDC_STATIC,static,1342308864
Control19=IDC_DATETIMEPICKER_MO5,SysDateTimePick32,1342242873
Control20=IDC_DATETIMEPICKER_MO6,SysDateTimePick32,1342242873
Control21=IDC_CPU_USAGE_MO3,edit,1350639744
Control22=IDC_STATIC,static,1342308864
Control23=IDC_STATIC,static,1342308864
Control24=IDC_DATETIMEPICKER_TU1,SysDateTimePick32,1342242873
Control25=IDC_DATETIMEPICKER_TU2,SysDateTimePick32,1342242873
Control26=IDC_CPU_USAGE_TU1,edit,1350639744
Control27=IDC_DATETIMEPICKER_TU3,SysDateTimePick32,1342242873
Control28=IDC_DATETIMEPICKER_TU4,SysDateTimePick32,1342242873
Control29=IDC_CPU_USAGE_TU2,edit,1350639744
Control30=IDC_DATETIMEPICKER_TU5,SysDateTimePick32,1342242873
Control31=IDC_DATETIMEPICKER_TU6,SysDateTimePick32,1342242873
Control32=IDC_CPU_USAGE_TU3,edit,1350639744
Control33=IDC_STATIC,static,1342308864
Control34=IDC_STATIC,static,1342308864
Control35=IDC_DATETIMEPICKER_WE1,SysDateTimePick32,1342242873
Control36=IDC_DATETIMEPICKER_WE2,SysDateTimePick32,1342242873
Control37=IDC_CPU_USAGE_WE1,edit,1350639744
Control38=IDC_DATETIMEPICKER_WE3,SysDateTimePick32,1342242873
Control39=IDC_DATETIMEPICKER_WE4,SysDateTimePick32,1342242873
Control40=IDC_CPU_USAGE_WE2,edit,1350639744
Control41=IDC_DATETIMEPICKER_WE5,SysDateTimePick32,1342242873
Control42=IDC_DATETIMEPICKER_WE6,SysDateTimePick32,1342242873
Control43=IDC_CPU_USAGE_WE3,edit,1350639744
Control44=IDC_STATIC,static,1342308864
Control45=IDC_STATIC,static,1342308864
Control46=IDC_DATETIMEPICKER_TH1,SysDateTimePick32,1342242873
Control47=IDC_DATETIMEPICKER_TH2,SysDateTimePick32,1342242873
Control48=IDC_CPU_USAGE_TH1,edit,1350639744
Control49=IDC_DATETIMEPICKER_TH3,SysDateTimePick32,1342242873
Control50=IDC_DATETIMEPICKER_TH4,SysDateTimePick32,1342242873
Control51=IDC_CPU_USAGE_TH2,edit,1350639744
Control52=IDC_DATETIMEPICKER_TH5,SysDateTimePick32,1342242873
Control53=IDC_DATETIMEPICKER_TH6,SysDateTimePick32,1342242873
Control54=IDC_CPU_USAGE_TH3,edit,1350639744
Control55=IDC_STATIC,static,1342308864
Control56=IDC_STATIC,static,1342308864
Control57=IDC_DATETIMEPICKER_FR1,SysDateTimePick32,1342242873
Control58=IDC_DATETIMEPICKER_FR2,SysDateTimePick32,1342242873
Control59=IDC_CPU_USAGE_FR1,edit,1350639744
Control60=IDC_DATETIMEPICKER_FR3,SysDateTimePick32,1342242873
Control61=IDC_DATETIMEPICKER_FR4,SysDateTimePick32,1342242873
Control62=IDC_CPU_USAGE_FR2,edit,1350639744
Control63=IDC_DATETIMEPICKER_FR5,SysDateTimePick32,1342242873
Control64=IDC_DATETIMEPICKER_FR6,SysDateTimePick32,1342242873
Control65=IDC_CPU_USAGE_FR3,edit,1350639744
Control66=IDC_STATIC,static,1342308864
Control67=IDC_STATIC,static,1342308864
Control68=IDC_DATETIMEPICKER_SA1,SysDateTimePick32,1342242873
Control69=IDC_DATETIMEPICKER_SA2,SysDateTimePick32,1342242873
Control70=IDC_CPU_USAGE_SA1,edit,1350639744
Control71=IDC_DATETIMEPICKER_SA3,SysDateTimePick32,1342242873
Control72=IDC_DATETIMEPICKER_SA4,SysDateTimePick32,1342242873
Control73=IDC_CPU_USAGE_SA2,edit,1350639744
Control74=IDC_DATETIMEPICKER_SA5,SysDateTimePick32,1342242873
Control75=IDC_DATETIMEPICKER_SA6,SysDateTimePick32,1342242873
Control76=IDC_CPU_USAGE_SA3,edit,1350639744
Control77=IDC_STATIC,static,1342308864
Control78=IDC_STATIC,static,1342308864
Control79=IDC_DATETIMEPICKER_SU1,SysDateTimePick32,1342242873
Control80=IDC_DATETIMEPICKER_SU2,SysDateTimePick32,1342242873
Control81=IDC_CPU_USAGE_SU1,edit,1350639744
Control82=IDC_DATETIMEPICKER_SU3,SysDateTimePick32,1342242873
Control83=IDC_DATETIMEPICKER_SU4,SysDateTimePick32,1342242873
Control84=IDC_CPU_USAGE_SU2,edit,1350639744
Control85=IDC_DATETIMEPICKER_SU5,SysDateTimePick32,1342242873
Control86=IDC_DATETIMEPICKER_SU6,SysDateTimePick32,1342242873
Control87=IDC_CPU_USAGE_SU3,edit,1350639744
Control88=IDC_STATIC,static,1342308864
Control89=IDC_BATTERY_MODE,button,1342242819
Control90=IDC_STATIC,static,1342308352
Control91=IDC_STATIC,static,1342308864
Control92=IDC_STATIC,static,1342308864
Control93=IDC_STATIC,static,1342308864
Control94=IDC_STATIC,static,1342308864
Control95=IDC_STATIC,static,1342308864
Control96=IDC_STATIC,static,1342308864
Control97=IDC_STATIC,static,1342308864
Control98=IDC_STATIC,static,1342308864
Control99=IDC_STATIC,static,1342308864
Control100=IDC_STATIC,static,1342308864
Control101=IDC_STATIC,static,1342308864
Control102=IDC_STATIC,static,1342308864

[CLS:CPowerPage]
Type=0
HeaderFile=PowerPage.h
ImplementationFile=PowerPage.cpp
BaseClass=CPropertyPage
Filter=D
LastObject=CPowerPage
VirtualFilter=idWC

[DLG:IDD_PROPPAGE_LOGGING (English (U.S.))]
Type=1
Class=LoggingPage
ControlCount=10
Control1=IDC_STATIC,static,1342308864
Control2=IDC_EDIT_EVENT_LOG_FILE,edit,1350631552
Control3=IDC_STATIC,static,1342308864
Control4=IDC_EDIT_EVENT_FORMAT_TIMESTAMP,edit,1350631552
Control5=IDC_STATIC,static,1342308864
Control6=IDC_EDIT_EVENT_LOG_FILE_SIZE,edit,1350631552
Control7=IDC_STATIC,static,1342308864
Control8=IDC_STATIC,static,1342308864
Control9=IDC_EDIT_EVENT_NUMBER_BACKUP,edit,1350639744
Control10=IDC_CHECK_STD_OUTPUT,button,1342242819

[CLS:LoggingPage]
Type=0
HeaderFile=LoggingPage.h
ImplementationFile=LoggingPage.cpp
BaseClass=CPropertyPage
Filter=D
LastObject=ID_CLOSE
VirtualFilter=idWC

[DLG:IDD_DIALOG_LICENSE]
Type=1
Class=CLicenseDlg
ControlCount=7
Control1=IDOK,button,1342242817
Control2=IDCANCEL,button,1342242816
Control3=IDC_STATIC,static,1342308352
Control4=IDC_STATIC,static,1342308352
Control5=IDC_EDIT_LICENSE,edit,1352730692
Control6=IDC_STATIC,static,1342308352
Control7=IDC_STATIC,static,1342308352

[CLS:CLicenseDlg]
Type=0
HeaderFile=LicenseDlg.h
ImplementationFile=LicenseDlg.cpp
BaseClass=CDialog
Filter=D
VirtualFilter=dWC
LastObject=CLicenseDlg

[DLG:IDD_PROPPAGE_SECURITY (English (U.S.))]
Type=1
Class=CSecurityPage
ControlCount=5
Control1=IDC_ENCRYPTS_URL,button,1342242819
Control2=IDC_TRUST_ALL_USER,button,1342242819
Control3=IDC_STATIC,static,1342308352
Control4=IDC_EDIT_TRUSTED_USERS,edit,1350631552
Control5=IDC_STATIC,static,1342308352

[CLS:CSecurityPage]
Type=0
HeaderFile=SecurityPage.h
ImplementationFile=SecurityPage.cpp
BaseClass=CPropertyPage
Filter=D
LastObject=IDC_ENCRYPTS_URL
VirtualFilter=idWC

