/*
 * service.h    2.0 (July 22 2001)
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

#ifndef _SERVICE_H
#define _SERVICE_H


#ifdef __cplusplus
extern "C" {
#endif


// =========================================================
// TO DO: change as needed for specific Java app and service
// =========================================================

// internal name of the service
#define SZSERVICENAME        "ZetaGrid"

// displayed name of the service
#define SZSERVICEDISPLAYNAME "ZetaGrid"

// Service TYPE Permissable values:
//		SERVICE_AUTO_START
//		SERVICE_DEMAND_START
//		SERVICE_DISABLED
#define SERVICESTARTTYPE SERVICE_AUTO_START

// list of service dependencies - "dep1\0dep2\0\0"
// If none, use ""
#define SZDEPENDENCIES ""

// Main java class
#define SZMAINCLASS "zeta/ZetaClient"

// Name of the Java SCMEventManager
// If none... 
#define SZSCMEVENTMANAGER ""
// Otherwise, provide full package qualified path to SCMEventManager
//#define SZSCMEVENTMANAGER "com/kcmultimedia/demo/SCMEventManager"

// =========================================================
// You should not need any changes below this line
// =========================================================

// Value name for app parameters
#define SZAPPPARAMS "AppParameters"

//
//  FUNCTION: getConsoleMode()
//
//  PURPOSE: Is the app running as a service or a console app.
//
//  RETURN VALUE:
//    TRUE  - if running as a console application 
//    FALSE - if running as a service
//
BOOL getConsoleMode();

//
//  FUNCTION: ReportStatusToSCMgr()
//
//  PURPOSE: Sets the current status of the service and
//           reports it to the Service Control Manager
//
//  PARAMETERS:
//    dwCurrentState - the state of the service
//    dwWin32ExitCode - error code to report
//    dwWaitHint - worst case estimate to next checkpoint
//
//  RETURN VALUE:
//    TRUE  - success 
//    FALSE - failure
//
BOOL ReportStatus(DWORD dwCurrentState, DWORD dwWin32ExitCode, DWORD dwWaitHint);


//
//  FUNCTION: AddToMessageLog(LPTSTR lpszMsg)
//
//  PURPOSE: Allows any thread to log an error message
//
//  PARAMETERS:
//    lpszMsg - text for message
//
//  RETURN VALUE:
//    none
//
void AddToMessageLog(LPTSTR lpszMsg);

VOID ServiceStart(DWORD dwArgc, LPTSTR *lpszArgv);
VOID ServiceStop();

void removeService();
void installService(int argc, char **argv);
void startService();

#ifdef __cplusplus
}
#endif

#endif
