// LoggingPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "LoggingPage.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// LoggingPage property page

IMPLEMENT_DYNCREATE(LoggingPage, CPropertyPage)

LoggingPage::LoggingPage() : CPropertyPage(LoggingPage::IDD)
{
	//{{AFX_DATA_INIT(LoggingPage)
	m_EventFormatTimestamp = _T("");
	m_EventLogFile = _T("");
	m_EventNumberBackup = 0;
	m_EventLogFileSize = _T("");
	//}}AFX_DATA_INIT
}

LoggingPage::~LoggingPage()
{
}

void LoggingPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(LoggingPage)
	DDX_Control(pDX, IDC_CHECK_STD_OUTPUT, m_StdOutput);
	DDX_Text(pDX, IDC_EDIT_EVENT_FORMAT_TIMESTAMP, m_EventFormatTimestamp);
	DDX_Text(pDX, IDC_EDIT_EVENT_LOG_FILE, m_EventLogFile);
	DDX_Text(pDX, IDC_EDIT_EVENT_NUMBER_BACKUP, m_EventNumberBackup);
	DDV_MinMaxInt(pDX, m_EventNumberBackup, 0, 100);
	DDX_Text(pDX, IDC_EDIT_EVENT_LOG_FILE_SIZE, m_EventLogFileSize);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(LoggingPage, CPropertyPage)
	//{{AFX_MSG_MAP(LoggingPage)
		// NOTE: the ClassWizard will add message map macros here
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// LoggingPage message handlers

void LoggingPage::set(const Config& cfg)
{
  UpdateData(TRUE);
	m_StdOutput.SetCheck(cfg.infoOutput);
	m_EventFormatTimestamp = cfg.infoEventLogTimestamp.c_str();
	m_EventLogFile = cfg.infoEventLogFilename.c_str();
  m_EventLogFileSize = cfg.infoLogFileSize.c_str();
	m_EventNumberBackup = cfg.infoLogMaxBackupIndex;
  UpdateData(FALSE);
}

void LoggingPage::get(Config& cfg)
{
  UpdateData(TRUE);
	cfg.infoOutput = bool(m_StdOutput.GetCheck());
	cfg.infoEventLogTimestamp = (const char*)m_EventFormatTimestamp;
	cfg.infoEventLogFilename = (const char*)m_EventLogFile;
  cfg.infoLogFileSize = (const char*)m_EventLogFileSize;
	cfg.infoLogMaxBackupIndex = m_EventNumberBackup;
}

bool LoggingPage::check(bool information)
{
  return true;
}
