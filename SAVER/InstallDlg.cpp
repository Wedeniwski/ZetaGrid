// InstallDlg.cpp : implementation file
//

#include "stdafx.h"
#include "config.h"
#include "saver.h"
#include "service.h"
#include "drawwnd.h"
#include "saverwnd.h"
#include "InstallDlg.h"
#include "KonfigurationDlg.h"
#include "ControlCenterDlg.h"
#include "ProgressDlg.h"
#include "registry.h"

#include <direct.h>
#include <io.h>
#include <winsvc.h>
#include <fstream>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////
// CInstallDlg dialog


CInstallDlg::CInstallDlg(CWnd* pParent /*=NULL*/)
  : CDialog(CInstallDlg::IDD, pParent)
{
  //{{AFX_DATA_INIT(CInstallDlg)
    // NOTE: the ClassWizard will add member initialization here
  //}}AFX_DATA_INIT
}


void CInstallDlg::DoDataExchange(CDataExchange* pDX)
{
  CDialog::DoDataExchange(pDX);
  //{{AFX_DATA_MAP(CInstallDlg)
	DDX_Control(pDX, IDC_CHECK_CMD_CONTROL_CENTER_STARTUP, m_CmdControlCenterStartup);
	DDX_Control(pDX, IDC_CHECK_SERVICE_CONTROL_CENTER_STARTUP, m_ServiceControlCenterStartup);
	DDX_Control(pDX, IDC_CHECK_START_CONTROL_CENTER, m_StartControlCenter);
	DDX_Control(pDX, IDC_CHECK_START_CLIENT, m_StartClient);
	DDX_Control(pDX, IDC_CHECK_SHORTCUT, m_CreateShortcut);
  DDX_Control(pDX, IDC_RADIO_MODE_SCREEN_SAVER, m_ModeScreenSaver);
  DDX_Control(pDX, IDC_RADIO_MODE_SERVICE, m_ModeService);
  DDX_Control(pDX, IDC_RADIO_MODE_COMMAND_LINE, m_ModeCommandLine);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CInstallDlg, CDialog)
  //{{AFX_MSG_MAP(CInstallDlg)
  ON_BN_CLICKED(IDC_RADIO_MODE_SERVICE, OnRadioModeService)
  ON_BN_CLICKED(IDC_RADIO_MODE_SCREEN_SAVER, OnRadioModeScreenSaver)
  ON_BN_CLICKED(IDC_BUTTON_HELP, OnButtonHelp)
  ON_BN_CLICKED(IDC_RADIO_MODE_COMMAND_LINE, OnRadioModeCommandLine)
	ON_BN_CLICKED(IDC_CHECK_SHORTCUT, OnCheckShortcut)
	ON_BN_CLICKED(IDC_CHECK_START_CLIENT, OnCheckStartClient)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CInstallDlg message handlers

void CInstallDlg::OnRadioModeService()
{
  m_ModeService.SetCheck(1);
  m_ModeScreenSaver.SetCheck(0);
  m_ModeCommandLine.SetCheck(0);
  m_CreateShortcut.EnableWindow(FALSE);
  m_StartClient.EnableWindow(FALSE);
  m_ServiceControlCenterStartup.EnableWindow(TRUE);
  m_CmdControlCenterStartup.EnableWindow(FALSE);
  m_StartControlCenter.EnableWindow(TRUE);
}

void CInstallDlg::OnRadioModeScreenSaver()
{
  m_ModeService.SetCheck(0);
  m_ModeScreenSaver.SetCheck(1);
  m_ModeCommandLine.SetCheck(0);
  m_CreateShortcut.EnableWindow(FALSE);
  m_StartClient.EnableWindow(FALSE);
  m_ServiceControlCenterStartup.EnableWindow(FALSE);
  m_CmdControlCenterStartup.EnableWindow(FALSE);
  m_StartControlCenter.SetCheck(0);
  m_StartControlCenter.EnableWindow(FALSE);
}

void CInstallDlg::OnRadioModeCommandLine()
{
  m_ModeService.SetCheck(0);
  m_ModeScreenSaver.SetCheck(0);
  m_ModeCommandLine.SetCheck(1);
  m_CreateShortcut.EnableWindow(TRUE);
  m_StartClient.EnableWindow(TRUE);
  m_ServiceControlCenterStartup.EnableWindow(FALSE);
  m_CmdControlCenterStartup.EnableWindow(TRUE);
  m_StartControlCenter.EnableWindow(TRUE);
}

void CInstallDlg::OnCheckShortcut() 
{
  //m_CreateShortcut.SetCheck((m_CreateShortcut.GetCheck()+1)&1);
}

void CInstallDlg::OnCheckStartClient() 
{
  //m_StartClient.SetCheck((m_StartClient.GetCheck()+1)&1);
}

BOOL CInstallDlg::OnInitDialog()
{
  CDialog::OnInitDialog();
  OnRadioModeScreenSaver();
  m_ModeService.EnableWindow(IsWindowsNT());
  m_CreateShortcut.SetCheck(1);
  m_StartClient.SetCheck(1);
  m_ServiceControlCenterStartup.SetCheck(1);
  m_CmdControlCenterStartup.SetCheck(1);
  m_StartControlCenter.SetCheck(0);
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta2.exe";
  if (_access(t.c_str(), 0)) {
    m_StartControlCenter.EnableWindow(FALSE);
  } else {
    m_StartControlCenter.EnableWindow(TRUE);
  }
  return TRUE;  // return TRUE unless you set the focus to a control
                // EXCEPTION: OCX Property Pages should return FALSE
}

BOOL CInstallDlg::StartupService()
{
  BOOL bRet = FALSE;
  SC_HANDLE schSCManager = ::OpenSCManager(
                0,            // machine (NULL == local)
                0,            // database (NULL == default)
                SC_MANAGER_ALL_ACCESS // access required
              );
  if (schSCManager) {
    SC_HANDLE schService = ::OpenService(schSCManager, SZSERVICENAME, SERVICE_ALL_ACCESS);
    if (schService) {
      if (::StartService(schService, 0, 0)) {
        Sleep(1000);
        SERVICE_STATUS m_ssStatus;
        while (::QueryServiceStatus(schService, &m_ssStatus)) {
          if (m_ssStatus.dwCurrentState == SERVICE_START_PENDING) {
            Sleep(1000);
          } else {
            break;
          }
        }
        if (m_ssStatus.dwCurrentState == SERVICE_RUNNING) {
          bRet = TRUE;
        }
      }
      ::CloseServiceHandle(schService);
    }
    ::CloseServiceHandle(schSCManager);
  }
  return bRet;
}

void CInstallDlg::RestartService()
{
  CSaverApp::exitCalculation(2000);
  //ServiceStop();
  //StartupService();
  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  char c[100];
  strcpy(c, "net stop ");
  strcat(c, SZSERVICENAME);
  GetStartupInfo(&si);
  ZeroMemory(&si, sizeof(si));
  si.wShowWindow = SW_MINIMIZE | SW_HIDE;
  si.cb = sizeof(si);
  ZeroMemory(&pi, sizeof(pi));
  int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
  if (success) {
    CloseHandle(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);
    CloseHandle(pi.hProcess);
    StartupService();
  }
/*  strcpy(c, "net stop ");
  strcat(c, SZSERVICENAME);
  GetStartupInfo(&si);
  ZeroMemory(&si, sizeof(si));
  si.wShowWindow = SW_MINIMIZE | SW_HIDE;
  si.cb = sizeof(si);
  ZeroMemory(&pi, sizeof(pi));
  int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
  if (success) {
    CloseHandle(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);
    CloseHandle(pi.hProcess);
    strcpy(c, "net start ZetaGrid");
    GetStartupInfo(&si);
    ZeroMemory(&si, sizeof(si));
    si.wShowWindow = SW_MINIMIZE | SW_HIDE;
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));
    success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
    if (success) {
      CloseHandle(pi.hThread);
      WaitForSingleObject(pi.hProcess, INFINITE);
      CloseHandle(pi.hProcess);
    }
  }*/
}

void CInstallDlg::ReinstallService(const Config& cfg)
{
  CProgressDlg progress(6);
  CSaverApp::exitCalculation(2000);
  progress.Step();
  ServiceStop();
  progress.Step();
  removeService();
  progress.Step();
  char c[2000];
  strcpy(c, "ZetaGrid -i -Dsun.net.inetaddr.ttl=0 -Dnetworkaddress.cache.ttl=0 -Dnetworkaddress.cache.negative.ttl=0 -Xmx128m -Djava.class.path=zeta.jar;zeta_client.jar");
  if (cfg.proxyHost.length() > 0) {
    strcat(strcat(c, " -Dhttp.proxyHost="), cfg.proxyHost.c_str());
  }
  if (cfg.proxyPort.length() > 0) {
    strcat(strcat(c, " -Dhttp.proxyPort="), cfg.proxyPort.c_str());
  }
  strcat(c, " wrkdir=\"");
  strcat(c, Config::ZETAGRID_ROOT_PATH.c_str());
  strcat(c, "\" libdir=\"");
  strcat(c, cfg.javaLib.c_str());
  strcat(c, "\" info=\"");
  strcat(c, cfg.rootPath.c_str());
  strcat(c, "\\");
  strcat(c, cfg.infoFilename.c_str());
  strcat(c, "\" prio=");
  strcat(c, cfg.priority.c_str());
  //strcat(c, " exit");
  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  ZeroMemory(&si, sizeof(si));
  GetStartupInfo(&si);
  si.wShowWindow = SW_MINIMIZE | SW_HIDE;
  //si.wShowWindow = SW_SHOW | SW_SHOWNORMAL;
  si.cb = sizeof(si);
  ZeroMemory(&pi, sizeof(pi));
  progress.Step();
  int success = CreateProcess(NULL, c, NULL, NULL, FALSE, DETACHED_PROCESS | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
  if (success) {
    CloseHandle(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);
    CloseHandle(pi.hProcess);
  }
  progress.Step();
  StartupService();
  progress.Step();
  progress.Destroy();
}

void CInstallDlg::StartInstall()
{
  CProgressDlg progress(3);
  // Stop calculation
  CSaverApp::exitCalculation(2000);
  CSaverApp::removeBasicFiles();
  CSaverApp::generateBasicFiles();
  progress.Step();
  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  ServiceStop();
  progress.Step();
  removeService();
  progress.Step();
  progress.Destroy();
  // check if first installation
  _finddata_t fileinfo;
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros_*.txt";
  bool firstInstallation = true;
  long handle = _findfirst(t.c_str(), &fileinfo);
  if (handle != -1) {
    firstInstallation = false;
  }
  KonfigurationDlg dlg(NULL, firstInstallation);
  if (dlg.DoModal() == IDCANCEL) {
    CString s,s2;
    s.LoadString(IDS_INSTALL_CONTINUE);
    s2.LoadString(IDS_INSTALL);
    if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDNO) {
      return;
    }
  }
  Config cfg;
  // ToDo: simple proxy detection
  /*if (cfg.proxyHost.length() == 0) {
    GetProxyName();
  }*/
  if (cfg.mode == Config::service) {
    ReinstallService(cfg);
    CString path = GetStartupPath();
    if (path.GetLength() > 0) {
      if (m_ServiceControlCenterStartup.GetCheck() != 0) {
        string t = Config::ZETAGRID_ROOT_PATH;
        t += "\\zeta.exe";
        if (!CreateLink(path, "control.bat", "-hide", "ZetaGrid Control Center", t.c_str(), 0, Config::ZETAGRID_ROOT_PATH.c_str())) {
          CString s,s2;
          s.LoadString(IDS_ERROR);
          s2.LoadString(IDS_INSTALL);
          MessageBox(s, s2, MB_OK);
        }
      }
    }
  } else if (cfg.mode == Config::screensaver) {
    SetCurrentDirectory(Config::ZETAGRID_ROOT_PATH.c_str());
    ZeroMemory(&si, sizeof(si));
    GetStartupInfo(&si);
    //si.wShowWindow = SW_MINIMIZE | SW_HIDE;
    si.wShowWindow = SW_SHOW | SW_SHOWNORMAL;
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));
    char* c = new char[Config::ZETAGRID_ROOT_PATH.length()+50];
    strcpy(c, "rundll32.exe desk.cpl,InstallScreenSaver ");
    strcat(c, Config::ZETAGRID_ROOT_PATH.c_str());
    strcat(c, "\\zeta.scr");
    int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
    if (success) {
      CloseHandle(pi.hThread);
      WaitForSingleObject(pi.hProcess, INFINITE);
      CloseHandle(pi.hProcess);
    }
    delete[] c;
  } else if (cfg.mode == Config::commandline) {
    CString path = GetStartupPath();
    if (path.GetLength() > 0) {
      if (m_CreateShortcut.GetCheck() != 0) {
        string t = Config::ZETAGRID_ROOT_PATH;
        t += "\\zeta.exe";
        if (!CreateLink(path, (IsWindowsNT())? "zeta.cmd" : "zeta.bat", "", "ZetaGrid command line mode", t.c_str(), 0, Config::ZETAGRID_ROOT_PATH.c_str())) {
          CString s,s2;
          s.LoadString(IDS_ERROR);
          s2.LoadString(IDS_INSTALL);
          MessageBox(s, s2, MB_OK);
        }
      }
      if (m_CmdControlCenterStartup.GetCheck() != 0) {
        string t = Config::ZETAGRID_ROOT_PATH;
        t += "\\zeta.exe";
        if (!CreateLink(path, "control.bat", "-hide", "ZetaGrid Control Center", t.c_str(), 0, Config::ZETAGRID_ROOT_PATH.c_str())) {
          CString s,s2;
          s.LoadString(IDS_ERROR);
          s2.LoadString(IDS_INSTALL);
          MessageBox(s, s2, MB_OK);
        }
      }
    }
    if (m_StartClient.GetCheck() != 0) {
      //CString s,s2;
      //s.LoadString(IDS_START_COMMAND_LINE_CLIENT);
      //s2.LoadString(IDS_START_CLIENT);
      //if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
      PROCESS_INFORMATION pi;
      STARTUPINFO si;
      ZeroMemory(&si, sizeof(si));
      GetStartupInfo(&si);
      si.wShowWindow = SW_SHOWMAXIMIZED | SW_SHOWNORMAL;
      si.cb = sizeof(si);
      ZeroMemory(&pi, sizeof(pi));
      char* c = new char[Config::ZETAGRID_ROOT_PATH.length()+10];
      strcpy(c, Config::ZETAGRID_ROOT_PATH.c_str());
      strcat(c, (IsWindowsNT())? "\\zeta.cmd" : "\\zeta.bat");
      int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
      if (success) {
        CloseHandle(pi.hThread);
      }
    }
  }
  ShowWindow(SW_RESTORE | SW_SHOW | SW_SHOWNORMAL);
  BringWindowToTop();
  if (m_StartControlCenter.GetCheck() != 0) {
    //string t = Config::ZETAGRID_ROOT_PATH;
    //t += "\\zeta2.exe";
    //if (_access(t.c_str(), 0)) {
    //CString s,s2;
    //s.LoadString(IDS_INSTALL_START_CONTROL_CENTER);
    //s2.LoadString(IDS_INSTALL_SUCCESSFUL);
    //if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
      PROCESS_INFORMATION pi;
      STARTUPINFO si;
      ZeroMemory(&si, sizeof(si));
      GetStartupInfo(&si);
      si.wShowWindow = SW_SHOWMAXIMIZED | SW_SHOWNORMAL;
      si.cb = sizeof(si);
      ZeroMemory(&pi, sizeof(pi));
      char* c = new char[Config::ZETAGRID_ROOT_PATH.length()+20];
      strcpy(c, Config::ZETAGRID_ROOT_PATH.c_str());
      strcat(c, "\\control.bat");
      int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
      if (success) {
        CloseHandle(pi.hThread);
        WaitForSingleObject(pi.hProcess, INFINITE);
        CloseHandle(pi.hProcess);
      }
      delete[] c;
    //}
  } else {
    CString s,s2;
    s.LoadString(IDS_INSTALL_COMPLETED_SUCCESSFULLY);
    s2.LoadString(IDS_INSTALL_SUCCESSFUL);
    MessageBox(s, s2, MB_ICONINFORMATION | MB_OK);
  }
}

void CInstallDlg::OnButtonHelp()
{
  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  GetStartupInfo(&si);
  si.wShowWindow = SW_SHOWNORMAL;
  SetEnvironmentVariable("PATH", Config::ZETAGRID_ROOT_PATH.c_str());
  SetCurrentDirectory(Config::ZETAGRID_ROOT_PATH.c_str());
  si.cb = sizeof(si);
  char* c = new char[Config::ZETAGRID_ROOT_PATH.length()+50];
  strcpy(c, "notepad.exe ");
  strcat(c, Config::ZETAGRID_ROOT_PATH.c_str());
  strcat(c, "\\install_en.txt");
  int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
  if (success) {
    CloseHandle(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);
    CloseHandle(pi.hProcess);
  }
  delete[] c;
}

void CInstallDlg::OnOK()
{
  Config cfg;
  if (m_ModeScreenSaver.GetCheck() != 0) {
    cfg.mode = Config::screensaver;
  } else if (m_ModeService.GetCheck() != 0) {
    cfg.mode = Config::service;
  } else {
    cfg.mode = Config::commandline;
  }
  cfg.save(false);
  ShowWindow(SW_HIDE);
  StartInstall();
  CDialog::OnOK();
}

bool CInstallDlg::IsWindowsNT()
{
  return (GetVersion() < 0x80000000);
  /*OSVERSIONINFO version;
  version.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
  ::GetVersionEx(&version);
  return (version.dwPlatformId == VER_PLATFORM_WIN32_NT);*/
}

CString CInstallDlg::GetStartupPath()
{
  LPITEMIDLIST pidl;
  if (SUCCEEDED(SHGetSpecialFolderLocation(NULL, CSIDL_STARTUP, &pidl))) {
    char path[_MAX_PATH];
    SHGetPathFromIDList(pidl, path);
    LPMALLOC pMalloc;
    if (SUCCEEDED(SHGetMalloc(&pMalloc))) {
      pMalloc->Free(pidl);
      pMalloc->Release();
      return path;
    }
  }
  return "";
}

void CInstallDlg::RemoveLink(const char* folderPath, const char* linkName)
{
  char linkPath[_MAX_PATH];
  _makepath(linkPath, 0, folderPath, linkName, ".lnk");
  if (_access(linkPath, 0) == 0) {
    _unlink(linkPath);
  }
}

bool CInstallDlg::CreateLink(const char* folderPath, const char* linkName, const char* linkArgs, const char* linkDescription, const char* iconPath, int iconIndex, const char* workingDirectory)
{
  char linkPath[_MAX_PATH];
  RemoveLink(folderPath, linkName);
  if (linkDescription) {
    RemoveLink(folderPath, linkDescription);
    _makepath(linkPath, 0, folderPath, linkDescription, ".lnk");
  } else {
    _makepath(linkPath, 0, folderPath, linkName, ".lnk");
  }
  bool successful = false;
  if (SUCCEEDED(CoInitialize(NULL))) {
    IShellLink* pLink;
    if (SUCCEEDED(CoCreateInstance(CLSID_ShellLink, 0, CLSCTX_INPROC_SERVER, IID_IShellLink, reinterpret_cast<PVOID*>(&pLink)))) {
      pLink->SetPath(CString(workingDirectory) + "\\" + linkName);
      if (linkArgs) {
        pLink->SetArguments(linkArgs);
      }
      pLink->SetShowCmd(SW_SHOW);
      if (linkDescription) {
        pLink->SetDescription(linkDescription);
      }
      if (iconIndex >= 0) {
        pLink->SetIconLocation(iconPath, iconIndex);
      }
      if (workingDirectory) {
        pLink->SetWorkingDirectory(workingDirectory);
      }
      IPersistFile* pPersistFile;
      if (SUCCEEDED(pLink->QueryInterface(IID_IPersistFile, reinterpret_cast<PVOID*>(&pPersistFile)))) {
#ifndef UNICODE
        WCHAR path[_MAX_PATH] = { 0 };
        MultiByteToWideChar(CP_ACP, 0, linkPath, static_cast<int>(strlen(linkPath)), path, static_cast<int>(_MAX_PATH));
        successful = SUCCEEDED(pPersistFile->Save(path, TRUE));
#else
        successful = SUCCEEDED(pPersistFile->Save(linkPath, TRUE));
#endif
        pPersistFile->Release();
      }
      pLink->Release();
    }
    CoUninitialize();
  }
  return successful;
}

CString CInstallDlg::GetProxyName()
{
  // ToDo: check
  unsigned char proxyName[1025];
  LONG l = 1024;
  if (getStringValue(proxyName, (LPDWORD)&l, HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyEnable") == 0) {
    if (strcmp((char*)proxyName, "1") == 0 && getStringValue(proxyName, (LPDWORD)&l, HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyServer") == 0) {
      return proxyName;
    }
  }
  return "";
}
