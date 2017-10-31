#if !defined(AFX_TESTDLG_H__726B6739_1004_45C5_A74E_2E364A37717C__INCLUDED_)
#define AFX_TESTDLG_H__726B6739_1004_45C5_A74E_2E364A37717C__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// TestDlg.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CTestDlg dialog

class CTestDlg : public CDialog
{
  bool cancel;

// Construction
public:
	CTestDlg(CWnd* pParent = NULL);   // standard constructor

  BOOL Create(const char* title);
  void Destroy();
  void Start();
  bool IsCancel() { return cancel; }

// Dialog Data
	//{{AFX_DATA(CTestDlg)
	enum { IDD = IDD_WAITFRAME };
	CStatic	m_CurrentStatus;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CTestDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CTestDlg)
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_TESTDLG_H__726B6739_1004_45C5_A74E_2E364A37717C__INCLUDED_)
