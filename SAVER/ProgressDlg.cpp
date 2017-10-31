// ProgressDlg.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "ProgressDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CProgressDlg dialog


CProgressDlg::CProgressDlg(int sz, CWnd* pParent /*=NULL*/)
	: CDialog(CProgressDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CProgressDlg)
		// NOTE: the ClassWizard will add member initialization here
	//}}AFX_DATA_INIT
  size = sz;
  Create();
}

void CProgressDlg::Step()
{
  m_Progress.OffsetPos(1);
}

BOOL CProgressDlg::Create() 
{
	BOOL result = CDialog::Create(IDD);
  if (result) {
    BringWindowToTop();
    if (AfxGetApp()) SetCursor(AfxGetApp()->LoadStandardCursor(IDC_WAIT));
  }
  return result;
}

void CProgressDlg::Destroy() 
{
  //DestroyWindow();
  ShowWindow(SW_HIDE);
  if (AfxGetApp()) SetCursor(AfxGetApp()->LoadStandardCursor(IDC_ARROW));
}

void CProgressDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CProgressDlg)
	DDX_Control(pDX, IDC_PROGRESS1, m_Progress);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(CProgressDlg, CDialog)
	//{{AFX_MSG_MAP(CProgressDlg)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CProgressDlg message handlers

BOOL CProgressDlg::OnInitDialog() 
{
	CDialog::OnInitDialog();
  CenterWindow();
  m_Progress.SetRange(0, size);
  m_Progress.SetPos(1);
  m_Progress.SetStep(1);
  UpdateData(FALSE);
	
	return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}
