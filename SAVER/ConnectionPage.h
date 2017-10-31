#if !defined(AFX_CONNECTIONPAGE_H__76D51E13_1B32_43C8_931B_6E426E119B43__INCLUDED_)
#define AFX_CONNECTIONPAGE_H__76D51E13_1B32_43C8_931B_6E426E119B43__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// ConnectionPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// ConnectionPage dialog

class ConnectionPage : public CPropertyPage
{
	DECLARE_DYNCREATE(ConnectionPage)

// Construction
public:
	ConnectionPage();
	~ConnectionPage();

  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(bool information);

// Dialog Data
	//{{AFX_DATA(ConnectionPage)
	enum { IDD = IDD_PROPPAGE_CONNECTION };
	CButton	m_CheckConnection;
	CStatic	m_Username;
	CStatic	m_Password;
	CEdit	m_PasswordText;
	CEdit	m_UsernameText;
	CButton	m_ProxyAuthentication;
	CButton	m_UseProxy;
	CStatic	m_ProxyPort;
	CStatic	m_ProxyAddress;
	CEdit	m_ProxyPortText;
	CEdit	m_ProxyAddressText;
	//}}AFX_DATA


// Overrides
	// ClassWizard generate virtual function overrides
	//{{AFX_VIRTUAL(ConnectionPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
public:
	// Generated message map functions
	//{{AFX_MSG(ConnectionPage)
	afx_msg void OnCheckUseProxy();
	afx_msg void OnCheckProxyAuthentication();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_CONNECTIONPAGE_H__76D51E13_1B32_43C8_931B_6E426E119B43__INCLUDED_)
