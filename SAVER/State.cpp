// State.cpp : implementation file
//

#include "stdafx.h"
#include "config.h"
#include "saver.h"
#include "State.h"
#include <afxsock.h>
#include <direct.h>
#include <time.h>
#include <fcntl.h>
#include <stdio.h>
#include <io.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fstream>
#include <string>

using namespace std;

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CState dialog


CState::CState(CWnd* pParent /*=NULL*/)
	: CDialog(CState::IDD, pParent)
{
	//{{AFX_DATA_INIT(CState)
	//}}AFX_DATA_INIT
  updateView = true;
  clientIsActive = true;
  lastConfig = new Config();
}


void CState::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CState)
	DDX_Control(pDX, IDC_STATIC_CURRENT_STATUS, m_CurrentStatus);
	DDX_Control(pDX, IDC_STATIC_PROGRESS, m_ProgressText);
	DDX_Control(pDX, IDC_STATIC_REMAINING, m_Remaining);
	DDX_Control(pDX, IDC_STATIC_PERFORMANCE, m_Performance);
	DDX_Control(pDX, IDC_STATIC_LOCAL_PACKAGES, m_LocalWorkUnits);
	DDX_Control(pDX, IDC_STATIC_CLOCK_LARGE, m_ClockLarge);
	DDX_Control(pDX, IDC_STATIC_PERCENT, m_Percent);
	DDX_Control(pDX, IDC_PROGRESS_ACTIVE, m_Active);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CState, CDialog)
	//{{AFX_MSG_MAP(CState)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CState message handlers

BOOL CState::OnInitDialog() 
{
	CDialog::OnInitDialog();
  CFont* font = m_ClockLarge.GetFont();
  font->CreatePointFont(500, "Symbol");
  m_ClockLarge.SetFont(font);
  update();
  updateRanking();
	return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}

void CState::update()
{
  time_t t;
  time(&t);
  tm* gmt = localtime(&t);
  char c[1001];
  sprintf(c, "%02d:%02d:%02d", gmt->tm_hour, gmt->tm_min, gmt->tm_sec);
  //m_Clock.SetWindowText(c);
  m_ClockLarge.SetWindowText(c);

  // see: CControlCenterDlg::OnButtonRefresh()
  static clock_t firstTimePos = 0;
  static clock_t lastTimePos = 0;
  static CString currentWorkUnit;
  static CString lastLastUpdate;
  static int firstPos = 0;
  static int lastPos = 0;
  static bool prevNoInfo = false;
  static CString cInfo = "";

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
    if (!clientIsActive && lastConfig->checkBatteryMode) {
      SYSTEM_POWER_STATUS sps;
      if (GetSystemPowerStatus(&sps)) {
        if (sps.ACLineStatus == 1) {
          CControlCenterDlg::RestartComputation(CControlCenterDlg::IsTransferEnabled());
          clientIsActive = true;
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
        clientIsActive = false;
        updateView = true;
      }
    }
  }
  if (fh != -1) {
    CSocket socket;
    if (socket.Create(lastConfig->port)) {
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
    /*if (systemTrayIsActive) {
      CString s;
      s.LoadString(IDS_SYSTEM_TRAY);
      s += " (0 %)";
      m_TrayIcon.SetTooltipText(s);
    }*/
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
        /*if (systemTrayIsActive) {
          CString s;
          s.LoadString(IDS_SYSTEM_TRAY);
          s += " (";
          s += c;
          s += ")";
          m_TrayIcon.SetTooltipText(s);
        }*/
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
    /*if (systemTrayIsActive) {
      CString s;
      s.LoadString(IDS_SYSTEM_TRAY);
      m_TrayIcon.SetTooltipText(s);
    }*/
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

void CState::updateThreadIDBox()
{
}

void CState::updateRanking()
{
  /*Config cfg;
  m_Ranking.ResetContent();
  //if (!cfg.displayRanking) {
    m_RankingText.ShowWindow(SW_HIDE);
    m_Ranking.ShowWindow(SW_HIDE);
    m_Clock.ShowWindow(SW_HIDE);
    m_ClockLarge.ShowWindow(SW_SHOW);
    return;
  }
  m_RankingText.ShowWindow(SW_SHOW);
  m_Ranking.ShowWindow(SW_SHOW);
  m_Clock.ShowWindow(SW_SHOW);
  m_ClockLarge.ShowWindow(SW_HIDE);
  string statFile = Config::ZETAGRID_ROOT_PATH;
  statFile += "\\statistic.txt";
  ifstream fin(statFile.c_str(), ios::binary);
  CString s;
  string s2,s3;
  int idx = 0;
  int i = 0;
  while (!fin.eof()) {
    getline(fin, s2);
    int j = s2.find(':');
    if (j == string::npos) continue;
    ++idx;
    if (i > 0 || s2.substr(0, j) == cfg.name) {
      if (i == 0 && s3.length() > 0) {
        int j2 = s3.find(':');
        if (j2 != string::npos) {
          ++i;
          s.Format("%3d. %s (%s)", (idx-1), s3.substr(0, j2).c_str(), s3.substr(j2+1).c_str());
          m_Ranking.AddString(s);
        }
      }
      ++i;
      s.Format("%3d. %s (%s)", idx, s2.substr(0, j).c_str(), s2.substr(j+1).c_str());
      m_Ranking.AddString(s);
    }
    if (i >= 5) break;
    s3 = s2;
  }*/
}

CString CState::getThreadIDs() const
{
  string tmpFile = Config::ZETAGRID_ROOT_PATH;
  tmpFile += "\\zeta_zeros.tmp";
  return tmpFile.c_str();
}

