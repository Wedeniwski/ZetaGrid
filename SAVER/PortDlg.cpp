// PortDlg.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "PortDlg.h"
#include "KonfigurationDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// PortDlg dialog


PortDlg::PortDlg(CWnd* pParent /*=NULL*/)
	: CDialog(PortDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(PortDlg)
	m_PortNumber = 10000;
	//}}AFX_DATA_INIT
}


void PortDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(PortDlg)
	DDX_Text(pDX, IDC_EDIT_PORT_NUMBER, m_PortNumber);
	DDV_MinMaxInt(pDX, m_PortNumber, 8000, 99999);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(PortDlg, CDialog)
	//{{AFX_MSG_MAP(PortDlg)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// PortDlg message handlers

void PortDlg::OnOK() 
{
	// TODO: Add extra validation here
  UpdateData(TRUE);
  if (KonfigurationDlg::running != 0) {
	  KonfigurationDlg::running->cfg.port = m_PortNumber;
  }
	CDialog::OnOK();
}

BOOL PortDlg::OnInitDialog() 
{
	CDialog::OnInitDialog();
	
  UpdateData(TRUE);
  if (KonfigurationDlg::running != 0) {
    m_PortNumber = KonfigurationDlg::running->cfg.port;
  }
  UpdateData(FALSE);

	return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}
