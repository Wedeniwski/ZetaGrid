#if !defined(AFX_KONFIGURATIONDLG_H__95E41071_A459_11D5_942A_2E1BBF000000__INCLUDED_)
#define AFX_KONFIGURATIONDLG_H__95E41071_A459_11D5_942A_2E1BBF000000__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// KonfigurationDlg.h : header file
//

#include "config.h"
#include "GeneralPage.h"
#include "ProcessPage.h"
#include "PowerPage.h"
#include "WorkUnitPage.h"
#include "SecurityPage.h"
#include "ConnectionPage.h"
#include "LoggingPage.h"
#include "OptionPage.h"

/////////////////////////////////////////////////////////////////////////////
// KonfigurationDlg dialog

class KonfigurationDlg : public CPropertySheet
{
  GeneralPage generalPage;
  ProcessPage processPage;
  CPowerPage powerPage;
  WorkUnitPage workUnitPage;
  CSecurityPage securityPage;
  ConnectionPage connectionPage;
  LoggingPage loggingPage;
  OptionPage optionPage;

  bool test;
  string originTeamname;
  bool originMessages;
  char originPackageSize;
  bool check(bool information);
	CButton	m_Test, m_About;

// Construction
public:
  static KonfigurationDlg* running;
  Config cfg;
	KonfigurationDlg(CWnd* pParent = NULL, bool installMode = false);   // standard constructor
  ~KonfigurationDlg();

// Dialog Data
	//{{AFX_DATA(KonfigurationDlg)
	enum { IDD = IDD_SAVER_DIALOG };
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(KonfigurationDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(KonfigurationDlg)
	afx_msg void OnAbout();
	virtual BOOL OnInitDialog();
	virtual void OnOK();
	afx_msg void OnButtonTest();
  afx_msg void OnButtonHelp();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_KONFIGURATIONDLG_H__95E41071_A459_11D5_942A_2E1BBF000000__INCLUDED_)
