#if !defined(AFX_JAVAERROR_H__9FB82E20_09BF_4E23_A7D5_BE3B9FA27FAB__INCLUDED_)
#define AFX_JAVAERROR_H__9FB82E20_09BF_4E23_A7D5_BE3B9FA27FAB__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// JavaError.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CJavaError dialog

class CJavaError : public CDialog
{
// Construction
public:
	CJavaError(CWnd* pParent = NULL);   // standard constructor

  void SetFilename(const char* filename, const char* type);
// Dialog Data
	//{{AFX_DATA(CJavaError)
	enum { IDD = IDD_DIALOG_JAVA_ERROR };
	CString	m_ErrorText;
	CString	m_ErrorText2;
	CString	m_ErrorText3;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CJavaError)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CJavaError)
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_JAVAERROR_H__9FB82E20_09BF_4E23_A7D5_BE3B9FA27FAB__INCLUDED_)
