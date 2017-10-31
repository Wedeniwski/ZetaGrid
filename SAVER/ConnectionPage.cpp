// ConnectionPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "ConnectionPage.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// ConnectionPage property page

IMPLEMENT_DYNCREATE(ConnectionPage, CPropertyPage)

ConnectionPage::ConnectionPage() : CPropertyPage(ConnectionPage::IDD)
{
	//{{AFX_DATA_INIT(ConnectionPage)
	//}}AFX_DATA_INIT
}

ConnectionPage::~ConnectionPage()
{
}

void ConnectionPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(ConnectionPage)
	DDX_Control(pDX, IDC_CHECK_CONNECTION_TO_SERVER, m_CheckConnection);
	DDX_Control(pDX, IDC_STATIC_USERNAME, m_Username);
	DDX_Control(pDX, IDC_STATIC_PASSWORD, m_Password);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_PasswordText);
	DDX_Control(pDX, IDC_EDIT_USERNAME, m_UsernameText);
	DDX_Control(pDX, IDC_CHECK_PROXY_AUTHENTICATION, m_ProxyAuthentication);
	DDX_Control(pDX, IDC_CHECK_USE_PROXY, m_UseProxy);
	DDX_Control(pDX, IDC_STATIC_PROXY_PORT, m_ProxyPort);
	DDX_Control(pDX, IDC_STATIC_PROXY_ADDRESS, m_ProxyAddress);
	DDX_Control(pDX, IDC_EDIT_PROXY_PORT, m_ProxyPortText);
	DDX_Control(pDX, IDC_EDIT_PROXY_ADDRESS, m_ProxyAddressText);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(ConnectionPage, CPropertyPage)
	//{{AFX_MSG_MAP(ConnectionPage)
	ON_BN_CLICKED(IDC_CHECK_USE_PROXY, OnCheckUseProxy)
	ON_BN_CLICKED(IDC_CHECK_PROXY_AUTHENTICATION, OnCheckProxyAuthentication)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// ConnectionPage message handlers

void ConnectionPage::set(const Config& cfg)
{
  UpdateData(TRUE);
  m_ProxyAddressText.SetWindowText(cfg.proxyHost.c_str());
  m_ProxyPortText.SetWindowText(cfg.proxyPort.c_str());
  m_UseProxy.SetCheck(cfg.proxyHost.length() > 0);
  m_ProxyAuthentication.SetCheck(cfg.proxyAuthenticationUsername.length() > 0);
  m_UsernameText.SetWindowText(cfg.proxyAuthenticationUsername.c_str());
  m_PasswordText.SetWindowText(cfg.proxyAuthenticationPassword.c_str());
  m_CheckConnection.SetCheck(cfg.checkConnectionToServer);
  UpdateData(FALSE);
}

void ConnectionPage::get(Config& cfg)
{
  UpdateData(TRUE);
  if (m_UseProxy.GetCheck()) {
    CString s;
    m_ProxyAddressText.GetWindowText(s);
    cfg.proxyHost = (const char*)s;
    m_ProxyPortText.GetWindowText(s);
    cfg.proxyPort = (const char*)s;
  } else {
    cfg.proxyHost = "";
    cfg.proxyPort = "";
  }
  if (m_ProxyAuthentication.GetCheck()) {
    CString s;
	  m_UsernameText.GetWindowText(s);
    cfg.proxyAuthenticationUsername = (const char*)s;
    m_PasswordText.GetWindowText(s);
    cfg.proxyAuthenticationPassword = (const char*)s;
  } else {
    cfg.proxyAuthenticationUsername = "";
    cfg.proxyAuthenticationPassword = "";
  }
  cfg.checkConnectionToServer = bool(m_CheckConnection.GetCheck());
}

bool ConnectionPage::check(bool information)
{
  UpdateData(TRUE);
  return true;
}

void ConnectionPage::OnCheckUseProxy() 
{
  BOOL enable = (m_UseProxy.GetCheck() != 0);
  m_ProxyPort.EnableWindow(enable);
	m_ProxyAddress.EnableWindow(enable);
	m_ProxyPortText.EnableWindow(enable);
	m_ProxyAddressText.EnableWindow(enable);
  m_ProxyAuthentication.EnableWindow(enable);
  enable = (m_ProxyAuthentication.GetCheck() != 0);
	m_Username.EnableWindow(enable);
	m_Password.EnableWindow(enable);
	m_PasswordText.EnableWindow(enable);
	m_UsernameText.EnableWindow(enable);
}

void ConnectionPage::OnCheckProxyAuthentication() 
{
  OnCheckUseProxy();
}
