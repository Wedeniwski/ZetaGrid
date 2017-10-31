#if !defined(AFX_INSTALLDLG_H__4E3DA2DD_188E_47D4_AED3_9A07B3A52EAD__INCLUDED_)
#define AFX_INSTALLDLG_H__4E3DA2DD_188E_47D4_AED3_9A07B3A52EAD__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// InstallDlg.h : header file
//

/////////////////////////////////////////////////////////////////////////////
// CInstallDlg dialog

class CInstallDlg : public CDialog
{
// Construction
public:
	CInstallDlg(CWnd* pParent = NULL);   // standard constructor

  static BOOL StartupService();
  static void RestartService();
  static void ReinstallService(const Config& cfg);
  void StartInstall();
  static bool IsWindowsNT();
  static CString GetStartupPath();
  static void RemoveLink(const char* folderPath, const char* linkName);
  static bool CreateLink(const char* folderPath, const char* linkName, const char* linkArgs, const char* linkDescription, const char* iconPath, int iconIndex, const char* workingDirectory);
  static CString GetProxyName();

// Dialog Data
	//{{AFX_DATA(CInstallDlg)
	enum { IDD = IDD_DIALOG_INSTALL };
	CButton	m_CmdControlCenterStartup;
	CButton	m_ServiceControlCenterStartup;
	CButton	m_StartControlCenter;
	CButton	m_StartClient;
	CButton	m_CreateShortcut;
	CButton	m_ModeScreenSaver;
	CButton	m_ModeService;
	CButton	m_ModeCommandLine;
	//}}AFX_DATA


// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CInstallDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:

	// Generated message map functions
	//{{AFX_MSG(CInstallDlg)
	afx_msg void OnRadioModeService();
	afx_msg void OnRadioModeScreenSaver();
	virtual BOOL OnInitDialog();
	afx_msg void OnButtonHelp();
	virtual void OnOK();
	afx_msg void OnRadioModeCommandLine();
	afx_msg void OnCheckShortcut();
	afx_msg void OnCheckStartClient();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_INSTALLDLG_H__4E3DA2DD_188E_47D4_AED3_9A07B3A52EAD__INCLUDED_)
