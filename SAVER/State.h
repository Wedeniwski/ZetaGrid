#if !defined(AFX_STATE_H__34EBCC41_B13B_11D5_9450_FA5BFB000000__INCLUDED_)
#define AFX_STATE_H__34EBCC41_B13B_11D5_9450_FA5BFB000000__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// State.h : header file
//

#include "ControlCenterDlg.h"

/////////////////////////////////////////////////////////////////////////////
// CState dialog

class CState : public CDialog
{
  bool updateView;
  bool clientIsActive;
  Config* lastConfig;

  void updateThreadIDBox();
  CString getThreadIDs() const;

// Construction
public:
	CState(CWnd* pParent = NULL);   // standard constructor

  void update();
  void updateRanking();

// Dialog Data
	//{{AFX_DATA(CState)
	enum { IDD = IDD_STATE };
	CStatic	m_CurrentStatus;
	CStatic	m_ProgressText;
	CStatic	m_Remaining;
	CStatic	m_Performance;
	CStatic	m_LocalWorkUnits;
	CStatic	m_ClockLarge;
	CStatic	m_Percent;
	CProgressCtrl	m_Active;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CState)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CState)
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_STATE_H__34EBCC41_B13B_11D5_9450_FA5BFB000000__INCLUDED_)
