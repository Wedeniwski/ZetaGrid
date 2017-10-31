// WorkUnitPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "WorkUnitPage.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// WorkUnitPage property page

IMPLEMENT_DYNCREATE(WorkUnitPage, CPropertyPage)

bool WorkUnitPage::completeAllLocalWorkUnits = false;


WorkUnitPage::WorkUnitPage() : CPropertyPage(WorkUnitPage::IDD)
{
	//{{AFX_DATA_INIT(WorkUnitPage)
	m_NumberOfPackages = 0;
	completeAllLocalWorkUnits = false;
	//}}AFX_DATA_INIT
}

WorkUnitPage::~WorkUnitPage()
{
}

void WorkUnitPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(WorkUnitPage)
	DDX_Control(pDX, IDC_COMPLETE_ALL_LOCAL_WU, m_CompleteAllLocalWorkUnits);
	DDX_Control(pDX, IDC_COMBO_PACKAGE_SIZE, m_PackageSize);
	DDX_Control(pDX, IDC_COMBO_TASK_NAME, m_TaskName);
	DDX_Text(pDX, IDC_EDIT_PACKAGES, m_NumberOfPackages);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(WorkUnitPage, CPropertyPage)
	//{{AFX_MSG_MAP(WorkUnitPage)
	ON_BN_CLICKED(IDC_BUTTON_NOTE_NUMBER_OF_WORK_UNITS, OnButtonNoteNumberOfWorkUnits)
	ON_BN_CLICKED(IDC_BUTTON_NOTE_SIZE_OF_WORK_UNIT, OnButtonNoteSizeOfWorkUnit)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// WorkUnitPage message handlers

void WorkUnitPage::set(const Config& cfg)
{
  UpdateData(TRUE);
  switch (cfg.packageSize) {
    case 't': m_PackageSize.SetCurSel(0); break;
    case 's': m_PackageSize.SetCurSel(1); break;
    case 'l': m_PackageSize.SetCurSel(3); break;
    case 'h': m_PackageSize.SetCurSel(4); break;
    default : m_PackageSize.SetCurSel(2); break;
  }
  m_TaskName.SetCurSel(0);
  m_NumberOfPackages = cfg.numberOfPackages;
  completeAllLocalWorkUnits = (cfg.numberOfProcessors == 0);
  m_CompleteAllLocalWorkUnits.SetCheck((completeAllLocalWorkUnits)? 1 : 0);
  UpdateData(FALSE);
}

void WorkUnitPage::get(Config& cfg)
{
  UpdateData(TRUE);
  switch (m_PackageSize.GetCurSel()) {
    case 0: cfg.packageSize = 't'; break;
    case 1: cfg.packageSize = 's'; break;
    case 3: cfg.packageSize = 'l'; break;
    case 4: cfg.packageSize = 'h'; break;
    default: cfg.packageSize = 'm'; break;
  }
  cfg.numberOfPackages = m_NumberOfPackages;
  completeAllLocalWorkUnits = m_CompleteAllLocalWorkUnits.GetCheck();
  if (completeAllLocalWorkUnits) {
    cfg.numberOfProcessors = 0;
  }
}

bool WorkUnitPage::check(const Config& cfg, bool information)
{
  UpdateData(TRUE);
  if (m_NumberOfPackages < 1) {
    CString s,s2;
    s.LoadString(IDS_WORK_UNITS_LESS_1);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  // Check cfg.numberOfProcessors <= m_NumberOfPackages
  if (cfg.numberOfProcessors > m_NumberOfPackages) {
    CString s,s2;
    s.LoadString(IDS_PROCESSORS_PACKAGES_ERROR);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  /*if (information && m_NumberOfPackages > 5) {
    CString s,s2;
    s.LoadString(IDS_WORK_UNITS_GREATRER_5);
    s2.LoadString(IDS_INFORMATION);
    MessageBox(s, s2, MB_ICONINFORMATION);
  }*/
  return true;
}

void WorkUnitPage::OnButtonNoteNumberOfWorkUnits() 
{
  CString s,s2;
  s.LoadString(IDS_WORK_UNITS_GREATRER_5);
  s2.LoadString(IDS_PLEASE_NOTE);
  MessageBox(s, s2, MB_ICONINFORMATION | MB_OK);
}

void WorkUnitPage::OnButtonNoteSizeOfWorkUnit() 
{
  CString s,s2;
  s.LoadString(IDS_NOTE_SIZE_OF_WORK_UNIT);
  s2.LoadString(IDS_PLEASE_NOTE);
  MessageBox(s, s2, MB_ICONINFORMATION | MB_OK);
}
