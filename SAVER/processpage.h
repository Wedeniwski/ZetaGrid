#if !defined(AFX_PROCESSPAGE_H__3C389955_4787_4051_ACAA_36826D09523C__INCLUDED_)
#define AFX_PROCESSPAGE_H__3C389955_4787_4051_ACAA_36826D09523C__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// ProcessPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// ProcessPage dialog

class ProcessPage : public CPropertyPage
{
	DECLARE_DYNCREATE(ProcessPage)

// Construction
public:
	ProcessPage();
	~ProcessPage();

  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(bool information);

// Dialog Data
	//{{AFX_DATA(ProcessPage)
	enum { IDD = IDD_PROPPAGE_PROCESS };
	CEdit	m_TransferDetectText;
	CStatic	m_TransferDetect;
	CStatic	m_PriorityText;
	CSliderCtrl	m_Intensity;
	CComboBox	m_Resources;
	CComboBox	m_Priority;
	int		m_NumberOfProcessors;
	//}}AFX_DATA


// Overrides
	// ClassWizard generate virtual function overrides
	//{{AFX_VIRTUAL(ProcessPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
public:
  // Generated message map functions
	//{{AFX_MSG(ProcessPage)
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_PROCESSPAGE_H__3C389955_4787_4051_ACAA_36826D09523C__INCLUDED_)
