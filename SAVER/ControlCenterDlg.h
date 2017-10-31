#if !defined(AFX_CONTROLCENTERDLG_H__8AFE296B_B13C_45BF_9CDE_FEEF4A912318__INCLUDED_)
#define AFX_CONTROLCENTERDLG_H__8AFE296B_B13C_45BF_9CDE_FEEF4A912318__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// ControlCenterDlg.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CControlCenterDlg dialog

#include "SystemTray.h"
#include "config.h"

class CControlCenterDlg : public CDialog
{
	CSystemTray m_TrayIcon;

  static bool transferEnabled;
  static CControlCenterDlg* running;
  bool updateView;
  bool systemTrayIsActive;
  bool clientIsActive;
  bool clientStopped;
  bool hideDlg;
  Config* lastConfig;

  CString getThreadIDs() const;
  void updateThreadIDBox();
  void StatusRefresh();

  virtual LRESULT OnTrayNotification(WPARAM uID, LPARAM lEvent);
  void OnButtonRefresh();

// Construction
public:
	CControlCenterDlg(CWnd* pParent = NULL);   // standard constructor
  ~CControlCenterDlg();

  void show();
  void hide();
  static bool IsTransferEnabled();
  static void StartBrowser(const char* url);
  static void StopComputation();
  static void RestartComputation(bool transferEnabled);
  static void Uninstall();
  static void StartServerConfig();

// Dialog Data
	//{{AFX_DATA(CControlCenterDlg)
	enum { IDD = IDD_DIALOG_CONTROL_CENTER };
	CStatic	m_ActiveThreads;
	CButton	m_HideButton;
	CStatic	m_Remaining;
	CStatic	m_ThreadIDText;
	CComboBox	m_ThreadID;
	CStatic	m_Performance;
	CStatic	m_ProgressText;
	CProgressCtrl	m_Active;
	CStatic	m_Percent;
	CButton	m_TransferButton;
	CStatic	m_CurrentStatus;
	CStatic	m_LocalWorkUnits;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CControlCenterDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CControlCenterDlg)
	afx_msg void OnButtonTransferWu();
	virtual BOOL OnInitDialog();
	afx_msg void OnTimer(UINT nIDEvent);
	afx_msg void OnConfigConfig();
	afx_msg void OnWebHome();
	afx_msg void OnWebNews();
	afx_msg void OnWebStatistic();
  afx_msg void OnLogViewInformationLog();
	afx_msg void OnHelpAbout();
	afx_msg void OnHelpFaq();
	afx_msg void OnSelchangeComboThreadId();
	afx_msg void OnHelpForum();
	afx_msg void OnWebStatisticDetail();
	afx_msg void OnWebTeammembers();
	afx_msg void OnButtonHide();
	afx_msg void OnClose();
	afx_msg void OnView();
	afx_msg void OnServerConfig();
  afx_msg void OnRestartComputation();
  afx_msg void OnStopComputation();
	afx_msg void OnPaint();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_CONTROLCENTERDLG_H__8AFE296B_B13C_45BF_9CDE_FEEF4A912318__INCLUDED_)
