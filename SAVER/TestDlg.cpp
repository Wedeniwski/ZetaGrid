// TestDlg.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "drawwnd.h"
#include "saverwnd.h"
#include "config.h"
#include "TestDlg.h"
#include "PortDlg.h"
#include "KonfigurationDlg.h"

#include <afxinet.h>
#include <fstream>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////
// CTestDlg dialog


CTestDlg::CTestDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CTestDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CTestDlg)
		// NOTE: the ClassWizard will add member initialization here
	//}}AFX_DATA_INIT
  Config cfg;
  bool retry = false;
  cancel = false;
  do {
    retry = false;
    TRY {
      CInternetSession checkConnection(NULL, 1, (cfg.proxyHost.length() == 0)? INTERNET_OPEN_TYPE_PRECONFIG : INTERNET_OPEN_TYPE_PROXY, (cfg.proxyHost.length() == 0)? NULL : cfg.proxyHost.c_str());
      CString url;
      url.Format("http://%s:%d/index.html", cfg.hostName.c_str(), cfg.hostPort);
      CStdioFile* connection = checkConnection.OpenURL(url, 1, INTERNET_FLAG_TRANSFER_ASCII | INTERNET_FLAG_DONT_CACHE);
      if (connection == NULL) {
        CString s,s2;
        s.LoadString(IDS_MUST_BE_ONLINE);
        s2.LoadString(IDS_CONNECTION);
        if (MessageBox(s, s2, MB_ICONQUESTION | MB_RETRYCANCEL) == IDCANCEL) {
          cancel = true;
        } else {
          retry = true;
        }
      }
    } CATCH (CInternetException, e) {
      char cause[255];
      CString s,s2;
      e->GetErrorMessage(cause, 255);
      s.LoadString(IDS_MUST_BE_ONLINE);
      s += "\n\n";
      s += cause;
      s2.LoadString(IDS_CONNECTION);
      if (MessageBox(s, s2, MB_ICONQUESTION | MB_RETRYCANCEL) == IDCANCEL) {
        cancel = true;
      } else {
        retry = true;
      }
    } AND_CATCH_ALL (e) {
      char cause[255];
      CString s,s2;
      e->GetErrorMessage(cause, 255);
      s.LoadString(IDS_MUST_BE_ONLINE);
      s += "\n(";
      s += cause;
      s += ')';
      s2.LoadString(IDS_CONNECTION);
      if (MessageBox(s, s2, MB_ICONQUESTION | MB_RETRYCANCEL) == IDCANCEL) {
        cancel = true;
      } else {
        retry = true;
      }
    } END_CATCH_ALL
  } while (retry);

  if (!cancel) {
    CString s,s2;
    s.LoadString(IDS_TEST_INFO);
    s2.LoadString(IDS_CONNECTION);
    MessageBox(s, s2, MB_ICONINFORMATION | MB_OK);
  }
}


void CTestDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CTestDlg)
	DDX_Control(pDX, IDC_STATUS, m_CurrentStatus);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CTestDlg, CDialog)
	//{{AFX_MSG_MAP(CTestDlg)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CTestDlg message handlers

BOOL CTestDlg::Create(const char* title) 
{
	BOOL result = CDialog::Create(IDD);
  if (result) {
    m_CurrentStatus.SetWindowText(title);
    BringWindowToTop();
    if (AfxGetApp()) SetCursor(AfxGetApp()->LoadStandardCursor(IDC_WAIT));
  }
  return result;
}

void CTestDlg::Start()
{
  Config cfg;
  SetCurrentDirectory(Config::ZETAGRID_ROOT_PATH.c_str());

  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  SetEnvironmentVariable("PATH", Config::ZETAGRID_ROOT_PATH.c_str());
  SetCurrentDirectory(Config::ZETAGRID_ROOT_PATH.c_str());
  char* c = new char[cfg.call.length()+Config::ZETAGRID_ROOT_PATH.length()+1+59];
  strcpy(c, "cmd.exe /c \"");
  strcat(c, cfg.call.c_str());
  strcat(c, " 2> ");
  strcat(c, Config::ZETAGRID_ROOT_PATH.c_str());
  strcat(c, "\\std.out\"");

  string infoTmp = Config::ZETAGRID_ROOT_PATH;
  infoTmp += "\\";
  infoTmp += cfg.infoFilename;
  char alreadyRunning = 0;
  for (int tries = 0; tries < 3; ++tries) {
    remove(infoTmp.c_str());
    string stdOut = Config::ZETAGRID_ROOT_PATH;
    stdOut += "\\std.out";
    remove(stdOut.c_str());
    GetStartupInfo(&si);
    ZeroMemory(&si, sizeof(si));
    si.wShowWindow = SW_SHOWNORMAL;
    ZeroMemory(&pi, sizeof(pi));
    int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
    if (success) {
      CloseHandle(pi.hThread);
      while (WaitForSingleObject(pi.hProcess, 1000) == WAIT_TIMEOUT) {
        ifstream fInfo(infoTmp.c_str());
        if (!fInfo || fInfo.eof()) {
          fInfo.close();
          CString s;
          m_CurrentStatus.GetWindowText(s);
          s = s.Left(s.Find(':')+1);
          s += " Not active";
          m_CurrentStatus.SetWindowText(s);
        } else {
          char c[101];
          fInfo.getline(c, 100);
          fInfo.close();
          CString s;
          m_CurrentStatus.GetWindowText(s);
          s = s.Left(s.Find(':')+1);
          s += " ";
          s += c;
          m_CurrentStatus.SetWindowText(s);
          if (strncmp(c, "Computing on", 12) == 0) {
            CSaverApp::exitCalculation(2000);
          }
        }
      }
      WaitForSingleObject(pi.hProcess, INFINITE);
      CloseHandle(pi.hProcess);
    }
    ifstream fin(stdOut.c_str());
    char ch;
    if (fin.good() && !fin.eof() && fin.get(ch)) {
      if (alreadyRunning == 1 && ch != 'E') {
        alreadyRunning = 2;
      }
      if (tries == 2 || ch != 'A') {
        string s;
        getline(fin, s);
        if (ch == 'E' && s.find("The program is already running or the port number ") != string::npos) {
          if (tries != 2) {
            CString s("The program is already running or the port number ");
            if (KonfigurationDlg::running != 0) {
              char c[20];
              s += itoa(KonfigurationDlg::running->cfg.port, c, 10);
            }
            s += " is not available (can be changed in the configuration).\nDo you want to stop the current running client?";
            CString s2;
            s2.LoadString(IDS_ERROR_OCCURRED);
            if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
              CSaverApp::exitCalculation(2000);
              fin.close();
              Sleep(10000);
            }
            alreadyRunning = 1;
          } else {
            PortDlg dlg;
            dlg.DoModal();
            alreadyRunning = 0;
          }
        } else if (ch == 'A') {
          CString s,s2;
          s.LoadString(IDS_SERVER_ERROR_OCCURRED);
          s2.LoadString(IDS_ERROR_OCCURRED);
          MessageBox(s, s2, MB_ICONERROR | MB_OK);
          //MessageBox("An error occured.\n\nNote: First time there may occur the error \"java.lang.ClassNotFoundException: zeta.ZetaCalc\" (depends on the version of the Java VM) which will not occur at second test.", "Error occured!", MB_ICONERROR | MB_OK);
        } else {
          CString s,s2;
          if (cfg.mode == Config::service) {
            s.LoadString(IDS_ERROR_OCCURRED_SEE_DETAILS_SERVICE);
          } else {
            s.LoadString(IDS_ERROR_OCCURRED_SEE_DETAILS);
          }
          s2.LoadString(IDS_ERROR_OCCURRED);
          if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
            strcpy(c, "notepad.exe ");
            strcat(c, Config::ZETAGRID_ROOT_PATH.c_str());
            strcat(c, "\\std.out");
            si.wShowWindow = SW_SHOWNORMAL;
            ZeroMemory(&pi, sizeof(pi));
            int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
            if (success) {
              CloseHandle(pi.hThread);
              WaitForSingleObject(pi.hProcess, INFINITE);
              CloseHandle(pi.hProcess);
            }
          }
        }
        break;
      }
    } else {
      CString s,s2;
      s.LoadString(IDS_SUCCESSFUL);
      MessageBox(s, s);
      if (alreadyRunning == 1) {
        alreadyRunning = 0;
      }
      break;
    }
    Sleep(1000);
  }
  if (alreadyRunning == 2) {
    CString s,s2;
    s.LoadString(IDS_ALREADY_RUNNING);
    s2.LoadString(IDS_WARNING);
    MessageBox(s, s2, MB_ICONWARNING | MB_OK);
  } else if (alreadyRunning == 1) {
    PortDlg dlg;
    dlg.DoModal();
  }
  delete[] c;
}

void CTestDlg::Destroy() 
{
  DestroyWindow();
  if (AfxGetApp()) SetCursor(AfxGetApp()->LoadStandardCursor(IDC_ARROW));
}

BOOL CTestDlg::OnInitDialog() 
{
	CDialog::OnInitDialog();

	return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}
