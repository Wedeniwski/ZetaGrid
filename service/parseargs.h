/*
 * parseargs.h    2.0 (July 22 2001)
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

  Febrary 22 2002, add function getJVMPath and getPriority by Sebastian Wedeniwski
 * 
 */

#ifndef _PARSEARGS_H
#define _PARSEARGS_H

#include<windows.h>


#ifdef __cplusplus
extern "C" {
#endif

//
//  FUNCTION: getWorkingDirectory()
//
//  PURPOSE: Get the working directory for the application based
//           on the wrkdir= command line value.
//
//  PARAMETERS:
//		dwArgc - The length of the argument list
//		lpszArgv - The argument list
//
//  RETURN VALUE:
//    String containing the name of the assigned working directory or
//    NULL on failure
//
LPTSTR getWorkingDirectory(DWORD dwArgc, LPTSTR *lpszArgv);
LPTSTR getInfoFilename(DWORD dwArgc, LPTSTR *lpszArgv);
LPTSTR getInfoFilename2();
LPTSTR getJVMPath(DWORD dwArgc, LPTSTR *lpszArgv);
LPTSTR getPriority(DWORD dwArgc, LPTSTR *lpszArgv);

//
//  FUNCTION: getJavaArgs()
//
//  PURPOSE: Return an array of strings containing all arguments that
//           begin with -D or /D (case-sensitive).
//
//  PARAMETERS:
//    lpszArgs  - The string array address to be allocated and receive the data
//    pdwLen - pointer to an int that will contain the returned array length
//    dwArgc - length of the argument list
//    lpszArgv - array of strings, the argument list.  
//
//  RETURN VALUE:
//    String array address containing the filtered arguments
//    NULL on failure
//
LPTSTR *getJavaArgs(LPTSTR *lpszArgs, PDWORD pdwLen, DWORD dwArgc, LPTSTR *lpszArgv);

//
//  FUNCTION: getAppArgs()
//
//  PURPOSE: Return an array of strings containing all arguments that
//           do not begin with -D, -X, /X or /D (case-sensitive) or wrkdir=
//
//  PARAMETERS:
//    lpszArgs  - The string array address to be allocated and receive the data
//    pdwLen - pointer to an int that will contain the returned array length
//    dwArgc - length of the argument list
//    lpszArgv - array of strings, the argument list.  
//
//  RETURN VALUE:
//    String array address containing the filtered arguments
//    NULL on failure
//
LPTSTR *getAppArgs(LPTSTR *lpszArgs, PDWORD pdwLen, DWORD dwArgc, LPTSTR *lpszArgv);

//
//  FUNCTION: convertArgStringToArgList()
//
//  PURPOSE: Return an array of strings containing all arguments that
//           are parsed from a tab-delimited argument string.
//
//  PARAMETERS:
//    args  - The string array address to be allocated and receive the data
//    len - pointer to an int that will contain the returned array length
//    argstring - string containing arguments to be parsed.  
//
//  RETURN VALUE:
//    String array address containing the filtered arguments
//    NULL on failure
//
LPTSTR* convertArgStringToArgList(LPTSTR *args, PDWORD pdwLen, LPTSTR lpszArgstring);

//
//  FUNCTION: convertArgListToArgString()
//
//  PURPOSE: Create a single tab-delimited string of arguments from
//           an argument list
//
//  PARAMETERS:
//    target - pointer to the string to be allocated and created
//    start  - zero-based offest into the list to the first arg value used to
//             build the list.
//    argc - length of the argument list
//    argv - array of strings, the argument list.  
//
//  RETURN VALUE:
//    Character pointer to the target string.
//    NULL on failure
//
LPTSTR convertArgListToArgString(LPTSTR lpszTarget, DWORD dwStart, DWORD dwArgc, LPTSTR *lpszArgv);

#ifdef __cplusplus
}
#endif

#endif
