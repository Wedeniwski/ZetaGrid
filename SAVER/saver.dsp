# Microsoft Developer Studio Project File - Name="Saver" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Application" 0x0101

CFG=Saver - Win32 Release
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "saver.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "saver.mak" CFG="Saver - Win32 Release"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "Saver - Win32 Release" (based on "Win32 (x86) Application")
!MESSAGE "Saver - Win32 Debug" (based on "Win32 (x86) Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=xicl6.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "Saver - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir ".\Release"
# PROP BASE Intermediate_Dir ".\Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 1
# PROP Use_Debug_Libraries 0
# PROP Output_Dir ".\Release"
# PROP Intermediate_Dir ".\Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /YX /c
# ADD CPP /nologo /MT /W3 /GX /O2 /I "D:\Compiler\j2sdk1.4.2_05\include" /I "D:\Compiler\j2sdk1.4.2_05\include\win32" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=xilink6.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /machine:I386
# ADD LINK32 /nologo /subsystem:windows /machine:I386 /out:".\Release\zeta.scr"

!ELSEIF  "$(CFG)" == "Saver - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir ".\Debug"
# PROP BASE Intermediate_Dir ".\Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 2
# PROP Use_Debug_Libraries 1
# PROP Output_Dir ".\Debug"
# PROP Intermediate_Dir ".\Debug"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /YX /c
# ADD CPP /nologo /MDd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /Yu"Stdafx.h" /FD /c
# ADD BASE MTL /nologo /D "_DEBUG" /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG" /d "_AFXDLL"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=xilink6.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:windows /debug /machine:I386
# ADD LINK32 /nologo /subsystem:windows /debug /machine:I386 /out:".\Debug\Saver.scr"

!ENDIF 

# Begin Target

# Name "Saver - Win32 Release"
# Name "Saver - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;hpj;bat;for;f90"
# Begin Source File

SOURCE=.\AboutDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\config.cpp
# End Source File
# Begin Source File

SOURCE=.\ConnectionPage.cpp
# End Source File
# Begin Source File

SOURCE=.\ControlCenterDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\Drawwnd.cpp
# End Source File
# Begin Source File

SOURCE=.\GeneralPage.cpp
# End Source File
# Begin Source File

SOURCE=.\InstallDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\JavaError.cpp
# End Source File
# Begin Source File

SOURCE=.\javaserver.c
# End Source File
# Begin Source File

SOURCE=.\KonfigurationDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\LicenseDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\LoggingPage.cpp
# End Source File
# Begin Source File

SOURCE=.\OptionPage.cpp
# End Source File
# Begin Source File

SOURCE=.\parseargs.c
# End Source File
# Begin Source File

SOURCE=.\PortDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\PowerPage.cpp
# End Source File
# Begin Source File

SOURCE=.\ProcessPage.cpp
# End Source File
# Begin Source File

SOURCE=.\ProgressDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\registry.c
# End Source File
# Begin Source File

SOURCE=.\Saver.cpp
# End Source File
# Begin Source File

SOURCE=.\Saver.rc
# End Source File
# Begin Source File

SOURCE=.\Saverwnd.cpp
# End Source File
# Begin Source File

SOURCE=.\SecurityPage.cpp
# End Source File
# Begin Source File

SOURCE=.\service.c
# End Source File
# Begin Source File

SOURCE=.\State.cpp
# End Source File
# Begin Source File

SOURCE=.\Stdafx.cpp
# ADD CPP /Yc"Stdafx.h"
# End Source File
# Begin Source File

SOURCE=.\SystemTray.cpp
# End Source File
# Begin Source File

SOURCE=.\TestDlg.cpp
# End Source File
# Begin Source File

SOURCE=.\WorkUnitPage.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;inl;fi;fd"
# Begin Source File

SOURCE=.\AboutDlg.h
# End Source File
# Begin Source File

SOURCE=.\config.h
# End Source File
# Begin Source File

SOURCE=.\ConnectionPage.h
# End Source File
# Begin Source File

SOURCE=.\ControlCenterDlg.h
# End Source File
# Begin Source File

SOURCE=.\drawwnd.h
# End Source File
# Begin Source File

SOURCE=.\GeneralPage.h
# End Source File
# Begin Source File

SOURCE=.\InstallDlg.h
# End Source File
# Begin Source File

SOURCE=.\JavaError.h
# End Source File
# Begin Source File

SOURCE=.\KonfigurationDlg.h
# End Source File
# Begin Source File

SOURCE=.\LicenseDlg.h
# End Source File
# Begin Source File

SOURCE=.\LoggingPage.h
# End Source File
# Begin Source File

SOURCE=.\messages.h
# End Source File
# Begin Source File

SOURCE=.\OptionPage.h
# End Source File
# Begin Source File

SOURCE=.\parseargs.h
# End Source File
# Begin Source File

SOURCE=.\PortDlg.h
# End Source File
# Begin Source File

SOURCE=.\PowerPage.h
# End Source File
# Begin Source File

SOURCE=.\ProcessPage.h
# End Source File
# Begin Source File

SOURCE=.\ProgressDlg.h
# End Source File
# Begin Source File

SOURCE=.\registry.h
# End Source File
# Begin Source File

SOURCE=.\Saver.h
# End Source File
# Begin Source File

SOURCE=.\saverwnd.h
# End Source File
# Begin Source File

SOURCE=.\SecurityPage.h
# End Source File
# Begin Source File

SOURCE=.\service.h
# End Source File
# Begin Source File

SOURCE=.\State.h
# End Source File
# Begin Source File

SOURCE=.\stdafx.h
# End Source File
# Begin Source File

SOURCE=.\SystemTray.h
# End Source File
# Begin Source File

SOURCE=.\TestDlg.h
# End Source File
# Begin Source File

SOURCE=.\WorkUnitPage.h
# End Source File
# Begin Source File

SOURCE=.\zeta_data.h
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;cnt;rtf;gif;jpg;jpeg;jpe"
# Begin Source File

SOURCE=.\RES\bitmap1.bmp
# End Source File
# Begin Source File

SOURCE=.\res\cursor1.cur
# End Source File
# Begin Source File

SOURCE=.\res\Saver.ico
# End Source File
# Begin Source File

SOURCE=.\res\Saver.rc2
# End Source File
# Begin Source File

SOURCE=.\RES\zeta.bmp
# End Source File
# End Group
# Begin Source File

SOURCE=.\RES\zeta.jar
# End Source File
# End Target
# End Project
