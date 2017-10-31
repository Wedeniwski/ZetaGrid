// ProcessPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "ProcessPage.h"
#include "WorkUnitPage.h"
#include "InstallDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// ProcessPage property page

IMPLEMENT_DYNCREATE(ProcessPage, CPropertyPage)

ProcessPage::ProcessPage() : CPropertyPage(ProcessPage::IDD)
{
	//{{AFX_DATA_INIT(ProcessPage)
	m_NumberOfProcessors = 0;
	//}}AFX_DATA_INIT
}

ProcessPage::~ProcessPage()
{
}

void ProcessPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(ProcessPage)
	DDX_Control(pDX, IDC_TRANSFER_DETECT, m_TransferDetectText);
	DDX_Control(pDX, IDC_STATIC_TRANSFER_DETECT, m_TransferDetect);
	DDX_Control(pDX, IDC_STATIC_PRIORITY, m_PriorityText);
	DDX_Control(pDX, IDC_SLIDER_CPU_USAGE, m_Intensity);
	DDX_Control(pDX, IDC_COMBO_RESOURCES, m_Resources);
	DDX_Control(pDX, IDC_COMBO_PRIO, m_Priority);
	DDX_Text(pDX, IDC_EDIT_PROZESSOREN, m_NumberOfProcessors);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(ProcessPage, CPropertyPage)
	//{{AFX_MSG_MAP(ProcessPage)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// ProcessPage message handlers

void ProcessPage::set(const Config& cfg)
{
  UpdateData(TRUE);
  string priority = cfg.priority;
  if (cfg.mode == Config::commandline && !CInstallDlg::IsWindowsNT()) {
    priority = "normal";
    m_Priority.EnableWindow(false);
    m_PriorityText.EnableWindow(false);
  } else {
    m_Priority.EnableWindow(true);
    m_PriorityText.EnableWindow(true);
  }
  m_Priority.SetCurSel((priority == "low")? 1 : 0);
  m_NumberOfProcessors = cfg.numberOfProcessors;
  m_Intensity.SetPos(cfg.processorsUsage);
  m_Resources.SetCurSel(cfg.resources);
  char c[20];
  if (!cfg.asynchronousTransfer) {
    m_TransferDetectText.SetWindowText("0");
  } else {
    m_TransferDetectText.SetWindowText(itoa(cfg.transferDetect, c, 10));
  }
  UpdateData(FALSE);
}

void ProcessPage::get(Config& cfg)
{
  UpdateData(TRUE);
  cfg.priority = (m_Priority.GetCurSel() == 1)? "low" : "normal";
  if (m_NumberOfProcessors == 0 && !WorkUnitPage::completeAllLocalWorkUnits) {
    CString s,s2;
    s.LoadString(IDS_PROCESSORS_SMALLER_1);
    s2.LoadString(IDS_INFORMATION);
    MessageBox(s, s2, MB_ICONINFORMATION);
    m_NumberOfProcessors = 1;
  }
  cfg.numberOfProcessors = m_NumberOfProcessors;
  cfg.processorsUsage = m_Intensity.GetPos();
  if (cfg.processorsUsage == 0) {
    cfg.processorsUsage = 1;
  }
  cfg.resources = m_Resources.GetCurSel();
  CString s;
  m_TransferDetectText.GetWindowText(s);
  cfg.transferDetect = atoi(s);
  cfg.asynchronousTransfer = (cfg.transferDetect != 0);
}

bool ProcessPage::check(bool information)
{
  UpdateData(TRUE);
  /*if (information && m_NumberOfProcessors > 5) {
    CString s,s2;
    s.LoadString(IDS_PROCESSORS_GREATER_5);
    s2.LoadString(IDS_INFORMATION);
    MessageBox(s, s2, MB_ICONINFORMATION);
  }*/
  return true;
}

BOOL ProcessPage::OnInitDialog() 
{
	CPropertyPage::OnInitDialog();
	
  UpdateData(TRUE);
  m_Intensity.SetRange(0, 100, TRUE);

  return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}
