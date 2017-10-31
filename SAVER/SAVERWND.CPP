// saverwnd.cpp : implementation file
//

#include "stdafx.h"
#include "config.h"
#include "Saver.h"
#include "drawwnd.h"
#include "saverwnd.h"

#include <fstream>
#include <string>

#ifdef _DEBUG
#undef THIS_FILE
static char BASED_CODE THIS_FILE[] = __FILE__;
#endif

using namespace std;


/////////////////////////////////////////////////////////////////////////////
// CSaverWnd

CSaverWnd::CSaverWnd()
{
  m_pDlg = 0;
	m_ptLast = CPoint(-1, -1);

  Config cfg;
  if (cfg.name.length() == 0) return;

  PROCESS_INFORMATION pi;
  STARTUPINFO si;
  GetStartupInfo(&si);
  si.wShowWindow = SW_MINIMIZE | SW_HIDE;
  SetEnvironmentVariable("PATH", Config::ZETAGRID_ROOT_PATH.c_str());
  SetCurrentDirectory(Config::ZETAGRID_ROOT_PATH.c_str());
  char* c = new char[cfg.call.length()+1];
  strcpy(c, cfg.call.c_str());
  ZeroMemory(&si, sizeof(si));
  si.cb = sizeof(si);
  ZeroMemory(&pi, sizeof(pi));
  string t = Config::ZETAGRID_ROOT_PATH;
  t += "\\zeta_zeros.tmp";
  remove(t.c_str());
  CreateProcess(NULL, c, NULL, NULL, FALSE, DETACHED_PROCESS | ((cfg.priority == "normal")? NORMAL_PRIORITY_CLASS : IDLE_PRIORITY_CLASS), NULL, Config::ZETAGRID_ROOT_PATH.c_str(), &si, &pi);
  delete[] c;
}

CSaverWnd::~CSaverWnd()
{
  if (m_pDlg) m_pDlg->DestroyWindow();
  delete m_pDlg;
}


BEGIN_MESSAGE_MAP(CSaverWnd, CDrawWnd)
	//{{AFX_MSG_MAP(CSaverWnd)
	ON_WM_SYSCOMMAND()
	ON_WM_DESTROY()
	ON_WM_SETCURSOR()
	ON_WM_NCACTIVATE()
	ON_WM_ACTIVATE()
	ON_WM_ACTIVATEAPP()
	ON_WM_MOUSEMOVE()
	ON_WM_LBUTTONDOWN()
	ON_WM_MBUTTONDOWN()
	ON_WM_RBUTTONDOWN()
	ON_WM_KEYDOWN()
	ON_WM_SYSKEYDOWN()
	ON_WM_CREATE()
	ON_WM_TIMER()
	ON_WM_PAINT()
	ON_WM_CLOSE()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

static bool isVerifyPasswordActive = false;
static bool closeWnd = false;


BOOL VerifyPassword(HWND hwnd)
{ // Under NT, we return TRUE immediately. This lets the saver quit,
  // and the system manages passwords. Under '95, we call VerifyScreenSavePwd.
  // This checks the appropriate registry key and, if necessary,
  // pops up a verify dialog
  if (isVerifyPasswordActive) return FALSE;
  OSVERSIONINFO osv;
  osv.dwOSVersionInfoSize = sizeof(osv);
  GetVersionEx(&osv);
  if (osv.dwPlatformId == VER_PLATFORM_WIN32_NT) return TRUE;
  HINSTANCE hpwdcpl = ::LoadLibrary("PASSWORD.CPL");
  if (hpwdcpl == NULL) {
    //Debug("Unable to load PASSWORD.CPL. Aborting");
    return TRUE;
  }
  typedef BOOL (WINAPI *VERIFYSCREENSAVEPWD)(HWND hwnd);
  VERIFYSCREENSAVEPWD VerifyScreenSavePwd;
  VerifyScreenSavePwd = (VERIFYSCREENSAVEPWD)GetProcAddress(hpwdcpl, "VerifyScreenSavePwd");
  if (VerifyScreenSavePwd == NULL) {
    //Debug("Unable to get VerifyPwProc address. Aborting");
    FreeLibrary(hpwdcpl);
    return TRUE;
  }
  //Debug("About to call VerifyPwProc");
  isVerifyPasswordActive = true;
  BOOL bres = VerifyScreenSavePwd(hwnd);
  FreeLibrary(hpwdcpl);
  isVerifyPasswordActive = false;
  return bres;
}

/////////////////////////////////////////////////////////////////////////////
// CSaverWnd message handlers

BOOL CSaverWnd::Create()
{
	CRect rect(0, 0, ::GetSystemMetrics(SM_CXSCREEN), ::GetSystemMetrics(SM_CYSCREEN));

	return CDrawWnd::Create(WS_EX_TOPMOST, WS_VISIBLE|WS_POPUP, rect, NULL,	0, NULL);
}

void CSaverWnd::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID == SC_SCREENSAVE) || (nID == SC_CLOSE)) return;
	CDrawWnd::OnSysCommand(nID, lParam);
}

void CSaverWnd::OnDestroy()
{
  PostQuitMessage(0);
  Config cfg;
  if (cfg.displayState) KillTimer(1);
	CDrawWnd::OnDestroy();
}

BOOL CSaverWnd::OnSetCursor(CWnd* pWnd, UINT nHitTest, UINT message)
{
  if (isVerifyPasswordActive) SetCursor(LoadCursor(NULL,IDC_ARROW));
	else SetCursor(NULL);
  return TRUE;
}

BOOL CSaverWnd::OnNcActivate(BOOL bActive)
{
  if (!isVerifyPasswordActive && !bActive) return FALSE;
	return CDrawWnd::OnNcActivate(bActive);
}

void CSaverWnd::OnActivate(UINT nState, CWnd* pWndOther, BOOL bMinimized)
{
  if (!isVerifyPasswordActive && nState == WA_INACTIVE) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnActivate(nState, pWndOther, bMinimized);
}

void CSaverWnd::OnActivateApp(BOOL bActive, HTASK hTask)
{
  if (!isVerifyPasswordActive && !bActive) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnActivateApp(bActive, hTask);
}

void CSaverWnd::OnMouseMove(UINT nFlags, CPoint point)
{
  if (!isVerifyPasswordActive) {
    if (m_ptLast == CPoint(-1,-1)) m_ptLast = point;
    else if (m_ptLast != point) {
      closeWnd = true;
      PostMessage(WM_CLOSE);
    }
  }
	CDrawWnd::OnMouseMove(nFlags, point);
}

void CSaverWnd::OnLButtonDown(UINT nFlags, CPoint point)
{
  if (!isVerifyPasswordActive) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnLButtonDown(nFlags, point);
}

void CSaverWnd::OnMButtonDown(UINT nFlags, CPoint point)
{
  if (!isVerifyPasswordActive) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnMButtonDown(nFlags, point);
}

void CSaverWnd::OnRButtonDown(UINT nFlags, CPoint point)
{
  if (!isVerifyPasswordActive) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnRButtonDown(nFlags, point);
}

void CSaverWnd::OnKeyDown(UINT nChar, UINT nRepCnt, UINT nFlags)
{
  if (!isVerifyPasswordActive) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnKeyDown(nChar, nRepCnt, nFlags);
}

void CSaverWnd::OnSysKeyDown(UINT nChar, UINT nRepCnt, UINT nFlags)
{
  if (!isVerifyPasswordActive) {
    closeWnd = true;
    PostMessage(WM_CLOSE);
  }
	CDrawWnd::OnSysKeyDown(nChar, nRepCnt, nFlags);
}

int CSaverWnd::OnCreate(LPCREATESTRUCT lpCreateStruct) 
{
	if (CDrawWnd::OnCreate(lpCreateStruct) == -1) return -1;

  Config cfg;
  if (cfg.displayState) {
	  KillTimer(1);
	  VERIFY(SetTimer(1, 1000, NULL) != 0);
  }
  return 0;
}

void CSaverWnd::OnTimer(UINT nIDEvent) 
{
  if (nIDEvent == 1) {
    if (isVerifyPasswordActive) return;

    if (!m_pDlg) {
      m_pDlg = new CState();
      if (m_pDlg->GetSafeHwnd() == 0) m_pDlg->Create(CState::IDD);
      m_pDlg->update();
      m_pDlg->ShowWindow(SW_SHOW);
    }

    static int timer = 0;
    if (++timer >= 10) {
      timer = 0;
      CRect rect;
      m_pDlg->GetWindowRect(&rect);
      int maxX = ::GetSystemMetrics(SM_CXSCREEN);
      if (maxX <= 0) maxX = 1;
      int x = rand()%maxX;
      int w = rect.Width();
      if (x+w > maxX) x = maxX-w;
      int maxY = ::GetSystemMetrics(SM_CYSCREEN);
      if (maxY <= 0) maxY = 1;
      int y = rand()%maxY;
      int h = rect.Height();
      if (y+h > maxY) y = maxY-h;
      m_pDlg->MoveWindow(x, y, w, h);
    }
    m_pDlg->update();
  }	else CDrawWnd::OnTimer(nIDEvent);
}

void CSaverWnd::OnPaint() 
{
  if (isVerifyPasswordActive) return;
  CPaintDC dc(this); // device context for painting
  CBrush brush(RGB(0,0,0));
  CRect rect;
  GetClientRect(rect);
  dc.FillRect(&rect, &brush);
}

void CSaverWnd::OnClose() 
{
  if (closeWnd && VerifyPassword(m_hWnd)) {
    CSaverApp::exitCalculation(0);
    CDrawWnd::OnClose();
  }
  closeWnd = false;
}
