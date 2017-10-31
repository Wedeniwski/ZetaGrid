#if !defined(AFX_PORTDLG_H__5EB40D04_71B1_4891_97DA_D32595F9842F__INCLUDED_)
#define AFX_PORTDLG_H__5EB40D04_71B1_4891_97DA_D32595F9842F__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// PortDlg.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// PortDlg dialog

class PortDlg : public CDialog
{
// Construction
public:
	PortDlg(CWnd* pParent = NULL);   // standard constructor

// Dialog Data
	//{{AFX_DATA(PortDlg)
	enum { IDD = IDD_DIALOG_PORT };
	int		m_PortNumber;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(PortDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(PortDlg)
	virtual void OnOK();
	virtual BOOL OnInitDialog();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_PORTDLG_H__5EB40D04_71B1_4891_97DA_D32595F9842F__INCLUDED_)
