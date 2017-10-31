// SecurityPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "SecurityPage.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CSecurityPage property page

IMPLEMENT_DYNCREATE(CSecurityPage, CPropertyPage)

CSecurityPage::CSecurityPage() : CPropertyPage(CSecurityPage::IDD)
{
	//{{AFX_DATA_INIT(CSecurityPage)
	//}}AFX_DATA_INIT
}

CSecurityPage::~CSecurityPage()
{
}

void CSecurityPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CSecurityPage)
	DDX_Control(pDX, IDC_EDIT_TRUSTED_USERS, m_TrustedUsers);
	DDX_Control(pDX, IDC_TRUST_ALL_USER, m_TrustAllUsers);
	DDX_Control(pDX, IDC_ENCRYPTS_URL, m_EncryptsURL);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CSecurityPage, CPropertyPage)
	//{{AFX_MSG_MAP(CSecurityPage)
	ON_BN_CLICKED(IDC_TRUST_ALL_USER, OnTrustAllUser)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CSecurityPage message handlers

void CSecurityPage::set(const Config& cfg)
{
  UpdateData(TRUE);
	m_EncryptsURL.SetCheck(cfg.encryptionUrl);
  const char* c = cfg.trustFilesFromUsers.c_str();
	m_TrustedUsers.SetWindowText(c);
  m_TrustAllUsers.SetCheck(*c == '\0');
  OnTrustAllUser();
  UpdateData(FALSE);
}

void CSecurityPage::get(Config& cfg)
{
  UpdateData(TRUE);
	cfg.encryptionUrl = bool(m_EncryptsURL.GetCheck());
  CString s;
  m_TrustedUsers.GetWindowText(s);
	cfg.trustFilesFromUsers = (const char*)s;
}

bool CSecurityPage::check(bool information)
{
  return true;
}

void CSecurityPage::OnTrustAllUser() 
{
  m_TrustedUsers.EnableWindow(!m_TrustAllUsers.GetCheck());
}
