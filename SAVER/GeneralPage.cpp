// GeneralPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "GeneralPage.h"
#include "ControlCenterDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// GeneralPage property page

IMPLEMENT_DYNCREATE(GeneralPage, CPropertyPage)

GeneralPage::GeneralPage() : CPropertyPage(GeneralPage::IDD)
{
	//{{AFX_DATA_INIT(GeneralPage)
	//}}AFX_DATA_INIT
}

GeneralPage::~GeneralPage()
{
}

void GeneralPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(GeneralPage)
	DDX_Control(pDX, IDC_EDIT_EMAIL, m_eMail);
	DDX_Control(pDX, IDC_EDIT_NAME, m_Name);
	DDX_Control(pDX, IDC_VALUES_CHANGED_AT_SERVER, m_ValuesChangedAtServer);
	DDX_Control(pDX, IDC_BUTTON_NOTE_EMAIL, m_NoteEMail);
	DDX_Control(pDX, IDC_BUTTON_NOTE_NAME, m_NoteName);
	DDX_Control(pDX, IDC_BUTTON_NOTE_TEAMNAME, m_NoteTeamname);
	DDX_Control(pDX, IDC_BUTTON_CHANGE_AT_SERVER, m_ChangeAtServer);
	DDX_Control(pDX, IDC_EDIT_TEAMNAME, m_Teamname);
	DDX_Control(pDX, IDC_CHECK_TEAM, m_ActivateTeam);
	DDX_Control(pDX, IDC_CHECK_MESSAGES, m_Messages);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(GeneralPage, CPropertyPage)
	//{{AFX_MSG_MAP(GeneralPage)
	ON_BN_CLICKED(IDC_BUTTON_NOTE_NAME, OnButtonNoteName)
	ON_BN_CLICKED(IDC_BUTTON_NOTE_EMAIL, OnButtonNoteEmail)
	ON_BN_CLICKED(IDC_BUTTON_NOTE_TEAMNAME, OnButtonNoteTeamname)
	ON_BN_CLICKED(IDC_CHECK_TEAM, OnCheckTeam)
	ON_BN_CLICKED(IDC_VALUES_CHANGED_AT_SERVER, OnValuesChangedAtServer)
	ON_BN_CLICKED(IDC_BUTTON_CHANGE_AT_SERVER, OnButtonChangeAtServer)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// GeneralPage message handlers

void GeneralPage::setInstallMode(bool mode)
{
  installMode = mode;
}

void GeneralPage::set(const Config& cfg)
{
  UpdateData(TRUE);
  m_Name.SetWindowText(cfg.name.c_str());
  m_eMail.SetWindowText(cfg.eMail.c_str());
  m_Messages.SetCheck(cfg.messages);
  m_ActivateTeam.SetCheck(cfg.team.length() > 0);
  if (installMode) {
    m_ValuesChangedAtServer.SetCheck(1);
    m_ValuesChangedAtServer.ShowWindow(SW_HIDE);
    m_ChangeAtServer.ShowWindow(SW_HIDE);
  } else {
    m_ValuesChangedAtServer.SetCheck(0);
  }
  OnValuesChangedAtServer();
  m_Teamname.SetWindowText(cfg.team.c_str());
  UpdateData(FALSE);
}

void GeneralPage::get(Config& cfg)
{
  UpdateData(TRUE);
  CString name;
  m_Name.GetWindowText(name);
  cfg.name = (const char*)name;
  CString eMail;
  m_eMail.GetWindowText(eMail);
  cfg.eMail = (const char*)eMail;
  cfg.messages = (m_Messages.GetCheck() != 0);
  CString team;
  m_Teamname.GetWindowText(team);
  cfg.team = (const char*)team;
}

static bool isValidEmailAddressSyntax(const char* emailAddress) {
  int idxAt = -1;
  int idxDot = -1;
  int l = strlen(emailAddress);
  for (int i = 0; i < l; ++i) {
    char c = emailAddress[i];
    if (!isalnum(c)) {
      if (c == '@') {
        idxAt = i;
        if (i+1 == l) {
          return false;
        }
        c = emailAddress[i+1];
        if (c == '.' || c == '-') {
          return false;
        }
      } else if (c == '-') {
        if (i+1 == l || emailAddress[i+1] == '.') {
          return false;
        }
      } else if (c == '.') {
        idxDot = i;
        if (i+1 == l || emailAddress[i+1] == '-' || emailAddress[i+1] == '.') {
          return false;
        }
      } else if (c == '_') {
        if (i+1 == l) {
          return false;
        }
      } else {
        return false;
      }
    }
  }
  return (l >= 6 && idxAt > 0 && idxAt < idxDot && idxDot+2 < l && l-idxAt <= 68);
}

bool GeneralPage::check(bool information)
{
  UpdateData(TRUE);
  // Check name
  CString name;
  m_Name.GetWindowText(name);
  name.TrimLeft();
  name.TrimRight();
  if (name.GetLength() == 0) {
    CString s,s2;
    s.LoadString(IDS_NAME_NOT_DEFINED);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  // Check email
  CString eMail;
  m_eMail.GetWindowText(eMail);
  eMail.TrimLeft();
  eMail.TrimRight();
  if (eMail.GetLength() > 0 && !isValidEmailAddressSyntax(eMail)) {
    CString s,s2;
    s.LoadString(IDS_EMAIL_NOT_VALID);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  // Check team name
  CString team;
  m_Teamname.GetWindowText(team);
  team.TrimLeft();
  team.TrimRight();
  if (m_ActivateTeam.GetCheck() && (team.GetLength() == 0 || eMail.GetLength() == 0)) {
    CString s,s2;
    s.LoadString(IDS_TEAMNAME_INFO);
    s2.LoadString(IDS_INFORMATION);
    if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
      return false;
    }
  }
  UpdateData(FALSE);
  return true;
}

void GeneralPage::OnButtonNoteName() 
{
  CString s,s2;
  s.LoadString(IDS_NAME_NOTE);
  s2.LoadString(IDS_PLEASE_NOTE);
  if (MessageBox(s, s2, MB_ICONQUESTION | MB_YESNO) == IDYES) {
    m_Name.SetWindowText("anonymous");
    UpdateData(FALSE);
  }
}

void GeneralPage::OnButtonNoteEmail() 
{
  CString s,s2;
  s.LoadString(IDS_EMAIL_NOTE);
  s2.LoadString(IDS_PLEASE_NOTE);
  MessageBox(s, s2, MB_ICONINFORMATION | MB_OK);
}

void GeneralPage::OnButtonNoteTeamname() 
{
  CString s,s2;
  s.LoadString(IDS_TEAM_NOTE);
  s2.LoadString(IDS_PLEASE_NOTE);
  MessageBox(s, s2, MB_ICONINFORMATION | MB_OK);
}

void GeneralPage::OnCheckTeam()
{
  BOOL enable = (m_ActivateTeam.GetCheck() != 0);
  m_Teamname.SetWindowText("");
  m_Teamname.EnableWindow(enable);
}

void GeneralPage::OnValuesChangedAtServer()
{
  BOOL enable = (m_ValuesChangedAtServer.GetCheck() != 0);
  m_ActivateTeam.EnableWindow(enable);
  m_Name.EnableWindow(enable);
  m_eMail.EnableWindow(enable);
  m_Messages.EnableWindow(enable);
  m_NoteEMail.EnableWindow(enable);
  m_NoteName.EnableWindow(enable);
  m_NoteTeamname.EnableWindow(enable);
  OnCheckTeam();
}

void GeneralPage::OnButtonChangeAtServer() 
{
  CControlCenterDlg::StartServerConfig();
  m_ValuesChangedAtServer.SetCheck(1);
  OnValuesChangedAtServer();
  CString s,s2;
  s.LoadString(IDS_CHANGE_LOCAL_CFG);
  s2.LoadString(IDS_INFORMATION);
  MessageBox(s, s2, MB_ICONINFORMATION);
}
