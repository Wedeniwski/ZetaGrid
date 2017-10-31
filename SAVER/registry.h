/*
 * registry.h    2.0 (July 22 2001)
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

#ifndef _REGISTRY_H
#define _REGISTRY_H


#ifdef __cplusplus
extern "C" {
#endif

//
//  FUNCTION: getStringValue()
//
//  PURPOSE: Fetches a REG_SZ or REG_EXPAND_SZ string value
//           from a specified registry key    
//
//  PARAMETERS:
//    lpVal - a string buffer for the desired value
//    lpcbLen  - pointer to LONG value with buffer length
//    hkRoot - the primary root key, e.g. HKEY_LOCAL_MACHINE
//    lpszPath - the registry path to the subkey containing th desired value
//    lpszValue - the name of the desired value    
//
//  RETURN VALUE:
//    0 on success, 1 on failure
//
int getStringValue(LPBYTE lpVal, LPDWORD lpcbLen, HKEY hkRoot, LPCTSTR lpszPath, LPTSTR lpszValue);

//
//  FUNCTION: setStringValue()
//
//  PURPOSE: Assigns a REG_SZ value to a 
//           specified registry key    
//
//  PARAMETERS:
//    lpVal - Constant byte array containing the value
//    cbLen  - data length
//    hkRoot - the primary root key, e.g. HKEY_LOCAL_MACHINE
//    lpszPath - the registry path to the subkey containing th desired value
//    lpszValue - the name of the desired value    
//
//  RETURN VALUE:
//    0 on success, 1 on failure
//
int setStringValue(CONST BYTE *lpVal, DWORD cbLen, HKEY hkRoot, LPCTSTR lpszPath, LPCTSTR lpszValue);


//
//  FUNCTION: makeNewKey()
//
//  PURPOSE: Creates a new key at the specified path  
//
//  PARAMETERS:
//    hkRoot - the primary root key, e.g. HKEY_LOCAL_MACHINE
//    lpszPath - the registry path to the new subkey
//
//  RETURN VALUE:
//    0 on success, 1 on failure
//
int makeNewKey(HKEY hkRoot, LPCTSTR lpszPath);

int setDwordValue(DWORD data, HKEY hkRoot, LPCTSTR lpszPath, LPCTSTR lpszValue);

#ifdef __cplusplus
}
#endif

#endif
