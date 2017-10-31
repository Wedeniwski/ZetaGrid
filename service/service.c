/*
 * service.c    2.0 (July 22 2001)
 *
 * Copyright© 2001 by Bill Giel/KC Multimedia and Design Group, Inc.
 *
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <process.h>
#include <tchar.h>

#include "service.h"
#include "parseargs.h"
#include "registry.h"
#include "messages.h"


//global variables
SERVICE_STATUS          ssStatus;
SERVICE_STATUS_HANDLE   sshStatusHandle;
DWORD                   dwErr = 0;
BOOL                    bConsole = FALSE;
TCHAR                   szErr[256];


#define SZFAILURE "StartServiceControlDispatcher failed!"
#define SZSCMGRFAILURE "OpenSCManager failed - %s\n"




BOOL getConsoleMode()
{
    return bConsole;
}

// Create an error message from GetLastError() using the
// FormatMessage API Call...
LPTSTR GetLastErrorText( LPTSTR lpszBuf, DWORD dwSize )
{
    DWORD dwRet;
    LPTSTR lpszTemp = NULL;


    dwRet = FormatMessage( FORMAT_MESSAGE_ALLOCATE_BUFFER |
        FORMAT_MESSAGE_FROM_SYSTEM |FORMAT_MESSAGE_ARGUMENT_ARRAY,
        NULL,
        GetLastError(),
        LANG_NEUTRAL,
        (LPTSTR)&lpszTemp,
        0,
        NULL);

    // supplied buffer is not long enough
    if (!dwRet || ((long)dwSize < (long)dwRet+14)){
        lpszBuf[0] = TEXT('\0');
    }
    else{
        lpszTemp[lstrlen(lpszTemp)-2] = TEXT('\0');  //remove cr and newline character
        _stprintf( lpszBuf, TEXT("%s (0x%x)"), lpszTemp, GetLastError());
    }

    if (lpszTemp){
        GlobalFree((HGLOBAL) lpszTemp);
    }

    return lpszBuf;
}


// We'll try to install the service with this function, and save any
// runtime args for the service itself as a REG_SZ value in a registry
// subkey

void installService(int argc, char **argv)
{
    SC_HANDLE   schService;
    SC_HANDLE   schSCManager;

    TCHAR szPath[512];

    TCHAR szAppParameters[8192];

	char szParamKey[1025], szParamKey2[1025];

	sprintf(szParamKey,"SYSTEM\\CurrentControlSet\\Services\\%s\\Parameters",SZSERVICENAME);

    // Get the full path and filename of this program
    if ( GetModuleFileName( NULL, szPath, 512 ) == 0 ){
        _tprintf(TEXT("Unable to install %s - %s\n"), TEXT(SZSERVICEDISPLAYNAME),
            GetLastErrorText(szErr, 256));
        return;
    }

    // Next, get a handle to the service control manager
    schSCManager = OpenSCManager(
                        NULL,
                        NULL,
                        SC_MANAGER_ALL_ACCESS
                        );

    if ( schSCManager ){

        schService = CreateService(
            schSCManager,               // SCManager database
            TEXT(SZSERVICENAME),        // name of service
            TEXT(SZSERVICEDISPLAYNAME), // name to display
            SERVICE_ALL_ACCESS,         // desired access
            SERVICE_WIN32_OWN_PROCESS,  // service type
            SERVICESTARTTYPE    ,       // start type
            SERVICE_ERROR_NORMAL,       // error control type
            szPath,                     // service's binary
            NULL,                       // no load ordering group
            NULL,                       // no tag identifier
            TEXT(SZDEPENDENCIES),       // dependencies
            NULL,                       // LocalSystem account
            NULL);                      // no password

        if (schService){

            _tprintf(TEXT("%s installed.\n"), TEXT(SZSERVICEDISPLAYNAME) );

            // Close the handle to this service object
            CloseServiceHandle(schService);

            //Make a registry key to support logging messages using the service name.
            sprintf(szParamKey2, "SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\%s",SZSERVICENAME);
            if(0 != makeNewKey(HKEY_LOCAL_MACHINE, szParamKey2)){
 	    			_tprintf(TEXT("The EventLog subkey could not be created.\n"));
		    }

            // Set the file value (where the message resources are located.... in this case, our runfile.)
			if(	0 != setStringValue((const unsigned char *) szPath,
			    strlen(szPath) + 1,HKEY_LOCAL_MACHINE,
				szParamKey2,TEXT("EventMessageFile")))
			{
				    _tprintf(TEXT("The Message File value could\nnot be assigned.\n"));
		    }

            // Set the supported types flags.
            if(	0 != setDwordValue(EVENTLOG_INFORMATION_TYPE,HKEY_LOCAL_MACHINE,	szParamKey2,TEXT("TypesSupported"))){
				    _tprintf(TEXT("The Types Supported value could\nnot be assigned.\n"));
     		}

            // Try to create a subkey to hold the runtime args for the JavaVM and
            // Java application
            if(0 != makeNewKey(HKEY_LOCAL_MACHINE, szParamKey)){
                _tprintf(TEXT("Could not create Parameters subkey.\n"));
            }else{

                 //Create an argument string from the argument list
                convertArgListToArgString((LPTSTR) szAppParameters,2, argc, argv);
                if(NULL == szAppParameters){
                    _tprintf(TEXT("Could not create AppParameters string.\n"));
                }

                else{

                    // Try to save the argument string under the new subkey
                    if(0 != setStringValue(szAppParameters, strlen(szAppParameters)+1,
                        HKEY_LOCAL_MACHINE, szParamKey, SZAPPPARAMS)){
                            _tprintf(TEXT("Could not save AppParameters value.\n"));
                    }

                }
            }

        }
        else{
            _tprintf(TEXT("CreateService failed - %s\n"), GetLastErrorText(szErr, 256));
        }

        // Close the handle to the service control manager database
        CloseServiceHandle(schSCManager);
    }
    else{
        _tprintf(TEXT(SZSCMGRFAILURE), GetLastErrorText(szErr,256));
    }
}


// We'll try to stop, and then remove the service using this function.

void removeService()
{
    SC_HANDLE   schService;
    SC_HANDLE   schSCManager;
    char szParamKey2[1025];


    // First, get a handle to the service control manager
    schSCManager = OpenSCManager(
                        NULL,
                        NULL,
                        SC_MANAGER_ALL_ACCESS
                        );
    if (schSCManager){

        // Next get the handle to this service...
        schService = OpenService(schSCManager, TEXT(SZSERVICENAME), SERVICE_ALL_ACCESS);

        if (schService){

            // Now, try to stop the service by passing a STOP code thru the control manager
            if (ControlService( schService, SERVICE_CONTROL_STOP, &ssStatus)){

                _tprintf(TEXT("Stopping %s."), TEXT(SZSERVICEDISPLAYNAME));
                // Wait a second...
                Sleep( 1000 );

                // Poll the status of the service for SERVICE_STOP_PENDING
                while(QueryServiceStatus( schService, &ssStatus)){

                    // If the service has not stopped, wait another second
                    if ( ssStatus.dwCurrentState == SERVICE_STOP_PENDING ){
                        _tprintf(TEXT("."));
                        Sleep( 1000 );
                    }
                    else
                        break;
                }

                if ( ssStatus.dwCurrentState == SERVICE_STOPPED )
                    _tprintf(TEXT("\n%s stopped.\n"), TEXT(SZSERVICEDISPLAYNAME) );
                else
                    _tprintf(TEXT("\n%s failed to stop.\n"), TEXT(SZSERVICEDISPLAYNAME) );
            }

            // Now try to remove the service...
            if(DeleteService(schService)){
                _tprintf(TEXT("%s removed.\n"), TEXT(SZSERVICEDISPLAYNAME) );

                // Delete our eventlog registry key
                sprintf(szParamKey2, "SYSTEM\\CurrentControlSet\\Services\\EventLog\\Application\\%s",SZSERVICENAME);
                RegDeleteKey(HKEY_LOCAL_MACHINE,szParamKey2);
            }else{
                _tprintf(TEXT("DeleteService failed - %s\n"), GetLastErrorText(szErr,256));
            }

            //Close this service object's handle to the service control manager
            CloseServiceHandle(schService);
        }
        else{
            _tprintf(TEXT("OpenService failed - %s\n"), GetLastErrorText(szErr,256));
        }

        // Finally, close the handle to the service control manager's database
        CloseServiceHandle(schSCManager);


    }
    else{
        _tprintf(TEXT(SZSCMGRFAILURE), GetLastErrorText(szErr,256));
    }
}

// This function permits running the Java application from the
// console.

void runService(int argc, char ** argv)
{
    DWORD dwArgc;
    LPTSTR *lpszArgv;

#ifdef UNICODE
    lpszArgv = CommandLineToArgvW(GetCommandLineW(), &(dwArgc) );
#else
    dwArgc   = (DWORD) argc;
    lpszArgv = argv;
#endif

    _tprintf(TEXT("Running %s.\n"), TEXT(SZSERVICEDISPLAYNAME));


    // Do it! But since this is a console start, skip the first two
	// arguments in the arg list being passed, and reduce its size by
	// two also. (The first two command line args should be ignored
	// in a console run.)
    ServiceStart( dwArgc-2, lpszArgv+2);
}


// If running as a service, use event logging to post a message
// If not, display the message on the console.

VOID AddToMessageLog(LPTSTR lpszMsg)
{
  HANDLE  hEventSource;
	TCHAR	szMsg[4096];

#ifdef UNICODE
    LPCWSTR  lpszStrings[1];
#else
    LPCSTR   lpszStrings[1];
#endif

  FILE* file;
/*
  LPTSTR wrkdir;
  long size;
  char* buffer;
  char* pos;
  char filename[1024];
  char path[1024];
  int i;
  DWORD len = 1024-15;

  i = 0;
  wrkdir = getWorkingDirectory2();
  if (wrkdir == NULL){
    if (!getStringValue((unsigned char*)path, &len, HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\ZetaGrid", "InstallLocation") == 0) {
      strcpy(path, "c:\\zeta");
    }
  } else {
    strcpy(path, wrkdir);
  }
  strcpy(filename, path);
  strcat(path, "\\zeta.cfg");
  file = fopen(path, "rt");
  if (file != NULL) {
    fseek(file, 0, SEEK_END);
    size = ftell(file);
    rewind(file);
    buffer = (char*)malloc(size);
    if (buffer != NULL) {
      fread(buffer, 1, size, file);
      pos = strstr(buffer, "info.filename=");
      if (pos != NULL && pos > buffer) {
        if (*(pos-1) == '\n' || *(pos-1) == '\r') {
          pos += 14;
          i = strlen(filename);
          filename[i] = '\\';
          while (++i < 1012 && *pos != '\n' && *pos != '\r') {
            filename[i] = *pos++;
          }
          filename[i] = '\0';
        }
      }
      free(buffer);
    }
    fclose(file);
    if (i > 0) {
      file = fopen(filename, "wt");
      if (file != NULL) {
        fputs(lpszMsg, file);
        fclose(file);
      }
    }
  }
*/
  LPTSTR infoFilename;

  infoFilename = getInfoFilename2();
  file = fopen(infoFilename, "wt");
  if (file != NULL) {
    fputs(lpszMsg, file);
    fclose(file);
  }

  if (!bConsole) {
    hEventSource = RegisterEventSource(NULL, TEXT(SZSERVICENAME));
    _stprintf(szMsg, TEXT("%s: %s"), SZSERVICENAME, lpszMsg);
    lpszStrings[0] = szMsg;
    if (hEventSource != NULL) {
      ReportEvent(hEventSource, EVENTLOG_INFORMATION_TYPE, 0, EVENT_GENERIC_INFORMATION, NULL, 1, 0, lpszStrings, NULL);
      DeregisterEventSource(hEventSource);
    }
  } else {
    _tprintf(TEXT("%s\n"), lpszMsg);
  }
}


// Throughout the program, calls to SetServiceStatus are required
// which are handled by calling this function. Here, the non-constant
// members of the SERVICE_STATUS struct are assigned and SetServiceStatus
// is called with the struct. Note that we will not report to the service
// control manager if we are running as  console application.

BOOL ReportStatus(DWORD dwCurrentState,
                         DWORD dwWin32ExitCode,
                         DWORD dwWaitHint)
{
    static DWORD dwCheckPoint = 1;
    BOOL bResult = TRUE;

    if ( !bConsole )
    {
        if (dwCurrentState == SERVICE_START_PENDING)
            ssStatus.dwControlsAccepted = 0;
        else
            ssStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;

        ssStatus.dwCurrentState = dwCurrentState;
        ssStatus.dwWin32ExitCode = dwWin32ExitCode;
        ssStatus.dwWaitHint = dwWaitHint;

        if ( ( dwCurrentState == SERVICE_RUNNING ) ||
             ( dwCurrentState == SERVICE_STOPPED ) )
            ssStatus.dwCheckPoint = 0;
        else
            ssStatus.dwCheckPoint = dwCheckPoint++;

        if (!(bResult = SetServiceStatus( sshStatusHandle, &ssStatus))) {
            AddToMessageLog(TEXT("SetServiceStatus"));
        }
    }

    return bResult;
}

// Each Win32 service must have a control handler to respond to
// control requests from the dispatcher.

VOID WINAPI controlHandler(DWORD dwCtrlCode)
{
    switch(dwCtrlCode)
    {

        case SERVICE_CONTROL_STOP:
            // Request to stop the service. Report SERVICE_STOP_PENDING
            // to the service control manager before calling ServiceStop()
            // to avoid a "Service did not respond" error.
            ReportStatus(SERVICE_STOP_PENDING, NO_ERROR, 0);
            ServiceStop();
            return;


        case SERVICE_CONTROL_INTERROGATE:
            // This case MUST be processed, even though we are not
            // obligated to do anything substantial in the process.
            break;

         default:
            // Any other cases...
            break;

    }

    // After invocation of this function, we MUST call the SetServiceStatus
    // function, which is accomplished through our ReportStatus function. We
    // must do this even if the current status has not changed.
    ReportStatus(ssStatus.dwCurrentState, NO_ERROR, 0);
}

// The ServiceMain function is the entry point for the service.

void WINAPI serviceMain(DWORD dwArgc, LPTSTR *lpszArgv)
{

    TCHAR szAppParameters[8192];
    LONG lLen = 8192;

    LPTSTR *lpszNewArgv = NULL;
    DWORD dwNewArgc;

    UINT i;

	char szParamKey[1025];

	sprintf(szParamKey,"SYSTEM\\CurrentControlSet\\Services\\%s\\Parameters",SZSERVICENAME);

    // Call RegisterServiceCtrlHandler immediately to register a service control
    // handler function. The returned SERVICE_STATUS_HANDLE is saved with global
    // scope, and used as a service id in calls to SetServiceStatus.
    sshStatusHandle = RegisterServiceCtrlHandler( TEXT(SZSERVICENAME), controlHandler);

    if (!sshStatusHandle)
        goto finally;

    // The global ssStatus SERVICE_STATUS structure contains information about the
    // service, and is used throughout the program in calls made to SetStatus through
    // the ReportStatus function.
    ssStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    ssStatus.dwServiceSpecificExitCode = 0;


    // If we could guarantee that all initialization would occur in less than one
    // second, we would not have to report our status to the service control manager.
    // For good measure, we will assign SERVICE_START_PENDING to the current service
    // state and inform the service control manager through our ReportStatus function.
    if (!ReportStatus(SERVICE_START_PENDING, NO_ERROR, 3000))
        goto finally;

    // When we installed this service, we probably saved a list of runtime args
    // in the registry as a subkey of the key for this service. We'll try to get
    // it here...
    if(0 != getStringValue(szAppParameters,(LPDWORD)&lLen, HKEY_LOCAL_MACHINE, szParamKey, SZAPPPARAMS)){
        dwNewArgc = 0;
        lpszNewArgv = NULL;
    }
    else{
        //If we have an argument string, convert it to a list of argc/argv type...
        lpszNewArgv = convertArgStringToArgList(lpszNewArgv, &dwNewArgc, szAppParameters);
    }

    // Do it! In ServiceStart, we'll send additional status reports to the
    // service control manager, especially the SERVICE_RUNNING report once
    // our JVM is initiallized and ready to be invoked.
    ServiceStart(dwNewArgc, lpszNewArgv);

    // Release the allocated storage used by our arg list. Java programmers
    // might remember this kind of stuff.
    for(i=0; i<dwNewArgc; i++){
        GlobalFree((HGLOBAL)lpszNewArgv[i]);
    }
    if(dwNewArgc > 0)
        GlobalFree((HGLOBAL)lpszNewArgv);

finally:

    // Report the stopped status to the service control manager, if we have
    // a valid server status handle.
     if (sshStatusHandle)
        (VOID)ReportStatus( SERVICE_STOPPED, dwErr, 0);
}

void main(int argc, char **argv)
{
    // The StartServiceCtrlDispatcher requires this table to specify
    // the ServiceMain function to run in the calling process. The first
    // member in this example is actually ignored, since we will install
    // our service as a SERVICE_WIN32_OWN_PROCESS service type. The NULL
    // members of the last entry are necessary to indicate the end of
    // the table;
    SERVICE_TABLE_ENTRY serviceTable[] =
    {
        { TEXT(SZSERVICENAME), (LPSERVICE_MAIN_FUNCTION)serviceMain },
        { NULL, NULL }
    };

    // This app may be started with one of three arguments, -i, -r, and
    // -c, or -?, followed by actual program arguments. These arguments
    // indicate if the program is to be installed, removed, run as a
    // console application, or to display a usage message.
    if(argc > 1){
        if(!stricmp(argv[1],"-i") || !stricmp(argv[1],"/i")){
            installService(argc,argv);
        }
        else if(!stricmp(argv[1],"-r") || !stricmp(argv[1],"/r")){
            removeService();
        }
        else if(!stricmp(argv[1],"-c") || !stricmp(argv[1],"/c")){
            bConsole = TRUE;
            runService(argc,argv);
        }
        else{
            printf("\nUnrecognized option: %s\n", argv[1]);
        }
        exit(0);

    }

    // If main is called without any arguments, it will probably be by the
    // service control manager, in which case StartServiceCtrlDispatcher
    // must be called here. A message will be printed just in case this
    // happens from the console.
    printf("\nNo arguments in command line...");
    printf("\nCalling StartServiceCtrlDispatcher...please wait.\n");
    if (!StartServiceCtrlDispatcher(serviceTable)){
            printf("\n%s\n", SZFAILURE);
            AddToMessageLog(TEXT(SZFAILURE));
    }
}

void startService()
{
    // The StartServiceCtrlDispatcher requires this table to specify
    // the ServiceMain function to run in the calling process. The first
    // member in this example is actually ignored, since we will install
    // our service as a SERVICE_WIN32_OWN_PROCESS service type. The NULL
    // members of the last entry are necessary to indicate the end of
    // the table;
    SERVICE_TABLE_ENTRY serviceTable[] =
    {
        { TEXT(SZSERVICENAME), (LPSERVICE_MAIN_FUNCTION)serviceMain },
        { NULL, NULL }
    };

    // If main is called without any arguments, it will probably be by the
    // service control manager, in which case StartServiceCtrlDispatcher
    // must be called here. A message will be printed just in case this
    // happens from the console.
    printf("\nCalling StartServiceCtrlDispatcher...please wait.\n");
    if (!StartServiceCtrlDispatcher(serviceTable)){
            printf("\n%s\n", SZFAILURE);
            AddToMessageLog(TEXT(SZFAILURE));
    }
}
