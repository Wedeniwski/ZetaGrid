#if !defined(AFX_LOGGINGPAGE_H__4C62B304_67E1_40B1_AA09_855580FED97C__INCLUDED_)
#define AFX_LOGGINGPAGE_H__4C62B304_67E1_40B1_AA09_855580FED97C__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// LoggingPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// LoggingPage dialog

class LoggingPage : public CPropertyPage
{
	DECLARE_DYNCREATE(LoggingPage)

// Construction
public:
	LoggingPage();
	~LoggingPage();

  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(bool information);

// Dialog Data
	//{{AFX_DATA(LoggingPage)
	enum { IDD = IDD_PROPPAGE_LOGGING };
	CButton	m_StdOutput;
	CString	m_EventFormatTimestamp;
	CString	m_EventLogFile;
	int		m_EventNumberBackup;
	CString	m_EventLogFileSize;
	//}}AFX_DATA


// Overrides
	// ClassWizard generate virtual function overrides
	//{{AFX_VIRTUAL(LoggingPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	// Generated message map functions
	//{{AFX_MSG(LoggingPage)
		// NOTE: the ClassWizard will add member functions here
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_LOGGINGPAGE_H__4C62B304_67E1_40B1_AA09_855580FED97C__INCLUDED_)
