#if !defined(AFX_GENERALPAGE_H__BA17FD71_A21D_4E4B_AB03_424194A6AD7B__INCLUDED_)
#define AFX_GENERALPAGE_H__BA17FD71_A21D_4E4B_AB03_424194A6AD7B__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// GeneralPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// GeneralPage dialog

class GeneralPage : public CPropertyPage
{
	DECLARE_DYNCREATE(GeneralPage)
  bool installMode;

// Construction
public:
	GeneralPage();
	~GeneralPage();

  void setInstallMode(bool installMode);
  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(bool information);

// Dialog Data
	//{{AFX_DATA(GeneralPage)
	enum { IDD = IDD_PROPPAGE_GENERAL };
	CEdit	m_eMail;
	CEdit	m_Name;
	CButton	m_ValuesChangedAtServer;
	CButton	m_NoteEMail;
	CButton	m_NoteName;
	CButton	m_NoteTeamname;
	CButton	m_ChangeAtServer;
	CEdit	m_Teamname;
	CButton	m_ActivateTeam;
	CButton	m_Messages;
	//}}AFX_DATA


// Overrides
	// ClassWizard generate virtual function overrides
	//{{AFX_VIRTUAL(GeneralPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	// Generated message map functions
	//{{AFX_MSG(GeneralPage)
	afx_msg void OnButtonNoteName();
	afx_msg void OnButtonNoteEmail();
	afx_msg void OnButtonNoteTeamname();
	afx_msg void OnCheckTeam();
	afx_msg void OnValuesChangedAtServer();
	afx_msg void OnButtonChangeAtServer();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_GENERALPAGE_H__BA17FD71_A21D_4E4B_AB03_424194A6AD7B__INCLUDED_)
