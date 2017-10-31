#if !defined(AFX_WORKUNITPAGE_H__BA6C54C5_210F_45E5_9CDE_0A65DACEB5AF__INCLUDED_)
#define AFX_WORKUNITPAGE_H__BA6C54C5_210F_45E5_9CDE_0A65DACEB5AF__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// WorkUnitPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// WorkUnitPage dialog

class WorkUnitPage : public CPropertyPage
{
	DECLARE_DYNCREATE(WorkUnitPage)

// Construction
public:
	WorkUnitPage();
	~WorkUnitPage();

  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(const Config& cfg, bool information);

  static bool completeAllLocalWorkUnits;
// Dialog Data
	//{{AFX_DATA(WorkUnitPage)
	enum { IDD = IDD_PROPPAGE_WORK_UNIT };
	CButton	m_CompleteAllLocalWorkUnits;
	CComboBox	m_PackageSize;
	CComboBox	m_TaskName;
	int		m_NumberOfPackages;
	//}}AFX_DATA


// Overrides
	// ClassWizard generate virtual function overrides
	//{{AFX_VIRTUAL(WorkUnitPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	// Generated message map functions
	//{{AFX_MSG(WorkUnitPage)
	afx_msg void OnButtonNoteNumberOfWorkUnits();
	afx_msg void OnButtonNoteSizeOfWorkUnit();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_WORKUNITPAGE_H__BA6C54C5_210F_45E5_9CDE_0A65DACEB5AF__INCLUDED_)
