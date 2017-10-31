// Saver.cpp : Defines the class behaviors for the application.
//

#include "stdafx.h"
#include "config.h"
#include "Saver.h"
#include "drawwnd.h"
#include "saverwnd.h"
#include "KonfigurationDlg.h"
#include "ControlCenterDlg.h"
#include "LicenseDlg.h"
#include "InstallDlg.h"
#include "zeta_data.h"
#include "service.h"

#include <afxsock.h>
#include <direct.h>
#include <io.h>
#include <fcntl.h>
#include <fstream>
#include <tlhelp32.h>

#ifdef _DEBUG
#undef THIS_FILE
static char BASED_CODE THIS_FILE[] = __FILE__;
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////
// CSaverApp

BEGIN_MESSAGE_MAP(CSaverApp, CWinApp)
	//{{AFX_MSG_MAP(CSaverApp)
		// NOTE - the ClassWizard will add and remove mapping macros here.
		//    DO NOT EDIT what you see in these blocks of generated code!
	//}}AFX_MSG
	ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()

TCHAR szConfig[]=_T("Config");

/////////////////////////////////////////////////////////////////////////////
// CSaverApp construction

CSaverApp::CSaverApp()
{
  _mkdir(Config::ZETAGRID_ROOT_PATH.c_str());
  Config cfg;
  CSocket socket;
  if (socket.Create(cfg.port)) {
    string t = Config::ZETAGRID_ROOT_PATH;
    t += "\\zeta_zeros.tmp";
    remove(t.c_str());
    t = Config::ZETAGRID_ROOT_PATH;
    t += cfg.infoFilename;
    remove(t.c_str());
  }
  generateBasicFiles();
}

void CSaverApp::removeBasicFiles()
{
  // remove startup link
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), (CInstallDlg::IsWindowsNT())? "zeta.cmd" : "zeta.bat");
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), "control.bat");
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), "ZetaGrid Control Center");
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), "ZetaGrid command line mode");

  Config cfg;
  // remove file 'zeta.jar'
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta.jar";
  remove(t.c_str());
  // remove all files zeta_zeros*.tmp
  _finddata_t fileinfo;
  t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros*.tmp";
  long handle = _findfirst(t.c_str(), &fileinfo);
  if (handle != -1) {
    do {
      t = Config::ZETAGRID_ROOT_PATH;
      t += "\\";
      t += fileinfo.name;
      remove(t.c_str());
    } while (_findnext(handle, &fileinfo) != -1);
    _findclose(handle);
  }
  // remove information file, e.g. 'info.log'
  t = Config::ZETAGRID_ROOT_PATH;
  t += "\\";
  t += cfg.infoFilename;
  remove(t.c_str());
  // remove file 'zeta.cmd'
  t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta.cmd";
  remove(t.c_str());
  // remove file 'zeta.bat'
  t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta.bat";
  remove(t.c_str());
}

bool CSaverApp::areBasicFilesValid()
{
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta.jar";
  if (_access(t.c_str(), 0) != 0) {
    return false;
  }
  unsigned int n = 0;
  int fh = _open(t.c_str(), _O_BINARY | _O_RDONLY);
  if (fh != -1) {
    n = _filelength(fh);
    _close( fh );
  }
  if (n != ZETA_DATA_BYTE_SIZE) {
    return false;
  }
  char* buffer = new char[n];
  ifstream fin(t.c_str(), ios::binary);
  fin.read(buffer, n);
  bool result = (strcmp(buffer, (const char*)ZETA_DATA) == 0);
  delete[] buffer;
  return result;
}

void CSaverApp::generateBasicFiles()
{
  if (!areBasicFilesValid()) {
    string t = Config::ZETAGRID_ROOT_PATH;
    t += "\\zeta.jar";
    ofstream fout(t.c_str(), ios::binary);
    fout.write((const char*)ZETA_DATA, ZETA_DATA_BYTE_SIZE);
    fout.close();
  }
}

typedef void (*CEC)(void);
typedef void (*CEC2)(int);

void CSaverApp::exitCalculation(int wait)
{
  CEC callExitCalculation;
  CEC2 callExitCalculation2;
  HINSTANCE local_hLib;

  DWORD len = 1024-15;
  string path = Config::ZETAGRID_ROOT_PATH;
  path += "\\zeta_zeros.dll";   // ToDo: remove code
  local_hLib = LoadLibrary(path.c_str());
  if (local_hLib) {
    callExitCalculation2 = (CEC2)GetProcAddress((HMODULE)local_hLib, "exitCalculation2");
    if (callExitCalculation2) {
      (*callExitCalculation2)(1);
    } else {
      callExitCalculation = (CEC)GetProcAddress((HMODULE)local_hLib, "exitCalculation");
      if (callExitCalculation) {
        (*callExitCalculation)();
      }
    }
    FreeLibrary(local_hLib);
  }

  if (!areBasicFilesValid()) {
    generateBasicFiles();
    if (!areBasicFilesValid()) {
      CString s,s2;
      s.LoadString(IDS_UPDATE_CLIENT);
      s2.LoadString(IDS_WARNING);
      ::MessageBox(NULL, s, s2, MB_OK);
      return;
    }
  }

  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  Config cfg;
  GetStartupInfo(&si);
  si.wShowWindow = SW_MINIMIZE | SW_HIDE;
  SetEnvironmentVariable("PATH", Config::ZETAGRID_ROOT_PATH.c_str());
  SetCurrentDirectory(Config::ZETAGRID_ROOT_PATH.c_str());
  char* c = new char[cfg.call.length()+6];
  strcpy(c, cfg.call.c_str());
  strcat(c, " exit");
  ZeroMemory(&si, sizeof(si));
  si.cb = sizeof(si);
  ZeroMemory(&pi, sizeof(pi));
  int success = CreateProcess(NULL, c, NULL, NULL, FALSE, DETACHED_PROCESS | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
  if (success) {
    WaitForSingleObject(pi.hProcess, INFINITE);
    CloseHandle(pi.hThread);
    CloseHandle(pi.hProcess);
  }
  delete[] c;
  if (wait > 0) {
    Sleep(wait);
  }
}

bool CSaverApp::isControlCenterRunning()
{
  PROCESSENTRY32 pe;
  HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
  if (!hSnapshot) {
    return false;
  }
  pe.dwSize = sizeof(pe);
  int controlCenterRunning = 0;
  for (BOOL i = Process32First(hSnapshot, &pe); i; i = Process32Next(hSnapshot, &pe)) {
    if (strcmp(pe.szExeFile, "zeta.exe") == 0) {
      ++controlCenterRunning;
    } else if (strcmp(pe.szExeFile, "zeta2.exe") == 0) {
      controlCenterRunning += 2;
    }
  }
  CloseHandle(hSnapshot);
  if (controlCenterRunning == 3) {
    CString s,s2;
    s.LoadString(IDS_STOP_CONTROL_CENTER);
    s2.LoadString(IDS_ERROR);
    MessageBox(NULL, s, s2, MB_OK);
    return true;
  }
  return false;
}

/////////////////////////////////////////////////////////////////////////////
// The one and only CSaverApp object

CSaverApp theApp;

BOOL MatchOption(LPTSTR lpsz, LPTSTR lpszOption)
{
  if (lpsz[0] == '-' || lpsz[0] == '/') {
		++lpsz;
  }
  return (lstrcmpi(lpsz, lpszOption) == 0)? TRUE : FALSE;
}

void ChangePassword(HWND hwnd)
{ // This only ever gets called under '95, when started with the /a option.
  HINSTANCE hmpr=::LoadLibrary("MPR.DLL");
  if (hmpr == NULL) {
    //Debug("MPR.DLL not found: cannot change password.");
    return;
  }
  typedef VOID (WINAPI *PWDCHANGEPASSWORD) (LPCSTR lpcRegkeyname,HWND hwnd,UINT uiReserved1,UINT uiReserved2);
  PWDCHANGEPASSWORD PwdChangePassword = (PWDCHANGEPASSWORD)::GetProcAddress(hmpr, "PwdChangePasswordA");
  if (PwdChangePassword == NULL) {
    FreeLibrary(hmpr);
    //Debug("PwdChangeProc not found: cannot change password");
    return;
  }
  PwdChangePassword("SCRSAVE",hwnd,0,0);
  FreeLibrary(hmpr);
}

/////////////////////////////////////////////////////////////////////////////
// CSaverApp initialization

BOOL CSaverApp::InitInstance()
{
	// Standard initialization
	// If you are not using these features and wish to reduce the size
	//  of your final executable, you should remove from the following
	//  the specific initialization routines you do not need.

	Enable3dControls();

	if (__argc == 1) return FALSE;
  if (MatchOption(__argv[1], _T("p"))) {
    CWnd* pParent = CWnd::FromHandle((HWND)atol(__argv[2]));
		ASSERT(pParent != NULL);
		CDrawWnd* pWnd = new CDrawWnd();
		CRect rect;
		pParent->GetClientRect(&rect);
		pWnd->Create(NULL, WS_VISIBLE|WS_CHILD, rect, pParent, NULL);
		m_pMainWnd = pWnd;
		return TRUE;
  } else if (MatchOption(__argv[1], _T("s"))) {
		CSaverWnd* pWnd = new CSaverWnd;
		pWnd->Create();
		m_pMainWnd = pWnd;
		return TRUE;
	}	else if (MatchOption(__argv[1], _T("a")))	{
    ChangePassword(NULL);
    return TRUE;
	}	else if (MatchOption(__argv[1], _T("t")))	{
    CControlCenterDlg::StopComputation();
    Sleep(1000);
    return TRUE;
	}	else if (MatchOption(__argv[1], _T("u")))	{
    CControlCenterDlg::Uninstall();
    return TRUE;
	}	else if (MatchOption(__argv[1], _T("i")))	{
    if (!isControlCenterRunning()) {
      CLicenseDlg lDlg;
      if (lDlg.DoModal() == IDOK) {
        CInstallDlg dlg;
        dlg.DoModal();
      }
    }
    return FALSE;
  } else if (MatchOption(__argv[1], _T("service"))) {
    startService();
  }	else if (MatchOption(__argv[1], _T("control")))	{
    CControlCenterDlg dlg;
    if (__argc == 3 && MatchOption(__argv[2], _T("hide")))	{
      dlg.hide();
    }
    dlg.DoModal();
	  return FALSE;
  }
  KonfigurationDlg dlg;
  dlg.DoModal();
	return FALSE;
}
