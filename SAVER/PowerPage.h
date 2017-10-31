#if !defined(AFX_POWERPAGE_H__0AA8E0B7_D881_4EB9_9963_593164259B68__INCLUDED_)
#define AFX_POWERPAGE_H__0AA8E0B7_D881_4EB9_9963_593164259B68__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
// PowerPage.h : header file
//

#include "config.h"

/////////////////////////////////////////////////////////////////////////////
// CPowerPage dialog

class CPowerPage : public CPropertyPage
{
	DECLARE_DYNCREATE(CPowerPage)

  static int CPowerPage::getTimeframe(const char* activeAt, const char* dayOfWeek, CTime& start, CTime& stop, CEdit& cpuUsage, const char* defaultCpuUsage);

// Construction
public:
	CPowerPage();
	~CPowerPage();

  void set(const Config& cfg);
  void get(Config& cfg);
  bool check(bool information);

// Dialog Data
	//{{AFX_DATA(CPowerPage)
	enum { IDD = IDD_PROPPAGE_POWER };
	CEdit	m_CPUWe3;
	CEdit	m_CPUWe2;
	CEdit	m_CPUWe1;
	CEdit	m_CPUSu1;
	CEdit	m_CPUTu3;
	CEdit	m_CPUTu2;
	CEdit	m_CPUTu1;
	CEdit	m_CPUTh3;
	CEdit	m_CPUTh2;
	CEdit	m_CPUTh1;
	CEdit	m_CPUSu3;
	CEdit	m_CPUSu2;
	CEdit	m_CPUSa3;
	CEdit	m_CPUSa2;
	CEdit	m_CPUSa1;
	CEdit	m_CPUMo3;
	CEdit	m_CPUMo2;
	CEdit	m_CPUMo1;
	CEdit	m_CPUFr3;
	CEdit	m_CPUFr2;
	CEdit	m_CPUFr1;
	CButton	m_CheckBatteryMode;
	CDateTimeCtrl	m_StartMo1;
	CDateTimeCtrl	m_StopMo1;
	CDateTimeCtrl	m_StartMo2;
	CDateTimeCtrl	m_StopMo2;
	CDateTimeCtrl	m_StartMo3;
	CDateTimeCtrl	m_StopMo3;
	CDateTimeCtrl	m_StartTu1;
	CDateTimeCtrl	m_StopTu1;
	CDateTimeCtrl	m_StartTu2;
	CDateTimeCtrl	m_StopTu2;
	CDateTimeCtrl	m_StartTu3;
	CDateTimeCtrl	m_StopTu3;
	CDateTimeCtrl	m_StartWe1;
	CDateTimeCtrl	m_StopWe1;
	CDateTimeCtrl	m_StartWe2;
	CDateTimeCtrl	m_StopWe2;
	CDateTimeCtrl	m_StartWe3;
	CDateTimeCtrl	m_StopWe3;
	CDateTimeCtrl	m_StartTh1;
	CDateTimeCtrl	m_StopTh1;
	CDateTimeCtrl	m_StartTh2;
	CDateTimeCtrl	m_StopTh2;
	CDateTimeCtrl	m_StartTh3;
	CDateTimeCtrl	m_StopTh3;
	CDateTimeCtrl	m_StartFr1;
	CDateTimeCtrl	m_StopFr1;
	CDateTimeCtrl	m_StartFr2;
	CDateTimeCtrl	m_StopFr2;
	CDateTimeCtrl	m_StartFr3;
	CDateTimeCtrl	m_StopFr3;
	CDateTimeCtrl	m_StartSa1;
	CDateTimeCtrl	m_StopSa1;
	CDateTimeCtrl	m_StartSa2;
	CDateTimeCtrl	m_StopSa2;
	CDateTimeCtrl	m_StartSa3;
	CDateTimeCtrl	m_StopSa3;
	CDateTimeCtrl	m_StartSu1;
	CDateTimeCtrl	m_StopSu1;
	CDateTimeCtrl	m_StartSu2;
	CDateTimeCtrl	m_StopSu2;
	CDateTimeCtrl	m_StartSu3;
	CDateTimeCtrl	m_StopSu3;
	CTime	StartMo1;
  CTime	StopMo1;
  CTime StartMo2;
  CTime StopMo2;
  CTime StartMo3;
  CTime StopMo3;
	CTime	StartTu1;
  CTime StopTu1;
  CTime StartTu2;
  CTime StopTu2;
  CTime StartTu3;
  CTime StopTu3;
	CTime	StartWe1;
  CTime StopWe1;
  CTime StartWe2;
  CTime StopWe2;
  CTime StartWe3;
  CTime StopWe3;
	CTime	StartTh1;
  CTime StopTh1;
  CTime StartTh2;
  CTime StopTh2;
  CTime StartTh3;
  CTime StopTh3;
	CTime	StartFr1;
  CTime StopFr1;
  CTime StartFr2;
  CTime StopFr2;
  CTime StartFr3;
  CTime StopFr3;
	CTime	StartSa1;
  CTime StopSa1;
  CTime StartSa2;
  CTime StopSa2;
  CTime StartSa3;
  CTime StopSa3;
	CTime	StartSu1;
  CTime StopSu1;
  CTime StartSu2;
  CTime StopSu2;
  CTime StartSu3;
  CTime StopSu3;
	//}}AFX_DATA


// Overrides
	// ClassWizard generate virtual function overrides
	//{{AFX_VIRTUAL(CPowerPage)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	// Generated message map functions
	//{{AFX_MSG(CPowerPage)
	afx_msg void OnDatetimechangeDatetimepicker(NMHDR* pNMHDR, LRESULT* pResult);
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_POWERPAGE_H__0AA8E0B7_D881_4EB9_9963_593164259B68__INCLUDED_)
