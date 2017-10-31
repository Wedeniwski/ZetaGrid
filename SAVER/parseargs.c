/*
 * parseargs.c    2.0 (July 22 2001)
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
#include<stdio.h>
#include<stdlib.h>
#include<string.h>

#include "parseargs.h"

#define MAX_ARGLEN 8192

#define PREFIX1 "-D"
#define PREFIX2 "/D"
#define PREFIX3 "-X"
#define PREFIX4 "/X"
#define WRKDIR  "wrkdir="
#define LIBDIR  "libdir="
#define INFO    "info="
#define PRIO    "prio="

LPSTR infoFilename;

LPTSTR getWorkingDirectory(DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT    i;
    TCHAR   szArg[MAX_ARGLEN];
    for(i=0; i < dwArgc; i++){
        if(strlen(lpszArgv[i]) > 7 && !strnicmp(lpszArgv[i],WRKDIR,7)){
			      return lpszArgv[i]+7;
        }
    }

	return NULL;
}

LPTSTR getInfoFilename(DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT    i;
    TCHAR   szArg[MAX_ARGLEN];
    for(i=0; i < dwArgc; i++){
        if(strlen(lpszArgv[i]) > 5 && !strnicmp(lpszArgv[i],INFO,5)){
            infoFilename = lpszArgv[i]+5;
			      return infoFilename;
        }
    }
	return NULL;
}

LPTSTR getInfoFilename2()
{
  return infoFilename;
}

LPTSTR getJVMPath(DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT    i;

    TCHAR   szArg[MAX_ARGLEN];


    for(i=0; i < dwArgc; i++){
        if(strlen(lpszArgv[i]) > 7 && !strnicmp(lpszArgv[i],LIBDIR,7)){
			return lpszArgv[i]+7;
        }
    }

	return NULL;
}


LPTSTR getPriority(DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT    i;

    TCHAR   szArg[MAX_ARGLEN];


    for(i=0; i < dwArgc; i++){
        if(strlen(lpszArgv[i]) > 5 && !strnicmp(lpszArgv[i],PRIO,5)){
			return lpszArgv[i]+5;
        }
    }

	return NULL;
}


LPTSTR *getJavaArgs(LPTSTR *lpszArgs, PDWORD pdwLen, DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT    i,
            uCount;

    TCHAR   szArg[MAX_ARGLEN];


    for(i=0,uCount=0; i < dwArgc; i++){
        if(!strncmp(lpszArgv[i],PREFIX1,2) || !strncmp(lpszArgv[i],PREFIX2,2)
			|| !strncmp(lpszArgv[i],PREFIX3,2) || !strncmp(lpszArgv[i],PREFIX4,2)){
            uCount++;
        }
    }

    if(uCount == 0)
        return NULL;

    lpszArgs = (LPTSTR *)GlobalAlloc(GMEM_FIXED, uCount * sizeof(LPTSTR));
    *pdwLen = uCount;

    for(i=0,uCount=0; i < dwArgc; i++){
        if(!strncmp(lpszArgv[i],PREFIX1,2) || !strncmp(lpszArgv[i],PREFIX2,2)
			|| !strncmp(lpszArgv[i],PREFIX3,2) || !strncmp(lpszArgv[i],PREFIX4,2)){
            strcpy(szArg, lpszArgv[i]);
            lpszArgs[uCount] = (LPTSTR)GlobalAlloc(GMEM_FIXED,strlen(szArg)+1);
            strcpy(lpszArgs[uCount],szArg);
            uCount++;
        }
    }

    return lpszArgs;
}

LPTSTR *getAppArgs(LPTSTR *lpszArgs, PDWORD pdwLen, DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT    i,
            uCount;

    TCHAR   szArg[MAX_ARGLEN];


    for(i=0,uCount=0; i < dwArgc; i++){
        if(strncmp(lpszArgv[i],PREFIX1,2) && strncmp(lpszArgv[i],PREFIX2,2) && strnicmp(lpszArgv[i],WRKDIR,7)
      && strnicmp(lpszArgv[i],LIBDIR,7) && strnicmp(lpszArgv[i],PRIO,5) && strnicmp(lpszArgv[i],INFO,5)
			&& strncmp(lpszArgv[i],PREFIX3,2) && strncmp(lpszArgv[i],PREFIX4,2)){
            uCount++;
        }
    }

    if(uCount == 0)
        return NULL;

    lpszArgs = (LPTSTR *)GlobalAlloc(GMEM_FIXED, uCount * sizeof(LPTSTR));
    *pdwLen = uCount;

    for(i=0,uCount=0; i < dwArgc; i++){
        if(strncmp(lpszArgv[i],PREFIX1,2) && strncmp(lpszArgv[i],PREFIX2,2) && strnicmp(lpszArgv[i],WRKDIR,7)
      && strnicmp(lpszArgv[i],LIBDIR,7) && strnicmp(lpszArgv[i],PRIO,5) && strnicmp(lpszArgv[i],INFO,5)
			&& strncmp(lpszArgv[i],PREFIX3,2) && strncmp(lpszArgv[i],PREFIX4,2)){
            strcpy(szArg, lpszArgv[i]);
            lpszArgs[uCount] = (LPTSTR)GlobalAlloc(GMEM_FIXED,strlen(szArg)+1);
            strcpy(lpszArgs[uCount],szArg);
            uCount++;
        }
    }

    return lpszArgs;
}

LPTSTR *convertArgStringToArgList(LPTSTR *lpszArgs, PDWORD pdwLen, LPTSTR lpszArgstring)
{
    UINT uCount;
    LPTSTR lpszArg, lpszToken;


    if(strlen(lpszArgstring) == 0){
        *pdwLen = 0;
        //lpszArgs = NULL;
        return NULL;
    }

    if(NULL == (lpszArg = (LPTSTR)GlobalAlloc(GMEM_FIXED,strlen(lpszArgstring)+1))){
        *pdwLen = 0;
        //lpszArgs = NULL;
        return NULL;
    }

    strcpy(lpszArg, lpszArgstring);

    lpszToken = strtok( lpszArg, "\t" ); 
    uCount = 0;
    while( lpszToken != NULL ){
        uCount++;
        lpszToken = strtok( NULL, "\t");   
    }

    GlobalFree((HGLOBAL)lpszArg);

    lpszArgs = (LPTSTR *)GlobalAlloc(GMEM_FIXED,uCount * sizeof(LPTSTR));
    *pdwLen = uCount;


    lpszToken = strtok(lpszArgstring,"\t");
    uCount = 0;
    while(lpszToken != NULL){
        lpszArgs[uCount] = (LPTSTR)GlobalAlloc(GMEM_FIXED,strlen(lpszToken)+1);
        strcpy(lpszArgs[uCount],lpszToken);
        uCount++;
        lpszToken = strtok( NULL, "\t"); 
    }


    return lpszArgs;

}

LPTSTR convertArgListToArgString(LPTSTR lpszTarget, DWORD dwStart, DWORD dwArgc, LPTSTR *lpszArgv)
{
    UINT i;

    if(dwStart >= dwArgc){
        return NULL;
    }

    *lpszTarget = 0;

    for(i=dwStart; i<dwArgc; i++){

        if(i != dwStart){
            strcat(lpszTarget,"\t");
        }
        strcat(lpszTarget,lpszArgv[i]);
    }

    return lpszTarget;
}

