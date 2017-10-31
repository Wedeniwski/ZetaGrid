// KonfigurationDlg.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "drawwnd.h"
#include "saverwnd.h"
#include "KonfigurationDlg.h"
#include "TestDlg.h"
#include "AboutDlg.h"
#include <fstream>

using namespace std;

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// KonfigurationDlg dialog

KonfigurationDlg* KonfigurationDlg::running = 0;

KonfigurationDlg::KonfigurationDlg(CWnd* pParent /*=NULL*/, bool installMode)
	: CPropertySheet(KonfigurationDlg::IDD, pParent)
{
  test = false;
  running = this;

  generalPage.setInstallMode(installMode);
  AddPage(&generalPage);
  AddPage(&workUnitPage);
  AddPage(&processPage);
  AddPage(&securityPage);
  AddPage(&powerPage);
  AddPage(&loggingPage);
  AddPage(&connectionPage);
  AddPage(&optionPage);
  m_psh.dwFlags |= PSH_NOAPPLYNOW;
}

KonfigurationDlg::~KonfigurationDlg()
{
  running = 0;
}

BEGIN_MESSAGE_MAP(KonfigurationDlg, CPropertySheet)
	//{{AFX_MSG_MAP(KonfigurationDlg)
	ON_BN_CLICKED(IDABOUT, OnAbout)
  ON_BN_CLICKED(IDOK, OnOK)
	ON_BN_CLICKED(IDC_BUTTON_TEST, OnButtonTest)
	ON_BN_CLICKED(IDC_BUTTON_HELP, OnButtonHelp)
	ON_BN_CLICKED(IDHELP, OnButtonHelp)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// KonfigurationDlg message handlers

void KonfigurationDlg::OnAbout() 
{
  AboutDlg dlg;
  dlg.DoModal();
}

BOOL KonfigurationDlg::OnInitDialog()
{
  CPropertySheet::OnInitDialog();
  HICON icon = GetIcon(IDR_MAINFRAME);
  SetIcon(icon, TRUE);
  SetIcon(icon, FALSE);
  CString s;
  s.LoadString(IDS_TITLE_CONFIGURATION);
  SetTitle(s);
  UpdateData(TRUE);

  SetActivePage(0);
  generalPage.set(cfg);
  SetActivePage(1);
  workUnitPage.set(cfg);
  SetActivePage(2);
  processPage.set(cfg);
  SetActivePage(3);
  securityPage.set(cfg);
  SetActivePage(4);
  powerPage.set(cfg);
  SetActivePage(5);
  loggingPage.set(cfg);
  SetActivePage(6);
  connectionPage.set(cfg);
  SetActivePage(7);
  optionPage.set(cfg);
  SetActivePage(0);

  UpdateData(FALSE);

  originTeamname = cfg.team;
  originMessages = cfg.messages;
  originPackageSize = cfg.packageSize;
  connectionPage.OnCheckUseProxy();

  CRect rectWnd,rectBtnCancel;
  CFont* font = GetDlgItem(IDOK)->GetFont();
  GetDlgItem(IDOK)->GetWindowRect(rectWnd);
  ScreenToClient(&rectWnd);
  GetDlgItem(IDCANCEL)->GetWindowRect(rectBtnCancel);
  ScreenToClient(&rectBtnCancel);
  int width = rectBtnCancel.left - rectWnd.left;
  rectWnd.left -= 2*width;
  rectWnd.right -= 2*width;
  GetDlgItem(IDOK)->MoveWindow(rectWnd);
  rectWnd.left += width;
  rectWnd.right += width;
  GetDlgItem(IDCANCEL)->MoveWindow(rectWnd);

  rectWnd.left += width;
  rectWnd.right += width;
  m_About.Create("&About", WS_CHILD | WS_VISIBLE /*| WS_TABSTOP*/ | BS_PUSHBUTTON, rectWnd, this, IDABOUT);
  m_About.SetFont(font);
  rectWnd.left += width;
  rectWnd.right += width;
  m_Test.Create("&Test", WS_CHILD | WS_VISIBLE /*| WS_TABSTOP*/ | BS_PUSHBUTTON, rectWnd, this, IDC_BUTTON_TEST);
  m_Test.SetFont(font);
  //GetDlgItem(IDHELP)->ModifyStyleEx(0, WS_TABSTOP);

  return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}

bool KonfigurationDlg::check(bool information)
{
  UpdateData(TRUE);
  if (!generalPage.check(information)) {
    SetActivePage(0);
    return false;
  }
  generalPage.get(cfg);
  if (!processPage.check(information)) {
    SetActivePage(2);
    return false;
  }
  processPage.get(cfg);
  if (!securityPage.check(information)) {
    SetActivePage(3);
    return false;
  }
  securityPage.get(cfg);
  if (!powerPage.check(information)) {
    SetActivePage(4);
    return false;
  }
  powerPage.get(cfg);
  if (!loggingPage.check(information)) {
    SetActivePage(5);
    return false;
  }
  loggingPage.get(cfg);
  if (!connectionPage.check(information)) {
    SetActivePage(6);
    return false;
  }
  connectionPage.get(cfg);
  if (!optionPage.check(information)) {
    SetActivePage(7);
    return false;
  }
  optionPage.get(cfg);
  if (!workUnitPage.check(cfg, information)) {
    SetActivePage(1);
    return false;
  }
  workUnitPage.get(cfg);
  /*if (information && (originTeamname != cfg.team || originMessages != cfg.messages)) {
    SetActivePage(0);
    CString s,s2;
    s.LoadString(IDS_MESSAGE_INFO);
    s2.LoadString(IDS_INFORMATION);
    MessageBox(s, s2, MB_ICONINFORMATION);
  }*/
  if (information && originPackageSize != cfg.packageSize) {
    int count,finished;
    Config::numberOfLocalWorkunits(count, finished);
    if (count > 0) {
      SetActivePage(1);
      CString s,s2;
      s.LoadString(IDS_PACKAGE_SIZE_INFO);
      s2.LoadString(IDS_INFORMATION);
      MessageBox(s, s2, MB_ICONINFORMATION);
    }
  }
  if (information) {
    cfg.save(true);
  }
  return true;
}

void KonfigurationDlg::OnOK() 
{
  if (check(false)) {
    CString s,s2;
    s.LoadString(IDS_TEST_THIS_CONFIGURATION);
    s2.LoadString(IDS_TEST);
    if (!test && MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
      OnButtonTest();
    } else if (check(true)) {
      EndDialog(IDOK);
    }
  }
}

void KonfigurationDlg::OnButtonTest() 
{
  if (!check(false)) return;
  test = true;

  CTestDlg testDlg;
  if (!testDlg.IsCancel()) {
    CString s;
    s.LoadString(IDS_TEST_RUNNING);
    testDlg.Create(s);
    testDlg.Start();
    testDlg.Destroy();
  }
}

void KonfigurationDlg::OnButtonHelp() 
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

void KonfigurationDlg::DoDataExchange(CDataExchange* pDX) 
{
	CPropertySheet::DoDataExchange(pDX);
}
