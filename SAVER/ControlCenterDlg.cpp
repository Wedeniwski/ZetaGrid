// ControlCenterDlg.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "drawwnd.h"
#include "saverwnd.h"
#include "ControlCenterDlg.h"
#include "KonfigurationDlg.h"
#include "TestDlg.h"
#include "InstallDlg.h"
#include "AboutDlg.h"
#include "service.h"

#include <afxsock.h>
#include <afxinet.h>
#include <direct.h>
#include <time.h>
#include <fcntl.h>
#include <stdio.h>
#include <io.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fstream>
#include <ctype.h>
#include <math.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

#define	WM_ICON_NOTIFY			WM_USER+10

using namespace std;

/////////////////////////////////////////////////////////////////////////////
// CControlCenterDlg dialog


bool CControlCenterDlg::transferEnabled = true;
CControlCenterDlg* CControlCenterDlg::running = NULL;

char x2c(char *what) {
  char digit = (what[0] >= 'A' ? ((what[0] & 0xdf) - 'A')+10 : (what[0] - '0'));
  digit *= 16;
  digit += (what[1] >= 'A' ? ((what[1] & 0xdf) - 'A')+10 : (what[1] - '0'));
  return digit;
}

void unescape_url(char *url) {
  for (int x = 0, y = 0; url[y]; ++x, ++y) {
    if ((url[x] = url[y]) == '%') {
      url[x] = x2c(&url[y+1]);
      y += 2;
    }
  }
  url[x] = '\0';
}

CString escape_url(const char *url)
{
  const char* hex = "0123456789abcdef";
  CString result = "";
  for (; *url != '\0'; ++url) {
    char c = *url;
    if (isalnum(c) || c == '-' || c == '_' || c == '.' || c == '*') {
      result += c;
    } else if (c == ' ') {
      result += '+';
    } else {
      result += '%';
      result += hex[(c >> 4) & 15];
      result += hex[c & 15];
    }
  }
  return result;
}

CControlCenterDlg::CControlCenterDlg(CWnd* pParent /*=NULL*/)
  : CDialog(CControlCenterDlg::IDD, pParent)
{
  //{{AFX_DATA_INIT(CControlCenterDlg)
	//}}AFX_DATA_INIT
  updateView = true;
  hideDlg = false;
  lastConfig = new Config();
  systemTrayIsActive = lastConfig->systemTray;
  CSocket socket;
  clientIsActive = !socket.Create(lastConfig->port);
  clientStopped = false;
  AfxSocketInit();
  running = this;
}

CControlCenterDlg::~CControlCenterDlg()
{
  running = NULL;
}

void CControlCenterDlg::DoDataExchange(CDataExchange* pDX)
{
  CDialog::DoDataExchange(pDX);
  //{{AFX_DATA_MAP(CControlCenterDlg)
	DDX_Control(pDX, IDC_STATIC_ACTIVE_THREADS, m_ActiveThreads);
	DDX_Control(pDX, IDC_BUTTON_HIDE, m_HideButton);
	DDX_Control(pDX, IDC_STATIC_REMAINING, m_Remaining);
	DDX_Control(pDX, IDC_STATIC_THREAD_ID, m_ThreadIDText);
	DDX_Control(pDX, IDC_COMBO_THREAD_ID, m_ThreadID);
	DDX_Control(pDX, IDC_STATIC_PERFORMANCE, m_Performance);
  DDX_Control(pDX, IDC_STATIC_PROGRESS, m_ProgressText);
  DDX_Control(pDX, IDC_PROGRESS_ACTIVE, m_Active);
  DDX_Control(pDX, IDC_STATIC_PERCENT, m_Percent);
  DDX_Control(pDX, IDC_BUTTON_TRANSFER_WU, m_TransferButton);
  DDX_Control(pDX, IDC_STATIC_CURRENT_STATUS, m_CurrentStatus);
  DDX_Control(pDX, IDC_STATIC_LOCAL_PACKAGES, m_LocalWorkUnits);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CControlCenterDlg, CDialog)
  //{{AFX_MSG_MAP(CControlCenterDlg)
  ON_BN_CLICKED(IDC_BUTTON_TRANSFER_WU, OnButtonTransferWu)
  ON_WM_TIMER()
  ON_COMMAND(ID_CONFIG_CONFIG, OnConfigConfig)
  ON_COMMAND(ID_WEB_HOME, OnWebHome)
  ON_COMMAND(ID_WEB_NEWS, OnWebNews)
  ON_COMMAND(ID_WEB_STATISTIC, OnWebStatistic)
  ON_COMMAND(ID_LOG_VIEW_INFORMATION_LOG, OnLogViewInformationLog)
	ON_COMMAND(ID_HELP_ABOUT, OnHelpAbout)
	ON_COMMAND(ID_HELP_FAQ, OnHelpFaq)
	ON_CBN_SELCHANGE(IDC_COMBO_THREAD_ID, OnSelchangeComboThreadId)
	ON_COMMAND(ID_HELP_FORUM, OnHelpForum)
	ON_COMMAND(ID_WEB_STATISTIC_DETAIL, OnWebStatisticDetail)
	ON_COMMAND(ID_WEB_TEAMMEMBERS, OnWebTeammembers)
	ON_BN_CLICKED(IDC_BUTTON_HIDE, OnButtonHide)
	ON_COMMAND(ID_CLOSE, OnClose)
	ON_COMMAND(ID_VIEW, OnView)
	ON_COMMAND(ID_SERVER_CONFIG, OnServerConfig)
  ON_COMMAND(IDD_ACTIONS_RESTART_COMPUTATION, OnRestartComputation)
  ON_COMMAND(IDD_ACTIONS_STOP_COMPUTATION, OnStopComputation)
	ON_WM_PAINT()
  ON_COMMAND(IDD_ACTIONS_TRANSFER_WORK_UNITS, OnButtonTransferWu)
  ON_COMMAND(ID_RESTART_COMPUTATION, OnRestartComputation)
  ON_COMMAND(ID_STOP_COMPUTATION, OnStopComputation)
	//}}AFX_MSG_MAP
  ON_MESSAGE(WM_ICON_NOTIFY, OnTrayNotification)
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CControlCenterDlg message handlers

static double InnerLoopFactor(double n)
{
  if (n < 2) {
    n = 2;
  }
  double t2,t1 = n/log(n);
  double d = n + 0.125;
  do {
    t2 = (t1+d)/log(t1);
    if (fabs(t1-t2) < t2*1e-13) {
      return sqrt(t2)/1000.0;
    }
    t1 = (t2+d)/log(t2);
  } while (fabs(t1-t2) >= t1*1e-13);
  return sqrt(t1)/1000.0;
}

LRESULT CControlCenterDlg::OnTrayNotification(WPARAM uID, LPARAM lEvent)
{
  if (m_TrayIcon.OnTrayNotification(uID, lEvent) != 0L) {
    if (LOWORD(lEvent) == WM_RBUTTONUP) {
      CMenu menu;
      if (!menu.LoadMenu(IDR_POPUP_MENU)) return 0;
      CMenu* pSubMenu = menu.GetSubMenu(0);
      if (!pSubMenu) return 0;

      // Make chosen menu item the default (bold font)
      ::SetMenuDefaultItem(pSubMenu->m_hMenu, ID_VIEW, 0);

      // Display and track the popup menu
      CPoint pos;
      GetCursorPos(&pos);

      SetForegroundWindow();  
      ::TrackPopupMenu(pSubMenu->m_hMenu, 0, pos.x, pos.y, 0, GetSafeHwnd(), NULL);

      // BUGFIX: See "PRB: Menus for Notification Icons Don't Work Correctly"
      PostMessage(WM_NULL, 0, 0);

      menu.DestroyMenu();
    } else if (LOWORD(lEvent) == WM_LBUTTONDBLCLK) {
      // double click received, the default action is to execute default menu item
      ShowWindow(SW_SHOWNORMAL);
      SetForegroundWindow();
      return 1;
    }
  }
  return 0L;
}

void CControlCenterDlg::show()
{
  hideDlg = false;
}
  
void CControlCenterDlg::hide()
{
  hideDlg = true;
}

void CControlCenterDlg::OnButtonRefresh() 
{
  static clock_t firstTimePos = 0;
  static clock_t lastTimePos = 0;
  static CString currentWorkUnit;
  static CString lastLastUpdate;
  static int firstPos = 0;
  static int lastPos = 0;
  static bool prevNoInfo = false;
  static CString cInfo = "";

  // synchronize with State::update
  // CControlCenterDlg::OnButtonRefresh() is master
  string infoFile = Config::ZETAGRID_ROOT_PATH;
  infoFile += "\\";
  infoFile += lastConfig->infoFilename;
  ifstream fInfo(infoFile.c_str());
  bool noInfo = false;
  if (!fInfo || fInfo.eof()) {
    fInfo.close();
    noInfo = true;
  } else {
    char c[101];
    CString s;
    fInfo.getline(c, 100);
    fInfo.close();
    m_CurrentStatus.GetWindowText(s);
    s = s.Left(s.Find(':')+1);
    s += " ";
    s += c;
    cInfo = (c[0] == ' ' && c[1] == '\0')? c : "";
    cInfo.TrimLeft();
    m_CurrentStatus.SetWindowText(s);
    if (!m_CurrentStatus.IsWindowVisible() && cInfo.GetLength() > 0 && cInfo[0] != 'C') {
      m_Remaining.GetWindowText(s);
      if (s.GetLength() == 0 || s[0] == ' ') {
        s = "    (";
        s += cInfo;
        s += ")";
        m_Remaining.SetWindowText(s);
      }
    }
    updateView = true;
  }

  struct _stat buf;
  CString lastUpdate(" last update ");
  CString filename = getThreadIDs();
  int fh = _open(filename, _O_RDONLY);
  if (fh != -1) {
    _fstat(fh, &buf);
    _close(fh);
    lastUpdate += ctime(&buf.st_mtime);
  } else if (noInfo && prevNoInfo) {
    if (!clientIsActive && !clientStopped && lastConfig->checkBatteryMode) {
      SYSTEM_POWER_STATUS sps;
      if (GetSystemPowerStatus(&sps)) {
        if (sps.ACLineStatus == 1) {
          CControlCenterDlg::RestartComputation(CControlCenterDlg::IsTransferEnabled());
          updateView = true;
        }
      }
    }
    return;
  }
  if ((lastConfig->numberOfProcessors == 1 || filename == currentWorkUnit) && lastLastUpdate == lastUpdate) {
    return;
  }
  if (clientIsActive && lastConfig->checkBatteryMode) {
    SYSTEM_POWER_STATUS sps;
    if (GetSystemPowerStatus(&sps)) {
      if (sps.ACLineStatus == 0) {
        CControlCenterDlg::StopComputation();
        updateView = true;
      }
    }
  }
  if (fh != -1) {
    CSocket socket;
    clientIsActive = !socket.Create(lastConfig->port);
    if (!clientIsActive) {
      remove((const char*)filename);
      if (remove(infoFile.c_str()) == 0) {
        prevNoInfo = false;
      }
    }
  }

  bool progressActive = false;
  ifstream fin(filename);
  if (!fin || fin.eof()) {
    fin.close();
    if (noInfo) {
      if (prevNoInfo) {
        return;
      }
      CString s;
      prevNoInfo = true;
      m_CurrentStatus.GetWindowText(s);
      s = s.Left(s.Find(':')+1);
      s += " No information available";
      m_CurrentStatus.SetWindowText(s);
      m_Performance.SetWindowText("Performance index (zeros per second): ...");
      m_Remaining.SetWindowText("");
    }
    firstPos = lastPos = 0;
    firstTimePos = lastTimePos = 0;
    m_Active.SetRange32(0, 0);
    m_Active.SetPos(0);
    m_Percent.SetWindowText("0 %");
    if (systemTrayIsActive) {
      CString s;
      s.LoadString(IDS_SYSTEM_TRAY);
      s += " (0 %)";
      m_TrayIcon.SetTooltipText(s);
    }
    updateThreadIDBox();
  } else {
    prevNoInfo = false;
    if (noInfo) {
      CString s;
      m_CurrentStatus.GetWindowText(s);
      s = s.Left(s.Find(':')+1);
      if (lastConfig->numberOfProcessors == 1) {
        s += " Computing on 1 processor";
      } else {
        char c[101];
        sprintf(c, " Computing on %d processors", lastConfig->numberOfProcessors);
        s += c;
      }
      m_CurrentStatus.SetWindowText(s);
      if (m_CurrentStatus.IsWindowVisible()) {
        m_Remaining.SetWindowText("");
      }
    } else {
      remove(infoFile.c_str());
    }
    char c[101];
    fin.getline(c, 100);
    fin.close();

    if (lastConfig->numberOfProcessors > 1 && filename != currentWorkUnit) {
      currentWorkUnit = filename;
      firstPos = lastPos = 0;
      firstTimePos = lastTimePos = 0;
    }

    clock_t currentTimePos = clock();
    char* c1 = strchr(c, ',');
    if (c1) {
      __int64 a = _atoi64(c);
      char* c2 = strchr(c1+1, ',');
      if (c2) {
        __int64 start = _atoi64(c1+1);
        int range = atoi(c2+1);
        int currentPos = int(a-start);
        *c2 = '\0';
        char cStart[30];
        strcpy(cStart, c1+1);
        m_Active.SetRange32(0, range);
        m_Active.SetPos(currentPos);
        int pos = (currentPos*1000)/range;
        sprintf(c, "%d.%d %%", (pos/10), (pos%10));
        m_Percent.SetWindowText(c);
        if (systemTrayIsActive) {
          CString s;
          s.LoadString(IDS_SYSTEM_TRAY);
          s += " (";
          s += c;
          s += ")";
          m_TrayIcon.SetTooltipText(s);
        }
        progressActive = true;
        if (currentPos != lastPos) {
          updateView = true;
          if (lastTimePos != 0) {
            double zerosPerSecond = double(currentPos-firstPos)*CLOCKS_PER_SEC/double(currentTimePos-firstTimePos);
            //double performance = zerosPerSecond*InnerLoopFactor(atof(cStart));
            if (zerosPerSecond > 0.0) {
              sprintf(c, "Performance index (zeros per second): %.2f    (work unit %s)", zerosPerSecond, cStart);
              m_Performance.SetWindowText(c);
            }
          }
          if (firstTimePos == 0) {
            firstTimePos = currentTimePos;
            firstPos = currentPos;
            sprintf(c, "Performance index (zeros per second): ...    (work unit %s)", cStart);
            m_Performance.SetWindowText(c);
          }
          lastTimePos = currentTimePos;
          lastPos = currentPos;
        }
      }
    }
    // Remaining:
    if (lastPos != 0 && lastPos != firstPos && firstTimePos != lastTimePos) {
      double zerosPerSecond = double(lastPos-firstPos)*CLOCKS_PER_SEC/double(lastTimePos-firstTimePos);
      int lower,range;
      m_Active.GetRange(lower, range);
      if (range > lastPos) {
        int remainingSeconds = int((range-lastPos)/zerosPerSecond - double(currentTimePos-lastTimePos)/CLOCKS_PER_SEC);
        if (remainingSeconds > 3600*24 || remainingSeconds <= 0) {
          c[0] = '\0';
        } else {
          sprintf(c, "Remaining: about %02d:%02d:%02d", (remainingSeconds/3600), ((remainingSeconds/60)%60), (remainingSeconds%60));
        }
      } else {
        c[0] = '\0';
      }
      cInfo.TrimLeft();
      if (cInfo.GetLength() > 0 && cInfo[0] != 'C') {
        CString s = c;
        s += "    (";
        s += cInfo;
        s += ")";
        m_Remaining.SetWindowText(s);
      } else {
        m_Remaining.SetWindowText(c);
      }
    }
    lastLastUpdate = lastUpdate;
  }

  if (progressActive) {
    m_Active.ShowWindow(SW_SHOW);
    m_Percent.ShowWindow(SW_SHOW);
    m_ProgressText.ShowWindow(SW_SHOW);
    m_CurrentStatus.ShowWindow(SW_HIDE);
  } else {
    m_Active.ShowWindow(SW_HIDE);
    m_Percent.ShowWindow(SW_HIDE);
    if (systemTrayIsActive) {
      CString s;
      s.LoadString(IDS_SYSTEM_TRAY);
      m_TrayIcon.SetTooltipText(s);
    }
    m_ProgressText.ShowWindow(SW_HIDE);
    m_CurrentStatus.ShowWindow(SW_SHOW);
  }

  if (updateView) {
    CString s;
    int count,finished;
    Config::numberOfLocalWorkunits(count, finished);
    s.Format("Number of local work units: %d  (completed %d)", count, finished);
    m_LocalWorkUnits.SetWindowText(s);
    //m_TransferButton.EnableWindow(finished > 0);
    updateThreadIDBox();

    m_ProgressText.GetWindowText(s);
    s = s.Left(s.Find(':')+1);
    struct _stat buf;
    int fh = _open(getThreadIDs(), _O_RDONLY);
    if (fh != -1) {
      _fstat(fh, &buf);
      _close(fh);
      s += " last update ";
      s += ctime(&buf.st_mtime);
    }
    m_ProgressText.SetWindowText(s);

    updateView = false;
  }
}

CString CControlCenterDlg::getThreadIDs() const
{
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros.tmp";
  if (lastConfig->numberOfProcessors <= 1) {
    return t.c_str();
  }
  CString s;
  m_ThreadID.GetWindowText(s);
  if (s == "" || s == "0") {
    return t.c_str();
  }
  CString s2 = Config::ZETAGRID_ROOT_PATH.c_str();
  s2 += "\\zeta_zeros_";
  s2 += s;
  s2 += ".tmp";
  return s2;
}

void CControlCenterDlg::updateThreadIDBox()
{
  CString s;
  m_ThreadID.GetWindowText(s);
  m_ThreadID.ResetContent();
  _finddata_t fileinfo;
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros*";
  long handle = _findfirst(t.c_str(), &fileinfo);
  if (handle != -1) {
    do {
      if (strcmp(fileinfo.name, "zeta_zeros.tmp") == 0) {
        m_ThreadID.AddString("0");
      } else if (strncmp(fileinfo.name, "zeta_zeros_", 11) == 0 && isdigit(fileinfo.name[11]) && strchr(fileinfo.name+11, '_') == NULL) {
        int l = 11+strlen(fileinfo.name+11);
        if (fileinfo.name[l-4] == '.' && fileinfo.name[l-3] == 't' && fileinfo.name[l-2] == 'm' && fileinfo.name[l-1] == 'p') {
          char c[20];
          int i = 11;
          do {
            c[i-11] = fileinfo.name[i];
          } while (++i < 30 && isdigit(fileinfo.name[i]));
          c[i-11] = '\0';
          m_ThreadID.AddString(c);
        }
      }
    } while (_findnext(handle, &fileinfo) != -1);
    _findclose(handle);
  }
  if (m_ThreadID.SelectString(-1, s) == CB_ERR) {
    m_ThreadID.SelectString(-1, "0");
  }
  char c[20];
  m_ActiveThreads.GetWindowText(s);
  s = s.Left(s.Find(':')+1);
  s += " ";
  s += itoa(lastConfig->numberOfProcessors, c, 10);
  m_ActiveThreads.SetWindowText(s);
}

bool CControlCenterDlg::IsTransferEnabled()
{
  return transferEnabled;
}

void CControlCenterDlg::OnButtonTransferWu() 
{
  if (!transferEnabled) {
    return;
  }
  transferEnabled = false;
  m_TransferButton.EnableWindow(FALSE);
  Config cfg;
  if (cfg.checkConnectionToServer) {
    TRY {
      CInternetSession checkConnection(NULL, 1, (cfg.proxyHost.length() == 0)? INTERNET_OPEN_TYPE_PRECONFIG : INTERNET_OPEN_TYPE_PROXY, (cfg.proxyHost.length() == 0)? NULL : cfg.proxyHost.c_str());
      CString url;
      url.Format("http://%s:%d/index.html", cfg.hostName.c_str(), cfg.hostPort);
      CStdioFile* connection = checkConnection.OpenURL(url, 1, INTERNET_FLAG_TRANSFER_ASCII | INTERNET_FLAG_DONT_CACHE);
      if (connection == NULL) {
        CString s,s2;
        s.Format(IDS_NO_CONNECTION_RESTART, cfg.hostName.c_str());
        s2.LoadString(IDS_CONNECTION);
        if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) != IDYES) {
          m_TransferButton.EnableWindow(TRUE);
          transferEnabled = true;
          return;
        }
      }
    } CATCH (CInternetException, e) {
      CString s,s2;
      s.Format(IDS_NO_CONNECTION_RESTART, cfg.hostName.c_str());
      s2.LoadString(IDS_CONNECTION);
      if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) != IDYES) {
        m_TransferButton.EnableWindow(TRUE);
        transferEnabled = true;
        return;
      }
    } AND_CATCH_ALL (e) {
      CString s,s2;
      s.Format(IDS_NO_CONNECTION_RESTART, cfg.hostName.c_str());
      s2.LoadString(IDS_CONNECTION);
      if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) != IDYES) {
        m_TransferButton.EnableWindow(TRUE);
        transferEnabled = true;
        return;
      }
    } END_CATCH_ALL
  }
  OnRestartComputation();
  m_TransferButton.EnableWindow(TRUE);
  transferEnabled = true;
}

void CControlCenterDlg::StatusRefresh()
{
  if (lastConfig->numberOfProcessors > 1) {
    m_ThreadIDText.ShowWindow(SW_SHOW);
    m_ThreadID.ShowWindow(SW_SHOW);
    m_ActiveThreads.ShowWindow(SW_SHOW);
    _finddata_t fileinfo;
    string t = Config::ZETAGRID_ROOT_PATH;
    t += "\\zeta_zeros*";
    long handle = _findfirst(t.c_str(), &fileinfo);
    if (handle != -1) {
      do {
        const int l = 11+strlen(fileinfo.name+11);
        if (isdigit(fileinfo.name[11]) && isdigit(fileinfo.name[l-5]) && fileinfo.name[l-4] == '.' && fileinfo.name[l-3] == 't' && fileinfo.name[l-2] == 'm' && fileinfo.name[l-1] == 'p') {
          t = Config::ZETAGRID_ROOT_PATH;
          t += "\\";
          t += fileinfo.name;
          remove(t.c_str());
        }
      } while (_findnext(handle, &fileinfo) != -1);
      _findclose(handle);
    }
  } else {
    m_ThreadIDText.ShowWindow(SW_HIDE);
    m_ThreadID.ShowWindow(SW_HIDE);
    m_ActiveThreads.ShowWindow(SW_HIDE);
  }
}

BOOL CControlCenterDlg::OnInitDialog() 
{
  CDialog::OnInitDialog();
  HICON icon = AfxGetApp()->LoadIcon(128);
  SetIcon(icon, false);
  SetIcon(icon, true);
  UpdateData(TRUE);
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros.tmp";
  remove(t.c_str());
  StatusRefresh();
  KillTimer(2);
  SetTimer(2, 1000, NULL);
  OnButtonRefresh();
  Config cfg;
  /*if (cfg.serviceMode) {
    OnRadioModeService();
  } else {
    OnRadioModeScreenSaver();
  }*/

  systemTrayIsActive = cfg.systemTray;
  if (systemTrayIsActive) {
    // Create the tray icon
    CString s;
    s.LoadString(IDS_SYSTEM_TRAY);
    if (!m_TrayIcon.Create(this, WM_ICON_NOTIFY, s, icon, IDR_POPUP_MENU)) {
      systemTrayIsActive = false;
      return FALSE;
    }
  }
  m_HideButton.ShowWindow((systemTrayIsActive)? SW_SHOW : SW_HIDE);
  return TRUE;  // return TRUE unless you set the focus to a control
                // EXCEPTION: OCX Property Pages should return FALSE
}

void CControlCenterDlg::OnTimer(UINT nIDEvent) 
{
  if (nIDEvent == 2) {
    OnButtonRefresh();
  } else {
    CDialog::OnTimer(nIDEvent);
  }
}

void CControlCenterDlg::StopComputation()
{
  // Stop calculation
  CSaverApp::exitCalculation(2000);
  Config cfg;
  if (cfg.mode == Config::service) {
    char c[100];
    PROCESS_INFORMATION pi;
    STARTUPINFO si;
    // Stop service
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
    }
    ServiceStop();
  }
  if (running != NULL) {
    running->clientIsActive = false;
  }
}

void CControlCenterDlg::RestartComputation(bool transferEnabled)
{
  StopComputation();
  Config cfg;
  if (cfg.mode == Config::service) {
    CInstallDlg::StartupService();
  } else if (cfg.mode == Config::screensaver && !transferEnabled) {
    CTestDlg testDlg;
    testDlg.Create("Transfer running:");
    testDlg.Start();
    testDlg.Destroy();
  } else if (cfg.mode == Config::commandline) {
    PROCESS_INFORMATION pi;
    STARTUPINFO si;
    GetStartupInfo(&si);
    si.wShowWindow = SW_SHOWNORMAL;
    ZeroMemory(&pi, sizeof(pi));
    char* c = new char[Config::ZETAGRID_ROOT_PATH.length()+10];
    strcpy(c, Config::ZETAGRID_ROOT_PATH.c_str());
    strcat(c, (CInstallDlg::IsWindowsNT())? "\\zeta.cmd" : "\\zeta.bat");
    int success = CreateProcess(NULL, c, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | ((cfg.priority == "low")? IDLE_PRIORITY_CLASS : NORMAL_PRIORITY_CLASS), NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
    if (success) {
      CloseHandle(pi.hThread);
    }
    delete[] c;
  }
  if (running != NULL) {
    running->clientIsActive = true;
  }
}

void CControlCenterDlg::Uninstall()
{
  StopComputation();
  removeService();
  CSaverApp::removeBasicFiles();

  // remove startup link
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), (CInstallDlg::IsWindowsNT())? "zeta.cmd" : "zeta.bat");
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), "control.bat");
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), "ZetaGrid Control Center");
  CInstallDlg::RemoveLink(CInstallDlg::GetStartupPath(), "ZetaGrid command line mode");

  CString s,s2;
  s.Format(IDS_REMOVE_FILES, Config::ZETAGRID_ROOT_PATH.c_str());
  s2.LoadString(IDS_UNINSTALL);
  if (::MessageBox(0, s, s2, MB_ICONQUESTION | MB_YESNO) != IDYES) {
    return;
  }
  _finddata_t fileinfo;
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\*";
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
  t = Config::ZETAGRID_ROOT_PATH;
  t += "\\";
  remove(t.c_str());
}

void CControlCenterDlg::OnConfigConfig() 
{
  Config cfgOrig;
  KonfigurationDlg dlg;
  if (dlg.DoModal() == IDOK) {
    Config cfg;
    if (cfgOrig.eMail != cfg.eMail || cfgOrig.name != cfg.name || cfgOrig.team != cfg.team || cfgOrig.messages != cfg.messages) {
      _finddata_t fileinfo;
      string t = Config::ZETAGRID_ROOT_PATH;
      t += "\\zeta_zeros_*.txt";
      long handle = _findfirst(t.c_str(), &fileinfo);
      if (handle != -1) {
        _findclose(handle);
        CString s,s2;
        s.LoadString(IDS_CHANGE_CONFIG_SERVER);
        s2.LoadString(IDS_CONFIG_SERVER);
        if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
          OnServerConfig();
        }
      }
    }
    lastConfig = new Config();
    if (cfg.mode == Config::service || cfg.mode == Config::commandline) {
      CString s,s2;
      s.LoadString(IDS_ACTIVATE_NEW_CONFIG_NOW);
      s2.LoadString(IDS_ACTIVATE_NEW_CONFIG);
      if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
        StatusRefresh();
        if (cfg.mode == Config::service) {
          updateView = true;
          CInstallDlg::ReinstallService(cfg);
          OnButtonRefresh();
        } else {
          OnButtonTransferWu();
        }
      }
    }
  }
}

void CControlCenterDlg::StartServerConfig()
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/servlet/service/producers?user=";
  int l = max(cfg.name.length(), cfg.eMail.length());
  char* c = new char[2*l];
  strcpy(c, cfg.name.c_str());
  s += escape_url(c);
  s += "&email=";
  strcpy(c, cfg.eMail.c_str());
  s += escape_url(c);
  delete[] c;
  s += "&workunit=";
  _finddata_t fileinfo;
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros_*.txt";
  long handle = _findfirst(t.c_str(), &fileinfo);
  if (handle != -1) {
    s += escape_url(fileinfo.name);
    _findclose(handle);
    StartBrowser(s);
  } else {
    CString s,s2;
    s.LoadString(IDS_NO_ACTIVE_WORK_UNIT);
    s2.LoadString(IDS_ERROR);
    ::MessageBox(NULL, s, s2, MB_ICONSTOP);
  }
}

void CControlCenterDlg::OnServerConfig()
{
  StartServerConfig();
}

void CControlCenterDlg::OnWebHome() 
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/";
  StartBrowser(s);
}

void CControlCenterDlg::OnWebNews() 
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/zeta/news.html";
  StartBrowser(s);
}

void CControlCenterDlg::OnWebStatistic() 
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/servlet/service/producers?user=";
  char* c = new char[2*cfg.name.length()];
  strcpy(c, cfg.name.c_str());
  s += escape_url(c);
  delete[] c;
  StartBrowser(s);
}

void CControlCenterDlg::OnWebStatisticDetail() 
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/servlet/service/producers?user=";
  int l = max(cfg.name.length(), cfg.eMail.length());
  char* c = new char[2*l];
  strcpy(c, cfg.name.c_str());
  s += escape_url(c);
  s += "&email=";
  strcpy(c, cfg.eMail.c_str());
  s += escape_url(c);
  delete[] c;
  StartBrowser(s);
}

void CControlCenterDlg::OnWebTeammembers() 
{
  Config cfg;
  if (cfg.team.length() == 0) {
    CString s = "http://";
    s += cfg.hostName.c_str();
    s += "/servlet/service/teams";
    StartBrowser(s);
  } else {
    CString s = "http://";
    s += cfg.hostName.c_str();
    s += "/servlet/service/teammembers?team=";
    char* c = new char[2*cfg.team.length()];
    strcpy(c, cfg.team.c_str());
    s += escape_url(c);
    delete[] c;
    StartBrowser(s);
  }
}

void CControlCenterDlg::OnLogViewInformationLog()
{
  Config cfg;
  string s = "notepad ";
  s += Config::ZETAGRID_ROOT_PATH;
  s += "\\";
  s += cfg.infoEventLogFilename;
  WinExec(s.c_str(), SW_SHOWDEFAULT);
}

void CControlCenterDlg::OnHelpAbout() 
{
  AboutDlg dlg;
  dlg.DoModal();
}

void CControlCenterDlg::OnHelpForum() 
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/forum/index.jsp";
  StartBrowser(s);
}

void CControlCenterDlg::OnHelpFaq() 
{
  Config cfg;
  CString s = "http://";
  s += cfg.hostName.c_str();
  s += "/zeta/faq.html";
  StartBrowser(s);
}

void CControlCenterDlg::StartBrowser(const char* url)
{
  HKEY key;
  unsigned char buffer[1000];
  unsigned long datatype;
  unsigned long bufferlength = sizeof(buffer);
  if (RegOpenKeyEx(HKEY_CLASSES_ROOT, "http\\shell\\open\\command", 0, KEY_READ, &key) == ERROR_SUCCESS) {
    if (RegQueryValueEx(key, NULL, NULL, &datatype, buffer, &bufferlength) == ERROR_SUCCESS) {
      char* pos = strstr((char*)buffer, "%1");
      if (pos != NULL) {
        *pos = '\0';
        CString s((char*)buffer);
        s += url;
        s += (pos+2);
        strcpy((char*)buffer, (const char*)s);
      } else {
        strcat(strcat((char*)buffer, " "), url);
      }
      PROCESS_INFORMATION pi;
      STARTUPINFO si;
      GetStartupInfo(&si);
      si.wShowWindow = SW_SHOWNORMAL;
      ZeroMemory(&pi, sizeof(pi));
      int success = CreateProcess(NULL, (char*)buffer, NULL, NULL, FALSE, CREATE_NEW_CONSOLE | NORMAL_PRIORITY_CLASS, NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
      if (success) {
        CloseHandle(pi.hThread);
      }
    }
  }
}

void CControlCenterDlg::OnSelchangeComboThreadId() 
{
  OnButtonRefresh();
}

void CControlCenterDlg::OnButtonHide() 
{
  ShowWindow(SW_HIDE);
}

void CControlCenterDlg::OnClose() 
{
  EndModalLoop(IDOK);
  //DestroyWindow();
}

void CControlCenterDlg::OnView() 
{
  ShowWindow(SW_SHOWNORMAL);
  SetForegroundWindow();
}

void CControlCenterDlg::OnRestartComputation()
{
  RestartComputation(IsTransferEnabled());
  updateView = true;
  clientStopped = false;
  OnButtonRefresh();
}

void CControlCenterDlg::OnStopComputation()
{
  updateView = true;
  clientStopped = true;
  StopComputation();
  OnButtonRefresh();
}

void CControlCenterDlg::OnPaint() 
{
  static bool firstCall = true;
  if (firstCall && hideDlg) {
    ShowWindow(SW_HIDE);
    firstCall = false;
  } else {
  	CPaintDC dc(this); // device context for painting
  }
	// Do not call CDialog::OnPaint() for painting messages
}
