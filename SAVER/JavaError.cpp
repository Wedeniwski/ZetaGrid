// JavaError.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "JavaError.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CJavaError dialog


CJavaError::CJavaError(CWnd* pParent /*=NULL*/)
	: CDialog(CJavaError::IDD, pParent)
{
	//{{AFX_DATA_INIT(CJavaError)
	m_ErrorText = _T("");
	m_ErrorText2 = _T("");
	m_ErrorText3 = _T("");
	//}}AFX_DATA_INIT
}


void CJavaError::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CJavaError)
	DDX_Text(pDX, IDC_STATIC_ERROR, m_ErrorText);
	DDX_Text(pDX, IDC_STATIC_ERROR2, m_ErrorText2);
	DDX_Text(pDX, IDC_STATIC_ERROR3, m_ErrorText3);
	//}}AFX_DATA_MAP
}

void CJavaError::SetFilename(const char* filename, const char* type)
{
  m_ErrorText.Format(IDS_FILE_NOT_EXISTS, (const char*)filename);
  m_ErrorText3.Format(IDS_HOW_TO_DOWNLOAD_JVM, type);
  if (strcmp(type, "jvm.dll") == 0) {
    m_ErrorText2.Format(IDS_HOW_TO_FIND_FILE, type, type, "C:\\Program Files\\java13\\jre\\bin\\hotspot\\", "C:\\Program Files\\java13\\jre\\bin\\hotspot\\jvm.dll");
  } else {
    m_ErrorText2.Format(IDS_HOW_TO_FIND_FILE, type, type, "C:\\Program Files\\java13\\jre\\bin\\", "C:\\Program Files\\java13\\jre\\bin\\java.exe");
  }
}


BEGIN_MESSAGE_MAP(CJavaError, CDialog)
	//{{AFX_MSG_MAP(CJavaError)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CJavaError message handlers
