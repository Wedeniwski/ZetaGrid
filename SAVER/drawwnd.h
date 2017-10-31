// drawwnd.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CDrawWnd window

#include "State.h"

class CDrawWnd : public CWnd
{
// Construction
public:
	CDrawWnd(BOOL bAutoDelete = TRUE);

// Attributes
public:
	static LPCTSTR m_lpszClassName;

// Operations
public:

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CDrawWnd)
	public:
	virtual BOOL Create(DWORD dwExStyle, DWORD dwStyle, const RECT& rect, CWnd* pParentWnd, UINT nID, CCreateContext* pContext = NULL);
	protected:
	virtual void PostNcDestroy();
	//}}AFX_VIRTUAL

// Implementation
public:
	virtual ~CDrawWnd();

protected:
	BOOL m_bAutoDelete;
  CRect m_rgnLast;

	// Generated message map functions
protected:
	//{{AFX_MSG(CDrawWnd)
	afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct);
	afx_msg void OnTimer(UINT nIDEvent);
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

/////////////////////////////////////////////////////////////////////////////
