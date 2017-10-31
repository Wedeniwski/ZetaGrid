#if !defined(AFX_LICENSEDLG_H__9BBB480B_A241_4AEE_9ACC_A226F4E4F4BE__INCLUDED_)
#define AFX_LICENSEDLG_H__9BBB480B_A241_4AEE_9ACC_A226F4E4F4BE__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// LicenseDlg.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CLicenseDlg dialog

class CLicenseDlg : public CDialog
{
// Construction
public:
	CLicenseDlg(CWnd* pParent = NULL);   // standard constructor

// Dialog Data
	//{{AFX_DATA(CLicenseDlg)
	enum { IDD = IDD_DIALOG_LICENSE };
	CEdit	m_License;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CLicenseDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CLicenseDlg)
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_LICENSEDLG_H__9BBB480B_A241_4AEE_9ACC_A226F4E4F4BE__INCLUDED_)
