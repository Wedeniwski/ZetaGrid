// PowerPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "PowerPage.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CPowerPage property page

IMPLEMENT_DYNCREATE(CPowerPage, CPropertyPage)

CPowerPage::CPowerPage() : CPropertyPage(CPowerPage::IDD)
{
	//{{AFX_DATA_INIT(CPowerPage)
	//}}AFX_DATA_INIT
}

CPowerPage::~CPowerPage()
{
}

void CPowerPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CPowerPage)
	DDX_Control(pDX, IDC_CPU_USAGE_WE3, m_CPUWe3);
	DDX_Control(pDX, IDC_CPU_USAGE_WE2, m_CPUWe2);
	DDX_Control(pDX, IDC_CPU_USAGE_WE1, m_CPUWe1);
	DDX_Control(pDX, IDC_CPU_USAGE_SU1, m_CPUSu1);
	DDX_Control(pDX, IDC_CPU_USAGE_TU3, m_CPUTu3);
	DDX_Control(pDX, IDC_CPU_USAGE_TU2, m_CPUTu2);
	DDX_Control(pDX, IDC_CPU_USAGE_TU1, m_CPUTu1);
	DDX_Control(pDX, IDC_CPU_USAGE_TH3, m_CPUTh3);
	DDX_Control(pDX, IDC_CPU_USAGE_TH2, m_CPUTh2);
	DDX_Control(pDX, IDC_CPU_USAGE_TH1, m_CPUTh1);
	DDX_Control(pDX, IDC_CPU_USAGE_SU3, m_CPUSu3);
	DDX_Control(pDX, IDC_CPU_USAGE_SU2, m_CPUSu2);
	DDX_Control(pDX, IDC_CPU_USAGE_SA3, m_CPUSa3);
	DDX_Control(pDX, IDC_CPU_USAGE_SA2, m_CPUSa2);
	DDX_Control(pDX, IDC_CPU_USAGE_SA1, m_CPUSa1);
	DDX_Control(pDX, IDC_CPU_USAGE_MO3, m_CPUMo3);
	DDX_Control(pDX, IDC_CPU_USAGE_MO2, m_CPUMo2);
	DDX_Control(pDX, IDC_CPU_USAGE_MO1, m_CPUMo1);
	DDX_Control(pDX, IDC_CPU_USAGE_FR3, m_CPUFr3);
	DDX_Control(pDX, IDC_CPU_USAGE_FR2, m_CPUFr2);
	DDX_Control(pDX, IDC_CPU_USAGE_FR1, m_CPUFr1);
	DDX_Control(pDX, IDC_BATTERY_MODE, m_CheckBatteryMode);
	DDX_Control(pDX, IDC_DATETIMEPICKER_MO1, m_StartMo1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_MO2, m_StopMo1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_MO3, m_StartMo2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_MO4, m_StopMo2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_MO5, m_StartMo3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_MO6, m_StopMo3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TU1, m_StartTu1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TU2, m_StopTu1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TU3, m_StartTu2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TU4, m_StopTu2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TU5, m_StartTu3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TU6, m_StopTu3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_WE1, m_StartWe1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_WE2, m_StopWe1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_WE3, m_StartWe2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_WE4, m_StopWe2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_WE5, m_StartWe3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_WE6, m_StopWe3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TH1, m_StartTh1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TH2, m_StopTh1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TH3, m_StartTh2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TH4, m_StopTh2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TH5, m_StartTh3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_TH6, m_StopTh3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FR1, m_StartFr1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FR2, m_StopFr1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FR3, m_StartFr2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FR4, m_StopFr2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FR5, m_StartFr3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_FR6, m_StopFr3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SA1, m_StartSa1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SA2, m_StopSa1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SA3, m_StartSa2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SA4, m_StopSa2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SA5, m_StartSa3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SA6, m_StopSa3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SU1, m_StartSu1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SU2, m_StopSu1);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SU3, m_StartSu2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SU4, m_StopSu2);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SU5, m_StartSu3);
	DDX_Control(pDX, IDC_DATETIMEPICKER_SU6, m_StopSu3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_MO1, StartMo1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_MO2, StopMo1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_MO3, StartMo2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_MO4, StopMo2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_MO5, StartMo3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_MO6, StopMo3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TU1, StartTu1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TU2, StopTu1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TU3, StartTu2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TU4, StopTu2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TU5, StartTu3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TU6, StopTu3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_WE1, StartWe1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_WE2, StopWe1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_WE3, StartWe2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_WE4, StopWe2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_WE5, StartWe3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_WE6, StopWe3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TH1, StartTh1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TH2, StopTh1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TH3, StartTh2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TH4, StopTh2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TH5, StartTh3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_TH6, StopTh3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_FR1, StartFr1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_FR2, StopFr1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_FR3, StartFr2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_FR4, StopFr2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_FR5, StartFr3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_FR6, StopFr3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SA1, StartSa1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SA2, StopSa1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SA3, StartSa2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SA4, StopSa2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SA5, StartSa3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SA6, StopSa3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SU1, StartSu1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SU2, StopSu1);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SU3, StartSu2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SU4, StopSu2);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SU5, StartSu3);
	DDX_DateTimeCtrl(pDX, IDC_DATETIMEPICKER_SU6, StopSu3);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CPowerPage, CPropertyPage)
	//{{AFX_MSG_MAP(CPowerPage)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_MO1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_MO2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_MO3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_MO4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_MO5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_MO6, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TU1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TU2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TU3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TU4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TU5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TU6, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_WE1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_WE2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_WE3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_WE4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_WE5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_WE6, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TH1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TH2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TH3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TH4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TH5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_TH6, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_FR1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_FR2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_FR3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_FR4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_FR5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_FR6, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SA1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SA2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SA3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SA4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SA5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SA6, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SU1, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SU2, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SU3, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SU4, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SU5, OnDatetimechangeDatetimepicker)
	ON_NOTIFY(DTN_DATETIMECHANGE, IDC_DATETIMEPICKER_SU6, OnDatetimechangeDatetimepicker)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CPowerPage message handlers

void CPowerPage::set(const Config& cfg)
{
  UpdateData(TRUE);
	m_CheckBatteryMode.SetCheck(cfg.checkBatteryMode);
  char defaultCpuUsage[20];
  itoa(cfg.processorsUsage, defaultCpuUsage, 10);
  const char* activeAt = cfg.activeAt.c_str();
  int i = getTimeframe(activeAt, "Mo", StartMo1, StopMo1, m_CPUMo1, defaultCpuUsage);
  int j = getTimeframe(activeAt+i, "Mo", StartMo2, StopMo2, m_CPUMo2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "Mo", StartMo3, StopMo3, m_CPUMo3, defaultCpuUsage);
  i = getTimeframe(activeAt, "Tu", StartTu1, StopTu1, m_CPUTu1, defaultCpuUsage);
  j = getTimeframe(activeAt+i, "Tu", StartTu2, StopTu2, m_CPUTu2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "Tu", StartTu3, StopTu3, m_CPUTu3, defaultCpuUsage);
  i = getTimeframe(activeAt, "We", StartWe1, StopWe1, m_CPUWe1, defaultCpuUsage);
  j = getTimeframe(activeAt+i, "We", StartWe2, StopWe2, m_CPUWe2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "We", StartWe3, StopWe3, m_CPUWe3, defaultCpuUsage);
  i = getTimeframe(activeAt, "Th", StartTh1, StopTh1, m_CPUTh1, defaultCpuUsage);
  j = getTimeframe(activeAt+i, "Th", StartTh2, StopTh2, m_CPUTh2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "Th", StartTh3, StopTh3, m_CPUTh3, defaultCpuUsage);
  i = getTimeframe(activeAt, "Fr", StartFr1, StopFr1, m_CPUFr1, defaultCpuUsage);
  j = getTimeframe(activeAt+i, "Fr", StartFr2, StopFr2, m_CPUFr2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "Fr", StartFr3, StopFr3, m_CPUFr3, defaultCpuUsage);
  i = getTimeframe(activeAt, "Sa", StartSa1, StopSa1, m_CPUSa1, defaultCpuUsage);
  j = getTimeframe(activeAt+i, "Sa", StartSa2, StopSa2, m_CPUSa2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "Sa", StartSa3, StopSa3, m_CPUSa3, defaultCpuUsage);
  i = getTimeframe(activeAt, "Su", StartSu1, StopSu1, m_CPUSu1, defaultCpuUsage);
  j = getTimeframe(activeAt+i, "Su", StartSu2, StopSu2, m_CPUSu2, defaultCpuUsage);
  getTimeframe(activeAt+max(i, i+j), "Su", StartSu3, StopSu3, m_CPUSu3, defaultCpuUsage);

  m_StartMo1.SetFormat("HH:mm");
  m_StartMo2.SetFormat("HH:mm");
  m_StartMo3.SetFormat("HH:mm");
  m_StopMo1.SetFormat("HH:mm");
  m_StopMo2.SetFormat("HH:mm");
  m_StopMo3.SetFormat("HH:mm");
  m_StartMo1.SetTime(&StartMo1);
  m_StartMo2.SetTime(&StartMo2);
  m_StartMo3.SetTime(&StartMo3);
  m_StopMo1.SetTime(&StopMo1);
  m_StopMo2.SetTime(&StopMo2);
  m_StopMo3.SetTime(&StopMo3);
  m_StartTu1.SetFormat("HH:mm");
  m_StartTu2.SetFormat("HH:mm");
  m_StartTu3.SetFormat("HH:mm");
  m_StopTu1.SetFormat("HH:mm");
  m_StopTu2.SetFormat("HH:mm");
  m_StopTu3.SetFormat("HH:mm");
  m_StartTu1.SetTime(&StartTu1);
  m_StartTu2.SetTime(&StartTu2);
  m_StartTu3.SetTime(&StartTu3);
  m_StopTu1.SetTime(&StopTu1);
  m_StopTu2.SetTime(&StopTu2);
  m_StopTu3.SetTime(&StopTu3);
  m_StartWe1.SetFormat("HH:mm");
  m_StartWe2.SetFormat("HH:mm");
  m_StartWe3.SetFormat("HH:mm");
  m_StopWe1.SetFormat("HH:mm");
  m_StopWe2.SetFormat("HH:mm");
  m_StopWe3.SetFormat("HH:mm");
  m_StartWe1.SetTime(&StartWe1);
  m_StartWe2.SetTime(&StartWe2);
  m_StartWe3.SetTime(&StartWe3);
  m_StopWe1.SetTime(&StopWe1);
  m_StopWe2.SetTime(&StopWe2);
  m_StopWe3.SetTime(&StopWe3);
  m_StartTh1.SetFormat("HH:mm");
  m_StartTh2.SetFormat("HH:mm");
  m_StartTh3.SetFormat("HH:mm");
  m_StopTh1.SetFormat("HH:mm");
  m_StopTh2.SetFormat("HH:mm");
  m_StopTh3.SetFormat("HH:mm");
  m_StartTh1.SetTime(&StartTh1);
  m_StartTh2.SetTime(&StartTh2);
  m_StartTh3.SetTime(&StartTh3);
  m_StopTh1.SetTime(&StopTh1);
  m_StopTh2.SetTime(&StopTh2);
  m_StopTh3.SetTime(&StopTh3);
  m_StartFr1.SetFormat("HH:mm");
  m_StartFr2.SetFormat("HH:mm");
  m_StartFr3.SetFormat("HH:mm");
  m_StopFr1.SetFormat("HH:mm");
  m_StopFr2.SetFormat("HH:mm");
  m_StopFr3.SetFormat("HH:mm");
  m_StartFr1.SetTime(&StartFr1);
  m_StartFr2.SetTime(&StartFr2);
  m_StartFr3.SetTime(&StartFr3);
  m_StopFr1.SetTime(&StopFr1);
  m_StopFr2.SetTime(&StopFr2);
  m_StopFr3.SetTime(&StopFr3);
  m_StartSa1.SetFormat("HH:mm");
  m_StartSa2.SetFormat("HH:mm");
  m_StartSa3.SetFormat("HH:mm");
  m_StopSa1.SetFormat("HH:mm");
  m_StopSa2.SetFormat("HH:mm");
  m_StopSa3.SetFormat("HH:mm");
  m_StartSa1.SetTime(&StartSa1);
  m_StartSa2.SetTime(&StartSa2);
  m_StartSa3.SetTime(&StartSa3);
  m_StopSa1.SetTime(&StopSa1);
  m_StopSa2.SetTime(&StopSa2);
  m_StopSa3.SetTime(&StopSa3);
  m_StartSu1.SetFormat("HH:mm");
  m_StartSu2.SetFormat("HH:mm");
  m_StartSu3.SetFormat("HH:mm");
  m_StopSu1.SetFormat("HH:mm");
  m_StopSu2.SetFormat("HH:mm");
  m_StopSu3.SetFormat("HH:mm");
  m_StartSu1.SetTime(&StartSu1);
  m_StartSu2.SetTime(&StartSu2);
  m_StartSu3.SetTime(&StartSu3);
  m_StopSu1.SetTime(&StopSu1);
  m_StopSu2.SetTime(&StopSu2);
  m_StopSu3.SetTime(&StopSu3);
  LRESULT result;
  OnDatetimechangeDatetimepicker(0, &result);
  UpdateData(FALSE);
}

void CPowerPage::get(Config& cfg)
{
  char c[50];
  UpdateData(TRUE);
  cfg.checkBatteryMode = bool(m_CheckBatteryMode.GetCheck());
  cfg.activeAt = "";
  char defaultCpuUsage[20];
  itoa(cfg.processorsUsage, defaultCpuUsage, 10);
  CString s;
  m_CPUMo1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "Mo%02d:%02d-%02d:%02d", StartMo1.GetHour(), StartMo1.GetMinute(), StopMo1.GetHour(), StopMo1.GetMinute());
  } else {
    sprintf(c, "Mo%02d:%02d-%02d:%02d,%d", StartMo1.GetHour(), StartMo1.GetMinute(), StopMo1.GetHour(), StopMo1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartMo2.IsWindowEnabled() && (StartMo2.GetHour() != 0 || StartMo2.GetMinute() != 0 || StopMo2.GetHour() != 0 || StopMo2.GetMinute() != 0)) {
    m_CPUMo2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Mo%02d:%02d-%02d:%02d", StartMo2.GetHour(), StartMo2.GetMinute(), StopMo2.GetHour(), StopMo2.GetMinute());
    } else {
      sprintf(c, "Mo%02d:%02d-%02d:%02d,%d", StartMo2.GetHour(), StartMo2.GetMinute(), StopMo2.GetHour(), StopMo2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartMo3.IsWindowEnabled() && (StartMo3.GetHour() != 0 || StartMo3.GetMinute() != 0 || StopMo3.GetHour() != 0 || StopMo3.GetMinute() != 0)) {
    m_CPUMo3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Mo%02d:%02d-%02d:%02d", StartMo3.GetHour(), StartMo3.GetMinute(), StopMo3.GetHour(), StopMo3.GetMinute());
    } else {
      sprintf(c, "Mo%02d:%02d-%02d:%02d,%d", StartMo3.GetHour(), StartMo3.GetMinute(), StopMo3.GetHour(), StopMo3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  m_CPUTu1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "Tu%02d:%02d-%02d:%02d", StartTu1.GetHour(), StartTu1.GetMinute(), StopTu1.GetHour(), StopTu1.GetMinute());
  } else {
    sprintf(c, "Tu%02d:%02d-%02d:%02d,%d", StartTu1.GetHour(), StartTu1.GetMinute(), StopTu1.GetHour(), StopTu1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartTu2.IsWindowEnabled() && (StartTu2.GetHour() != 0 || StartTu2.GetMinute() != 0 || StopTu2.GetHour() != 0 || StopTu2.GetMinute() != 0)) {
    m_CPUTu2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Tu%02d:%02d-%02d:%02d", StartTu2.GetHour(), StartTu2.GetMinute(), StopTu2.GetHour(), StopTu2.GetMinute());
    } else {
      sprintf(c, "Tu%02d:%02d-%02d:%02d,%d", StartTu2.GetHour(), StartTu2.GetMinute(), StopTu2.GetHour(), StopTu2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartTu3.IsWindowEnabled() && (StartTu3.GetHour() != 0 || StartTu3.GetMinute() != 0 || StopTu3.GetHour() != 0 || StopTu3.GetMinute() != 0)) {
    m_CPUTu3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Tu%02d:%02d-%02d:%02d", StartTu3.GetHour(), StartTu3.GetMinute(), StopTu3.GetHour(), StopTu3.GetMinute());
    } else {
      sprintf(c, "Tu%02d:%02d-%02d:%02d,%d", StartTu3.GetHour(), StartTu3.GetMinute(), StopTu3.GetHour(), StopTu3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  m_CPUWe1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "We%02d:%02d-%02d:%02d", StartWe1.GetHour(), StartWe1.GetMinute(), StopWe1.GetHour(), StopWe1.GetMinute());
  } else {
    sprintf(c, "We%02d:%02d-%02d:%02d,%d", StartWe1.GetHour(), StartWe1.GetMinute(), StopWe1.GetHour(), StopWe1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartWe2.IsWindowEnabled() && (StartWe2.GetHour() != 0 || StartWe2.GetMinute() != 0 || StopWe2.GetHour() != 0 || StopWe2.GetMinute() != 0)) {
    m_CPUWe2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "We%02d:%02d-%02d:%02d", StartWe2.GetHour(), StartWe2.GetMinute(), StopWe2.GetHour(), StopWe2.GetMinute());
    } else {
      sprintf(c, "We%02d:%02d-%02d:%02d,%d", StartWe2.GetHour(), StartWe2.GetMinute(), StopWe2.GetHour(), StopWe2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartWe3.IsWindowEnabled() && (StartWe3.GetHour() != 0 || StartWe3.GetMinute() != 0 || StopWe3.GetHour() != 0 || StopWe3.GetMinute() != 0)) {
    m_CPUWe3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "We%02d:%02d-%02d:%02d", StartWe3.GetHour(), StartWe3.GetMinute(), StopWe3.GetHour(), StopWe3.GetMinute());
    } else {
      sprintf(c, "We%02d:%02d-%02d:%02d,%d", StartWe3.GetHour(), StartWe3.GetMinute(), StopWe3.GetHour(), StopWe3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  m_CPUTh1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "Th%02d:%02d-%02d:%02d", StartTh1.GetHour(), StartTh1.GetMinute(), StopTh1.GetHour(), StopTh1.GetMinute());
  } else {
    sprintf(c, "Th%02d:%02d-%02d:%02d,%d", StartTh1.GetHour(), StartTh1.GetMinute(), StopTh1.GetHour(), StopTh1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartTh2.IsWindowEnabled() && (StartTh2.GetHour() != 0 || StartTh2.GetMinute() != 0 || StopTh2.GetHour() != 0 || StopTh2.GetMinute() != 0)) {
    m_CPUTh2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Th%02d:%02d-%02d:%02d", StartTh2.GetHour(), StartTh2.GetMinute(), StopTh2.GetHour(), StopTh2.GetMinute());
    } else {
      sprintf(c, "Th%02d:%02d-%02d:%02d,%d", StartTh2.GetHour(), StartTh2.GetMinute(), StopTh2.GetHour(), StopTh2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartTh3.IsWindowEnabled() && (StartTh3.GetHour() != 0 || StartTh3.GetMinute() != 0 || StopTh3.GetHour() != 0 || StopTh3.GetMinute() != 0)) {
    m_CPUTh3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Th%02d:%02d-%02d:%02d", StartTh3.GetHour(), StartTh3.GetMinute(), StopTh3.GetHour(), StopTh3.GetMinute());
    } else {
      sprintf(c, "Th%02d:%02d-%02d:%02d,%d", StartTh3.GetHour(), StartTh3.GetMinute(), StopTh3.GetHour(), StopTh3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  m_CPUFr1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "Fr%02d:%02d-%02d:%02d", StartFr1.GetHour(), StartFr1.GetMinute(), StopFr1.GetHour(), StopFr1.GetMinute());
  } else {
    sprintf(c, "Fr%02d:%02d-%02d:%02d,%d", StartFr1.GetHour(), StartFr1.GetMinute(), StopFr1.GetHour(), StopFr1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartFr2.IsWindowEnabled() && (StartFr2.GetHour() != 0 || StartFr2.GetMinute() != 0 || StopFr2.GetHour() != 0 || StopFr2.GetMinute() != 0)) {
    m_CPUFr2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Fr%02d:%02d-%02d:%02d", StartFr2.GetHour(), StartFr2.GetMinute(), StopFr2.GetHour(), StopFr2.GetMinute());
    } else {
      sprintf(c, "Fr%02d:%02d-%02d:%02d,%d", StartFr2.GetHour(), StartFr2.GetMinute(), StopFr2.GetHour(), StopFr2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartFr3.IsWindowEnabled() && (StartFr3.GetHour() != 0 || StartFr3.GetMinute() != 0 || StopFr3.GetHour() != 0 || StopFr3.GetMinute() != 0)) {
    m_CPUFr3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Fr%02d:%02d-%02d:%02d", StartFr3.GetHour(), StartFr3.GetMinute(), StopFr3.GetHour(), StopFr3.GetMinute());
    } else {
      sprintf(c, "Fr%02d:%02d-%02d:%02d,%d", StartFr3.GetHour(), StartFr3.GetMinute(), StopFr3.GetHour(), StopFr3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  m_CPUSa1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "Sa%02d:%02d-%02d:%02d", StartSa1.GetHour(), StartSa1.GetMinute(), StopSa1.GetHour(), StopSa1.GetMinute());
  } else {
    sprintf(c, "Sa%02d:%02d-%02d:%02d,%d", StartSa1.GetHour(), StartSa1.GetMinute(), StopSa1.GetHour(), StopSa1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartSa2.IsWindowEnabled() && (StartSa2.GetHour() != 0 || StartSa2.GetMinute() != 0 || StopSa2.GetHour() != 0 || StopSa2.GetMinute() != 0)) {
    m_CPUSa2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Sa%02d:%02d-%02d:%02d", StartSa2.GetHour(), StartSa2.GetMinute(), StopSa2.GetHour(), StopSa2.GetMinute());
    } else {
      sprintf(c, "Sa%02d:%02d-%02d:%02d,%d", StartSa2.GetHour(), StartSa2.GetMinute(), StopSa2.GetHour(), StopSa2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartSa3.IsWindowEnabled() && (StartSa3.GetHour() != 0 || StartSa3.GetMinute() != 0 || StopSa3.GetHour() != 0 || StopSa3.GetMinute() != 0)) {
    m_CPUSa3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Sa%02d:%02d-%02d:%02d", StartSa3.GetHour(), StartSa3.GetMinute(), StopSa3.GetHour(), StopSa3.GetMinute());
    } else {
      sprintf(c, "Sa%02d:%02d-%02d:%02d,%d", StartSa3.GetHour(), StartSa3.GetMinute(), StopSa3.GetHour(), StopSa3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  m_CPUSu1.GetWindowText(s);
  if (s == defaultCpuUsage) {
    sprintf(c, "Su%02d:%02d-%02d:%02d", StartSu1.GetHour(), StartSu1.GetMinute(), StopSu1.GetHour(), StopSu1.GetMinute());
  } else {
    sprintf(c, "Su%02d:%02d-%02d:%02d,%d", StartSu1.GetHour(), StartSu1.GetMinute(), StopSu1.GetHour(), StopSu1.GetMinute(), atoi(s));
  }
  cfg.activeAt += c;
  if (m_StartSu2.IsWindowEnabled() && (StartSu2.GetHour() != 0 || StartSu2.GetMinute() != 0 || StopSu2.GetHour() != 0 || StopSu2.GetMinute() != 0)) {
    m_CPUSu2.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Su%02d:%02d-%02d:%02d", StartSu2.GetHour(), StartSu2.GetMinute(), StopSu2.GetHour(), StopSu2.GetMinute());
    } else {
      sprintf(c, "Su%02d:%02d-%02d:%02d,%d", StartSu2.GetHour(), StartSu2.GetMinute(), StopSu2.GetHour(), StopSu2.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (m_StartSu3.IsWindowEnabled() && (StartSu3.GetHour() != 0 || StartSu3.GetMinute() != 0 || StopSu3.GetHour() != 0 || StopSu3.GetMinute() != 0)) {
    m_CPUSu3.GetWindowText(s);
    if (s == defaultCpuUsage) {
      sprintf(c, "Su%02d:%02d-%02d:%02d", StartSu3.GetHour(), StartSu3.GetMinute(), StopSu3.GetHour(), StopSu3.GetMinute());
    } else {
      sprintf(c, "Su%02d:%02d-%02d:%02d,%d", StartSu3.GetHour(), StartSu3.GetMinute(), StopSu3.GetHour(), StopSu3.GetMinute(), atoi(s));
    }
    cfg.activeAt += c;
  }
  if (cfg.activeAt.find('1') == string::npos && cfg.activeAt.find('2') == string::npos && cfg.activeAt.find('3') == string::npos && cfg.activeAt.find('4') == string::npos
      && cfg.activeAt.find('5') == string::npos && cfg.activeAt.find('6') == string::npos && cfg.activeAt.find('7') == string::npos
      && cfg.activeAt.find('8') == string::npos && cfg.activeAt.find('9') == string::npos) {
    cfg.activeAt = "";
  }
}

bool CPowerPage::check(bool information)
{
  UpdateData(TRUE);
  // the CPU usage must be between 0 and 100
  CString cpu;
  m_CPUMo1.GetWindowText(cpu);
  int usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUMo1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Mo", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUMo2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUMo2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Mo", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUMo3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUMo3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Mo", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUTu1.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUTu1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Tu", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUTu2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUTu2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Tu", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUTu3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUTu3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Tu", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUWe1.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUWe1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "We", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUWe2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUWe2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "We", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUWe3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUWe3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "We", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUTh1.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUTh1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Th", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUTh2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUTh2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Th", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUTh3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUTh3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Th", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUFr1.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUFr1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Fr", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUFr2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUFr2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Fr", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUFr3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUFr3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Fr", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUSa1.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUSa1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Sa", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUSa2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUSa2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Sa", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUSa3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUSa3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Sa", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUSu1.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUSu1.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Su", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUSu2.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUSu2.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Su", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  m_CPUSu3.GetWindowText(cpu);
  usage = atoi(cpu);
  if (usage < 0 || usage > 100) {
    m_CPUSu3.SetFocus();
    CString s,s2;
    s.Format(IDS_CPU_USAGE_BETWEEN_0_100, "Su", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  // the end of the timeframe must be after the begin, except midnight
  if ((StopMo1.GetHour() != 0 || StopMo1.GetMinute() != 0) && (StopMo1.GetHour() < StartMo1.GetHour() || StopMo1.GetHour() == StartMo1.GetHour() && StopMo1.GetMinute() <= StartMo1.GetMinute())) {
    m_StopMo1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Mo", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopMo2.GetHour() != 0 || StopMo2.GetMinute() != 0) && (StopMo2.GetHour() < StartMo2.GetHour() || StopMo2.GetHour() == StartMo2.GetHour() && StopMo2.GetMinute() <= StartMo2.GetMinute())) {
    m_StopMo2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Mo", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopMo3.GetHour() != 0 || StopMo3.GetMinute() != 0) && (StopMo3.GetHour() < StartMo3.GetHour() || StopMo3.GetHour() == StartMo3.GetHour() && StopMo3.GetMinute() <= StartMo3.GetMinute())) {
    m_StopMo3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Mo", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopTu1.GetHour() != 0 || StopTu1.GetMinute() != 0) && (StopTu1.GetHour() < StartTu1.GetHour() || StopTu1.GetHour() == StartTu1.GetHour() && StopTu1.GetMinute() <= StartTu1.GetMinute())) {
    m_StopTu1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Tu", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopTu2.GetHour() != 0 || StopTu2.GetMinute() != 0) && (StopTu2.GetHour() < StartTu2.GetHour() || StopTu2.GetHour() == StartTu2.GetHour() && StopTu2.GetMinute() <= StartTu2.GetMinute())) {
    m_StopTu2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Tu", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopTu3.GetHour() != 0 || StopTu3.GetMinute() != 0) && (StopTu3.GetHour() < StartTu3.GetHour() || StopTu3.GetHour() == StartTu3.GetHour() && StopTu3.GetMinute() <= StartTu3.GetMinute())) {
    m_StopTu3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Tu", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopWe1.GetHour() != 0 || StopWe1.GetMinute() != 0) && (StopWe1.GetHour() < StartWe1.GetHour() || StopWe1.GetHour() == StartWe1.GetHour() && StopWe1.GetMinute() <= StartWe1.GetMinute())) {
    m_StopWe1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "We", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopWe2.GetHour() != 0 || StopWe2.GetMinute() != 0) && (StopWe2.GetHour() < StartWe2.GetHour() || StopWe2.GetHour() == StartWe2.GetHour() && StopWe2.GetMinute() <= StartWe2.GetMinute())) {
    m_StopWe2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "We", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopWe3.GetHour() != 0 || StopWe3.GetMinute() != 0) && (StopWe3.GetHour() < StartWe3.GetHour() || StopWe3.GetHour() == StartWe3.GetHour() && StopWe3.GetMinute() <= StartWe3.GetMinute())) {
    m_StopWe3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "We", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopTh1.GetHour() != 0 || StopTh1.GetMinute() != 0) && (StopTh1.GetHour() < StartTh1.GetHour() || StopTh1.GetHour() == StartTh1.GetHour() && StopTh1.GetMinute() <= StartTh1.GetMinute())) {
    m_StopTh1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Th", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopTh2.GetHour() != 0 || StopTh2.GetMinute() != 0) && (StopTh2.GetHour() < StartTh2.GetHour() || StopTh2.GetHour() == StartTh2.GetHour() && StopTh2.GetMinute() <= StartTh2.GetMinute())) {
    m_StopTh2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Th", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopTh3.GetHour() != 0 || StopTh3.GetMinute() != 0) && (StopTh3.GetHour() < StartTh3.GetHour() || StopTh3.GetHour() == StartTh3.GetHour() && StopTh3.GetMinute() <= StartTh3.GetMinute())) {
    m_StopTh3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Th", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopFr1.GetHour() != 0 || StopFr1.GetMinute() != 0) && (StopFr1.GetHour() < StartFr1.GetHour() || StopFr1.GetHour() == StartFr1.GetHour() && StopFr1.GetMinute() <= StartFr1.GetMinute())) {
    m_StopFr1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Fr", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopFr2.GetHour() != 0 || StopFr2.GetMinute() != 0) && (StopFr2.GetHour() < StartFr2.GetHour() || StopFr2.GetHour() == StartFr2.GetHour() && StopFr2.GetMinute() <= StartFr2.GetMinute())) {
    m_StopFr2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Fr", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopFr3.GetHour() != 0 || StopFr3.GetMinute() != 0) && (StopFr3.GetHour() < StartFr3.GetHour() || StopFr3.GetHour() == StartFr3.GetHour() && StopFr3.GetMinute() <= StartFr3.GetMinute())) {
    m_StopFr3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Fr", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopSa1.GetHour() != 0 || StopSa1.GetMinute() != 0) && (StopSa1.GetHour() < StartSa1.GetHour() || StopSa1.GetHour() == StartSa1.GetHour() && StopSa1.GetMinute() <= StartSa1.GetMinute())) {
    m_StopSa1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Sa", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopSa2.GetHour() != 0 || StopSa2.GetMinute() != 0) && (StopSa2.GetHour() < StartSa2.GetHour() || StopSa2.GetHour() == StartSa2.GetHour() && StopSa2.GetMinute() <= StartSa2.GetMinute())) {
    m_StopSa2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Sa", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopSa3.GetHour() != 0 || StopSa3.GetMinute() != 0) && (StopSa3.GetHour() < StartSa3.GetHour() || StopSa3.GetHour() == StartSa3.GetHour() && StopSa3.GetMinute() <= StartSa3.GetMinute())) {
    m_StopSa3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Sa", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopSu1.GetHour() != 0 || StopSu1.GetMinute() != 0) && (StopSu1.GetHour() < StartSu1.GetHour() || StopSu1.GetHour() == StartSu1.GetHour() && StopSu1.GetMinute() <= StartSu1.GetMinute())) {
    m_StopSu1.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Su", 1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopSu2.GetHour() != 0 || StopSu2.GetMinute() != 0) && (StopSu2.GetHour() < StartSu2.GetHour() || StopSu2.GetHour() == StartSu2.GetHour() && StopSu2.GetMinute() <= StartSu2.GetMinute())) {
    m_StopSu2.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Su", 2);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if ((StopSu3.GetHour() != 0 || StopSu3.GetMinute() != 0) && (StopSu3.GetHour() < StartSu3.GetHour() || StopSu3.GetHour() == StartSu3.GetHour() && StopSu3.GetMinute() <= StartSu3.GetMinute())) {
    m_StopSu3.SetFocus();
    CString s,s2;
    s.Format(IDS_TIMEFRAME_ENDS_AFTER_BEGINS, "Su", 3);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  // the begin of the second timeframe must be after the end of the first timeframe
  if (m_StartMo2.IsWindowEnabled() && (StartMo2.GetHour() != 0 || StartMo2.GetMinute() != 0 || StopMo2.GetHour() != 0 || StopMo2.GetMinute() != 0) && (StopMo1.GetHour() > StartMo2.GetHour() || StopMo1.GetHour() == StartMo2.GetHour() && StopMo1.GetMinute() >= StartMo2.GetMinute())) {
    m_StartMo2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "Mo");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartTu2.IsWindowEnabled() && (StartTu2.GetHour() != 0 || StartTu2.GetMinute() != 0 || StopTu2.GetHour() != 0 || StopTu2.GetMinute() != 0) && (StopTu1.GetHour() > StartTu2.GetHour() || StopTu1.GetHour() == StartTu2.GetHour() && StopTu1.GetMinute() >= StartTu2.GetMinute())) {
    m_StartTu2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "Tu");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartWe2.IsWindowEnabled() && (StartWe2.GetHour() != 0 || StartWe2.GetMinute() != 0 || StopWe2.GetHour() != 0 || StopWe2.GetMinute() != 0) && (StopWe1.GetHour() > StartWe2.GetHour() || StopWe1.GetHour() == StartWe2.GetHour() && StopWe1.GetMinute() >= StartWe2.GetMinute())) {
    m_StartWe2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "We");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartTh2.IsWindowEnabled() && (StartTh2.GetHour() != 0 || StartTh2.GetMinute() != 0 || StopTh2.GetHour() != 0 || StopTh2.GetMinute() != 0) && (StopTh1.GetHour() > StartTh2.GetHour() || StopTh1.GetHour() == StartTh2.GetHour() && StopTh1.GetMinute() >= StartTh2.GetMinute())) {
    m_StartTh2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "Th");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartFr2.IsWindowEnabled() && (StartFr2.GetHour() != 0 || StartFr2.GetMinute() != 0 || StopFr2.GetHour() != 0 || StopFr2.GetMinute() != 0) && (StopFr1.GetHour() > StartFr2.GetHour() || StopFr1.GetHour() == StartFr2.GetHour() && StopFr1.GetMinute() >= StartFr2.GetMinute())) {
    m_StartFr2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "Fr");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartSa2.IsWindowEnabled() && (StartSa2.GetHour() != 0 || StartSa2.GetMinute() != 0 || StopSa2.GetHour() != 0 || StopSa2.GetMinute() != 0) && (StopSa1.GetHour() > StartSa2.GetHour() || StopSa1.GetHour() == StartSa2.GetHour() && StopSa1.GetMinute() >= StartSa2.GetMinute())) {
    m_StartSa2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "Sa");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartSu2.IsWindowEnabled() && (StartSu2.GetHour() != 0 || StartSu2.GetMinute() != 0 || StopSu2.GetHour() != 0 || StopSu2.GetMinute() != 0) && (StopSu1.GetHour() > StartSu2.GetHour() || StopSu1.GetHour() == StartSu2.GetHour() && StopSu1.GetMinute() >= StartSu2.GetMinute())) {
    m_StartSu2.SetFocus();
    CString s,s2;
    s.Format(IDS_TF2_BEGINS_AFTER_TF1_ENDS, "Su");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  // the begin of the third timeframe must be after the end of the second timeframe, except midnight
  if (m_StartMo3.IsWindowEnabled() && (StartMo3.GetHour() != 0 || StartMo3.GetMinute() != 0 || StopMo3.GetHour() != 0 || StopMo3.GetMinute() != 0) && (StopMo2.GetHour() > StartMo3.GetHour() || StopMo2.GetHour() == StartMo3.GetHour() && StopMo2.GetMinute() >= StartMo3.GetMinute())) {
    m_StartMo3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "Mo");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartTu3.IsWindowEnabled() && (StartTu3.GetHour() != 0 || StartTu3.GetMinute() != 0 || StopTu3.GetHour() != 0 || StopTu3.GetMinute() != 0) && (StopTu2.GetHour() > StartTu3.GetHour() || StopTu2.GetHour() == StartTu3.GetHour() && StopTu2.GetMinute() >= StartTu3.GetMinute())) {
    m_StartTu3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "Tu");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartWe3.IsWindowEnabled() && (StartWe3.GetHour() != 0 || StartWe3.GetMinute() != 0 || StopWe3.GetHour() != 0 || StopWe3.GetMinute() != 0) && (StopWe2.GetHour() > StartWe3.GetHour() || StopWe2.GetHour() == StartWe3.GetHour() && StopWe2.GetMinute() >= StartWe3.GetMinute())) {
    m_StartWe3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "We");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartTh3.IsWindowEnabled() && (StartTh3.GetHour() != 0 || StartTh3.GetMinute() != 0 || StopTh3.GetHour() != 0 || StopTh3.GetMinute() != 0) && (StopTh2.GetHour() > StartTh3.GetHour() || StopTh2.GetHour() == StartTh3.GetHour() && StopTh2.GetMinute() >= StartTh3.GetMinute())) {
    m_StartTh3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "Th");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartFr3.IsWindowEnabled() && (StartFr3.GetHour() != 0 || StartFr3.GetMinute() != 0 || StopFr3.GetHour() != 0 || StopFr3.GetMinute() != 0) && (StopFr2.GetHour() > StartFr3.GetHour() || StopFr2.GetHour() == StartFr3.GetHour() && StopFr2.GetMinute() >= StartFr3.GetMinute())) {
    m_StartFr3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "Fr");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartSa3.IsWindowEnabled() && (StartSa3.GetHour() != 0 || StartSa3.GetMinute() != 0 || StopSa3.GetHour() != 0 || StopSa3.GetMinute() != 0) && (StopSa2.GetHour() > StartSa3.GetHour() || StopSa2.GetHour() == StartSa3.GetHour() && StopSa2.GetMinute() >= StartSa3.GetMinute())) {
    m_StartSa3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "Sa");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (m_StartSu3.IsWindowEnabled() && (StartSu3.GetHour() != 0 || StartSu3.GetMinute() != 0 || StopSu3.GetHour() != 0 || StopSu3.GetMinute() != 0) && (StopSu2.GetHour() > StartSu3.GetHour() || StopSu2.GetHour() == StartSu3.GetHour() && StopSu2.GetMinute() >= StartSu3.GetMinute())) {
    m_StartSu3.SetFocus();
    CString s,s2;
    s.Format(IDS_TF3_BEGINS_AFTER_TF2_ENDS, "Su");
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  return true;
}

void CPowerPage::OnDatetimechangeDatetimepicker(NMHDR* pNMHDR, LRESULT* pResult) 
{
  UpdateData(TRUE);
  if (StopMo1.GetHour() == 0 && StopMo1.GetMinute() == 0) {
    m_StartMo2.EnableWindow(FALSE);
    m_StopMo2.EnableWindow(FALSE);
    m_CPUMo2.EnableWindow(FALSE);
  } else {
    m_StartMo2.EnableWindow(TRUE);
    m_StopMo2.EnableWindow(TRUE);
    m_CPUMo2.EnableWindow(TRUE);
  }
  if (!m_StartMo2.IsWindowEnabled() || StopMo2.GetHour() == 0 && StopMo2.GetMinute() == 0) {
    m_StartMo3.EnableWindow(FALSE);
    m_StopMo3.EnableWindow(FALSE);
    m_CPUMo3.EnableWindow(FALSE);
  } else {
    m_StartMo3.EnableWindow(TRUE);
    m_StopMo3.EnableWindow(TRUE);
    m_CPUMo3.EnableWindow(TRUE);
  }
  if (StopTu1.GetHour() == 0 && StopTu1.GetMinute() == 0) {
    m_StartTu2.EnableWindow(FALSE);
    m_StopTu2.EnableWindow(FALSE);
    m_CPUTu2.EnableWindow(FALSE);
  } else {
    m_StartTu2.EnableWindow(TRUE);
    m_StopTu2.EnableWindow(TRUE);
    m_CPUTu2.EnableWindow(TRUE);
  }
  if (!m_StartTu2.IsWindowEnabled() || StopTu2.GetHour() == 0 && StopTu2.GetMinute() == 0) {
    m_StartTu3.EnableWindow(FALSE);
    m_StopTu3.EnableWindow(FALSE);
    m_CPUTu3.EnableWindow(FALSE);
  } else {
    m_StartTu3.EnableWindow(TRUE);
    m_StopTu3.EnableWindow(TRUE);
    m_CPUTu3.EnableWindow(TRUE);
  }
  if (StopWe1.GetHour() == 0 && StopWe1.GetMinute() == 0) {
    m_StartWe2.EnableWindow(FALSE);
    m_StopWe2.EnableWindow(FALSE);
    m_CPUWe2.EnableWindow(FALSE);
  } else {
    m_StartWe2.EnableWindow(TRUE);
    m_StopWe2.EnableWindow(TRUE);
    m_CPUWe2.EnableWindow(TRUE);
  }
  if (!m_StartWe2.IsWindowEnabled() || StopWe2.GetHour() == 0 && StopWe2.GetMinute() == 0) {
    m_StartWe3.EnableWindow(FALSE);
    m_StopWe3.EnableWindow(FALSE);
    m_CPUWe3.EnableWindow(FALSE);
  } else {
    m_StartWe3.EnableWindow(TRUE);
    m_StopWe3.EnableWindow(TRUE);
    m_CPUWe3.EnableWindow(TRUE);
  }
  if (StopTh1.GetHour() == 0 && StopTh1.GetMinute() == 0) {
    m_StartTh2.EnableWindow(FALSE);
    m_StopTh2.EnableWindow(FALSE);
    m_CPUTh2.EnableWindow(FALSE);
  } else {
    m_StartTh2.EnableWindow(TRUE);
    m_StopTh2.EnableWindow(TRUE);
    m_CPUTh2.EnableWindow(TRUE);
  }
  if (!m_StartTh2.IsWindowEnabled() || StopTh2.GetHour() == 0 && StopTh2.GetMinute() == 0) {
    m_StartTh3.EnableWindow(FALSE);
    m_StopTh3.EnableWindow(FALSE);
    m_CPUTh3.EnableWindow(FALSE);
  } else {
    m_StartTh3.EnableWindow(TRUE);
    m_StopTh3.EnableWindow(TRUE);
    m_CPUTh3.EnableWindow(TRUE);
  }
  if (StopFr1.GetHour() == 0 && StopFr1.GetMinute() == 0) {
    m_StartFr2.EnableWindow(FALSE);
    m_StopFr2.EnableWindow(FALSE);
    m_CPUFr2.EnableWindow(FALSE);
  } else {
    m_StartFr2.EnableWindow(TRUE);
    m_StopFr2.EnableWindow(TRUE);
    m_CPUFr2.EnableWindow(TRUE);
  }
  if (!m_StartFr2.IsWindowEnabled() || StopFr2.GetHour() == 0 && StopFr2.GetMinute() == 0) {
    m_StartFr3.EnableWindow(FALSE);
    m_StopFr3.EnableWindow(FALSE);
    m_CPUFr3.EnableWindow(FALSE);
  } else {
    m_StartFr3.EnableWindow(TRUE);
    m_StopFr3.EnableWindow(TRUE);
    m_CPUFr3.EnableWindow(TRUE);
  }
  if (StopSa1.GetHour() == 0 && StopSa1.GetMinute() == 0) {
    m_StartSa2.EnableWindow(FALSE);
    m_StopSa2.EnableWindow(FALSE);
    m_CPUSa2.EnableWindow(FALSE);
  } else {
    m_StartSa2.EnableWindow(TRUE);
    m_StopSa2.EnableWindow(TRUE);
    m_CPUSa2.EnableWindow(TRUE);
  }
  if (!m_StartSa2.IsWindowEnabled() || StopSa2.GetHour() == 0 && StopSa2.GetMinute() == 0) {
    m_StartSa3.EnableWindow(FALSE);
    m_StopSa3.EnableWindow(FALSE);
    m_CPUSa3.EnableWindow(FALSE);
  } else {
    m_StartSa3.EnableWindow(TRUE);
    m_StopSa3.EnableWindow(TRUE);
    m_CPUSa3.EnableWindow(TRUE);
  }
  if (StopSu1.GetHour() == 0 && StopSu1.GetMinute() == 0) {
    m_StartSu2.EnableWindow(FALSE);
    m_StopSu2.EnableWindow(FALSE);
    m_CPUSu2.EnableWindow(FALSE);
  } else {
    m_StartSu2.EnableWindow(TRUE);
    m_StopSu2.EnableWindow(TRUE);
    m_CPUSu2.EnableWindow(TRUE);
  }
  if (!m_StartSu2.IsWindowEnabled() || StopSu2.GetHour() == 0 && StopSu2.GetMinute() == 0) {
    m_StartSu3.EnableWindow(FALSE);
    m_StopSu3.EnableWindow(FALSE);
    m_CPUSu3.EnableWindow(FALSE);
  } else {
    m_StartSu3.EnableWindow(TRUE);
    m_StopSu3.EnableWindow(TRUE);
    m_CPUSu3.EnableWindow(TRUE);
  }
	*pResult = 0;
}

int CPowerPage::getTimeframe(const char* activeAt, const char* dayOfWeek, CTime& start, CTime& stop, CEdit& cpuUsageEdit, const char* defaultCpuUsage)
{
  int found = 0;
  int startHour = 0;
  int startMin = 0;
  int stopHour = 0;
  int stopMin = 0;
  char cpuUsage[20];
  strcpy(cpuUsage, defaultCpuUsage);
  const char* pos = strstr(activeAt, dayOfWeek);
  int i = (pos == NULL)? -1 : pos-activeAt;
  if (i >= 0) {
    int l = strlen(activeAt);
    int j = i+strlen(dayOfWeek);
    if (j < l) {
      while (j < l && isdigit(activeAt[j])) {
        startHour = 10*startHour + (activeAt[j]-'0');
        ++j;
      }
      if (++j < l && activeAt[j-1] == ':') {
        while (j < l && isdigit(activeAt[j])) {
          startMin = 10*startMin + (activeAt[j]-'0');
          ++j;
        }
        if (++j < l && activeAt[j-1] == '-') {
          while (j < l && isdigit(activeAt[j])) {
            stopHour = 10*stopHour + (activeAt[j]-'0');
            ++j;
          }
          if (j < l && activeAt[j] == ':') {
            while (++j < l && isdigit(activeAt[j])) {
              stopMin = 10*stopMin + (activeAt[j]-'0');
            }
            if (j < l && activeAt[j] == ',') {
              int k = 0;
              while (++j < l && isdigit(activeAt[j])) {
                if (k < 3) {
                  cpuUsage[k++] = activeAt[j];
                }
              }
              cpuUsage[k] = '\0';
            }
            found = j;
          } else {
            startHour = startMin = stopHour = 0;
          }
        } else {
          startHour = startMin = 0;
        }
      } else {
        startHour = 0;
      }
    }
  }
  CTime today = CTime::GetCurrentTime();
  start = CTime(today.GetYear(), today.GetMonth(), today.GetDay(), startHour, startMin, 0, -1);
  stop = CTime(today.GetYear(), today.GetMonth(), today.GetDay(), stopHour, stopMin, 0, -1);
  cpuUsageEdit.SetWindowText(cpuUsage);
  return found;
}
