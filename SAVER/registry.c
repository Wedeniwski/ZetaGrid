/*
 * registry.c    2.0 (July 22 2001)
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
 
#include<windows.h>
#include<stdio.h>
#include<stdlib.h>
#include<winreg.h>

#include "registry.h"

int getStringValue(LPBYTE lpVal, LPDWORD lpcbLen, HKEY hkRoot, LPCTSTR lpszPath, LPTSTR lpszValue)
{

    LONG result;
    HKEY hKey;

    DWORD dwType;

    result = RegOpenKeyEx(
        hkRoot,
        lpszPath,
        (DWORD)0,
        KEY_EXECUTE | KEY_QUERY_VALUE,
        (PHKEY)&hKey);

    if(result != ERROR_SUCCESS){
        return 1;
    }

    result = RegQueryValueEx(
        hKey,
        lpszValue, 
        NULL, 
        (LPDWORD)&dwType, 
        lpVal, 
        lpcbLen);    

    RegCloseKey(hKey);

    return !(result == ERROR_SUCCESS && 
        (dwType == REG_SZ || dwType == REG_EXPAND_SZ));
}

int setStringValue(CONST BYTE *lpVal, DWORD cbLen, HKEY hkRoot, LPCTSTR lpszPath, LPCTSTR lpszValue)
{

    LONG result;
    HKEY hKey;

    DWORD dwType = REG_SZ;

    result = RegOpenKeyEx(
        hkRoot,
        lpszPath,
        (DWORD)0,
        KEY_WRITE,
        (PHKEY)&hKey);

    if(result != ERROR_SUCCESS){
        return 1;
    }

    result = RegSetValueEx(
        hKey,
        lpszValue, 
        (DWORD)0, 
        dwType, 
        lpVal, 
        cbLen);    

    RegCloseKey(hKey);

    return !(result == ERROR_SUCCESS);
}

int makeNewKey(HKEY hkRoot, LPCTSTR lpszPath)
{
    char *classname = "LocalSystem";

    LONG result;
    HKEY hKey;
    DWORD disposition;


    result = RegCreateKeyEx(
        hkRoot,
        lpszPath,
        (DWORD)0,
        classname,
        REG_OPTION_NON_VOLATILE,
        KEY_ALL_ACCESS,
        NULL,
        (PHKEY)&hKey,
        (LPDWORD) &disposition);

    if(result != ERROR_SUCCESS){
        return 1;
    }


    RegCloseKey(hKey);

    return !(result == ERROR_SUCCESS);
}


int setDwordValue(DWORD data, HKEY hkRoot, LPCTSTR lpszPath, LPCTSTR lpszValue)
{

	LONG	result;
	HKEY	hKey;

	result = RegOpenKeyEx(hkRoot, lpszPath, (DWORD) 0, KEY_WRITE, (PHKEY) & hKey);

	if(result != ERROR_SUCCESS)
	{
		return 1;
	}

	result = RegSetValueEx(
        hKey, 
        lpszValue, 
        0, 
        REG_DWORD, 
        (CONST BYTE*)&data, 
        sizeof(DWORD));

	RegCloseKey(hKey);

	return !(result == ERROR_SUCCESS);
}
