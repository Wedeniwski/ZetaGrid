// OptionPage.cpp : implementation file
//

#include "stdafx.h"
#include "saver.h"
#include "OptionPage.h"
#include "KonfigurationDlg.h"
#include "JavaError.h"
#include <io.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// OptionPage property page

IMPLEMENT_DYNCREATE(OptionPage, CPropertyPage)

OptionPage::OptionPage() : CPropertyPage(OptionPage::IDD)
{
	//{{AFX_DATA_INIT(OptionPage)
	m_DisplayState = TRUE;
	m_SystemTray = FALSE;
	m_JavaVM = _T("java.exe");
	m_RootPath = _T("");
	m_JavaLib = _T("jvm.dll");
	//}}AFX_DATA_INIT
}

OptionPage::~OptionPage()
{
}

void OptionPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(OptionPage)
	DDX_Control(pDX, IDC_STATIC_JAVA_LIB, m_JavaLibText);
	DDX_Control(pDX, IDC_EDIT_JAVA_LIB, m_JavaLibEdit);
	DDX_Control(pDX, IDC_BUTTON_LIB_CHOOSE, m_LibChoose);
	DDX_Check(pDX, IDC_CHECK_DISPLAY_ST, m_DisplayState);
	DDX_Check(pDX, IDC_CHECK_SYSTEM_TRAY, m_SystemTray);
	DDX_Text(pDX, IDC_EDIT_JAVA_VM, m_JavaVM);
	DDX_Text(pDX, IDC_EDIT_ROOT_PATH, m_RootPath);
	DDX_Text(pDX, IDC_EDIT_JAVA_LIB, m_JavaLib);
	//}}AFX_DATA_MAP
}


BEGIN_MESSAGE_MAP(OptionPage, CPropertyPage)
	//{{AFX_MSG_MAP(OptionPage)
	ON_BN_CLICKED(IDC_BUTTON_ROOT_BROWSE, OnButtonRootBrowse)
	ON_BN_CLICKED(IDC_BUTTON_VM_CHOOSE, OnButtonVmChoose)
	ON_BN_CLICKED(IDC_BUTTON_LIB_CHOOSE, OnButtonLibChoose)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// OptionPage message handlers

void OptionPage::set(const Config& cfg)
{
  UpdateData(TRUE);
  bool javaProblem = false;
  m_JavaVM = cfg.javaVM.c_str();
  if (_access(m_JavaVM, 0) == -1) {
    CString s = getRegistryJava(true);
    if (s.GetLength() > 0) {
      m_JavaVM = s;
    } else {
      javaProblem = true;
    }
  }
  m_JavaLib = cfg.javaLib.c_str();
  if (cfg.mode == Config::service) {
    if (_access(m_JavaLib, 0) == -1) {
      CString s = getRegistryJavaLib(!javaProblem);
      if (s.GetLength() > 0) {
        m_JavaLib = s;
      }
    }
  }
  m_RootPath = cfg.rootPath.c_str();
  m_DisplayState = cfg.displayState;
  m_SystemTray = cfg.systemTray;
  UpdateData(FALSE);
}

void OptionPage::get(Config& cfg)
{
  UpdateData(TRUE);
  cfg.javaVM = (const char*)m_JavaVM;
  cfg.javaLib = (const char*)m_JavaLib;
  cfg.rootPath = (const char*)m_RootPath;
  cfg.displayState = (m_DisplayState == TRUE);
  cfg.systemTray = (m_SystemTray == TRUE);
}

bool OptionPage::check(bool information)
{
  UpdateData(TRUE);
  // Check java
  if (_access(m_JavaVM, 0) == -1) {
    MessageBeep(MB_ICONHAND);
    CJavaError dlg;
    dlg.SetFilename(m_JavaVM, "java.exe");
    dlg.DoModal();
    return false;
  }
  Config cfg;
  if (cfg.mode == Config::service && _access(m_JavaLib, 0) == -1) {
    MessageBeep(MB_ICONHAND);
    CJavaError dlg;
    dlg.SetFilename(m_JavaLib, "jvm.dll");
    dlg.DoModal();
    return false;
  }
  // Check Root-Path: zeta.jar
  if (_access(m_RootPath, 0) == -1) {
    CString s,s2;
    s.Format(IDS_FILE_NOT_EXISTS, (const char*)m_RootPath);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (_access(m_RootPath, 6) == -1) {
    CString s,s2;
    s.Format(IDS_FILE_NO_PERMISSION, (const char*)m_RootPath);
    s2.LoadString(IDS_ERROR);
    MessageBox(s, s2, MB_ICONERROR);
    return false;
  }
  if (information && cfg.mode == Config::service && m_JavaLib.Find("jvm.dll") == -1) {
    CString s,s2;
    s.Format(IDS_JAVA_LIB, m_JavaLib);
    s2.LoadString(IDS_INFORMATION);
    MessageBox(s, s2, MB_ICONINFORMATION);
  }
  return true;
}

void OptionPage::OnButtonVmChoose() 
{
  UpdateData(TRUE);
  CFileDialog dlg(TRUE, "exe", m_JavaVM, OFN_HIDEREADONLY | OFN_OVERWRITEPROMPT, "*.exe|*.exe||");
  if (dlg.DoModal() == IDOK) {
    m_JavaVM = dlg.GetPathName();
    UpdateData(FALSE);
  }
}

void OptionPage::OnButtonLibChoose() 
{
  UpdateData(TRUE);
  CFileDialog dlg(TRUE, "dll", m_JavaLib, OFN_HIDEREADONLY | OFN_OVERWRITEPROMPT, "*.dll|*.dll||");
  if (dlg.DoModal() == IDOK) {
    m_JavaLib = dlg.GetPathName();
    UpdateData(FALSE);
  }
}

void OptionPage::OnButtonRootBrowse() 
{
}

CString OptionPage::getRegistryJava(bool message)
{
  HKEY key,subkey;
  const char* versions[] = { "Software\\JavaSoft\\Java Runtime Environment", "Software\\JavaSoft\\Java Development Kit",
                             "Software\\IBM\\Java3 Runtime Environment", "Software\\IBM\\Java3 Development Kit",
                             "Software\\IBM\\Java2 Runtime Environment", "Software\\IBM\\Java2 Development Kit",
                             "Software\\IBM\\Java Runtime Environment", "Software\\IBM\\Java Development Kit" };
  unsigned char buffer[1000];
  unsigned long datatype;
  unsigned long bufferlength = sizeof(buffer);

  for (int i = 0; i < 8; ++i) {
    CString path = versions[i];
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, path, 0, KEY_READ, &key) == ERROR_SUCCESS) {
      if (RegQueryValueEx(key, "CurrentVersion", NULL, &datatype, buffer, &bufferlength) == ERROR_SUCCESS) {
        path = buffer;
        CString version = path.Left(3);
        if (message && (version == "1.0" || version == "1.1" || version == "1.2")) {
          CString s,s2;
          s.LoadString(IDS_REQUIRED_JAVA_VM);
          s2.LoadString(IDS_PREREQUISITE);
          ::MessageBox(NULL, s, s2, MB_ICONWARNING);
        }
        if (RegOpenKeyEx(key, path, 0, KEY_READ, &subkey) == ERROR_SUCCESS) {
          bufferlength = sizeof(buffer);
          if (RegQueryValueEx(subkey, "JavaHome", NULL, &datatype, buffer, &bufferlength) == ERROR_SUCCESS) {
            path = buffer;
            path += "\\bin\\java.exe";
            if (_access(path, 0) == 0) {
              RegCloseKey(subkey);
              RegCloseKey(key);
              return path;
            }
          }
          RegCloseKey(subkey);
        }
        RegCloseKey(key);
      } else {
        RegCloseKey(key);
      }
    }
  }
  return "";
}

CString OptionPage::getRegistryJavaLib(bool message)
{
  HKEY key,subkey;
  const char* versions[] = { "Software\\JavaSoft\\Java Runtime Environment", "Software\\JavaSoft\\Java Development Kit",
                             "Software\\IBM\\Java3 Runtime Environment", "Software\\IBM\\Java3 Development Kit",
                             "Software\\IBM\\Java2 Runtime Environment", "Software\\IBM\\Java2 Development Kit",
                             "Software\\IBM\\Java Runtime Environment", "Software\\IBM\\Java Development Kit" };
  unsigned char buffer[1000];
  unsigned long datatype;
  unsigned long bufferlength = sizeof(buffer);

  for (int i = 0; i < 8; ++i) {
    CString path = versions[i];
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, path, 0, KEY_READ, &key) == ERROR_SUCCESS) {
      if (RegQueryValueEx(key, "CurrentVersion", NULL, &datatype, buffer, &bufferlength) == ERROR_SUCCESS) {
        path = buffer;
        CString version = path.Left(3);
        if (message && (version == "1.0" || version == "1.1" || version == "1.2")) {
          CString s,s2;
          s.LoadString(IDS_REQUIRED_JAVA_VM);
          s2.LoadString(IDS_PREREQUISITE);
          ::MessageBox(NULL, s, s2, MB_ICONWARNING);
        }
        if (RegOpenKeyEx(key, path, 0, KEY_READ, &subkey) == ERROR_SUCCESS) {
          bufferlength = sizeof(buffer);
          if (RegQueryValueEx(subkey, "RuntimeLib", NULL, &datatype, buffer, &bufferlength) == ERROR_SUCCESS) {
            path = buffer;
            if (_access(path, 0) == 0) {
              RegCloseKey(subkey);
              RegCloseKey(key);
              return path;
            }
          }
          RegCloseKey(subkey);
        }
        RegCloseKey(key);
      } else {
        RegCloseKey(key);
      }
    }
  }
  return "";
}

BOOL OptionPage::OnInitDialog() 
{
	CPropertyPage::OnInitDialog();

  Config cfg;
  BOOL enable = (cfg.mode == Config::service);
  m_JavaLibText.EnableWindow(enable);
  m_JavaLibEdit.EnableWindow(enable);
  m_LibChoose.EnableWindow(enable);

  return TRUE;  // return TRUE unless you set the focus to a control
	              // EXCEPTION: OCX Property Pages should return FALSE
}
