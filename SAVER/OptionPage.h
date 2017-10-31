#if !defined(AFX_OPTIONPAGE_H__54EDD5F5_A043_4947_907A_C459710C181E__INCLUDED_)
#define AFX_OPTIONPAGE_H__54EDD5F5_A043_4947_907A_C459710C181E__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// OptionPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// OptionPage dialog

class OptionPage : public CPropertyPage
{
	DECLARE_DYNCREATE(OptionPage)

// Construction
public:
	OptionPage();
	~OptionPage();

  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(bool information);

  static CString getRegistryJava(bool message);
  static CString getRegistryJavaLib(bool message);

// Dialog Data
	//{{AFX_DATA(OptionPage)
	enum { IDD = IDD_PROPPAGE_OPTION };
	CStatic	m_JavaLibText;
	CEdit	m_JavaLibEdit;
	CButton	m_LibChoose;
	BOOL	m_DisplayState;
	BOOL	m_SystemTray;
	CString	m_JavaVM;
	CString	m_RootPath;
	CString	m_JavaLib;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(OptionPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	// Generated message map functions
	//{{AFX_MSG(OptionPage)
	afx_msg void OnButtonRootBrowse();
	afx_msg void OnButtonVmChoose();
	afx_msg void OnButtonLibChoose();
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_OPTIONPAGE_H__54EDD5F5_A043_4947_907A_C459710C181E__INCLUDED_)
