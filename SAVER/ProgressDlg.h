#if !defined(AFX_PROGRESSDLG_H__54E3D4CD_2395_4ACB_9C26_A39D893D996D__INCLUDED_)
#define AFX_PROGRESSDLG_H__54E3D4CD_2395_4ACB_9C26_A39D893D996D__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// ProgressDlg.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CProgressDlg dialog

class CProgressDlg : public CDialog
{
  int size;
// Construction
public:
	CProgressDlg(int size, CWnd* pParent = NULL);   // standard constructor

  void Step();
  BOOL Create();
  void Destroy();

// Dialog Data
	//{{AFX_DATA(CProgressDlg)
	enum { IDD = IDD_PROGRESS };
	CProgressCtrl	m_Progress;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CProgressDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CProgressDlg)
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_PROGRESSDLG_H__54E3D4CD_2395_4ACB_9C26_A39D893D996D__INCLUDED_)
