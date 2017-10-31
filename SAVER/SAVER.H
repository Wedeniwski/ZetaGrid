// Saver.h : main header file for the SAVER application
//

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"       // main symbols

/////////////////////////////////////////////////////////////////////////////
// CSaverApp:
// See Saver.cpp for the implementation of this class
//

class CSaverApp : public CWinApp
{
public:
	CSaverApp();

  static void removeBasicFiles();
  static bool areBasicFilesValid();
  static void generateBasicFiles();
  static void exitCalculation(int wait);
  static bool isControlCenterRunning();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CSaverApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CSaverApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////
