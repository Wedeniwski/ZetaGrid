/*
 * javaserver.c    2.0 (July 22 2001)
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
#include <jni.h>

#ifdef BORLAND
#include <dir.h>
#endif

#include "service.h"
#include "parseargs.h"
#include "registry.h"


// Twenty args passed to Java should be enough. Make this bigger if you need more.
#define MAX_OPTIONS 20

// These should be the same integer values used in SCMEvent.java
#define JS_SERVICE_STOPPED 1
#define JS_SHUTDOWN 2
#define JS_CLOSE 3
#define JS_LOGOFF 4

typedef jint (JNICALL *CreateJavaVM_t) (JavaVM **pvm, void **env, void *args);
typedef jint (JNICALL *GetDefaultJavaVMInitArgs_t) (void *args);

typedef struct{
    CreateJavaVM_t CreateJavaVM;
    GetDefaultJavaVMInitArgs_t GetDefaultJavaVMInitArgs;
}invoker_t;

invoker_t invoker;

HANDLE  hServerStopEvent = NULL;

LPTSTR *lpszJavaArgs=NULL, 
       *lpszAppArgs=NULL;

DWORD dwJLen=0,
      dwALen=0;

DWORD dwSubALen=0;

LPTSTR wrkdir;
LPTSTR infoFilename;
LPTSTR JVMPathParam;
LPTSTR processPriority;

static JNIEnv  *env;
static jobject jobj = NULL;
static JavaVM  *vm;



int JNIError(JNIEnv *env, char *msg)
{

    int hasException = (*env)->ExceptionCheck(env);


    if(JNI_TRUE == hasException)
    {
        AddToMessageLog(TEXT(msg));
        (*env)->ExceptionClear(env);
    }

    return hasException;
}

int isNullValue(void *value, char *msg)
{

    if(NULL == value)
    {
        AddToMessageLog(TEXT(msg));
    }

    return NULL == value;
}


//Here we attempt to call a java instance method from native code. We obtained
//a global reference to the singleton SCMEventManager object associated with our
//Java application when we invoked the JVM (if implemented.)
//
//Call this method to pass an event ID to Java's SCMEventManager.dispatchSCMEvent
//In this code, we send CLOSE, LOGOFF, SHUTDOWN and STOP events to a listening
//Java application (one that uses SCMEventManager or an equivalent) The Java
//application can respond to the events as applicable.
VOID PassSCMEvent(int eventid)
{

    jint        j = (jint) eventid;
    jmethodID   mid;
    jclass      cls;

    /* jobj is the global reference to the SCMEventManager */
    if(NULL == jobj)
    {
        return;
    }
    (*vm)->AttachCurrentThread(vm, (void **) &env, NULL);


    cls = (*env)->GetObjectClass(env, jobj);
    if(JNIError(env, "JNI error Cannot get SCMEventManager class") || isNullValue(cls, "JNI error Cannot get SCMEventManager class"))
    {
        goto finished;
    }

    mid = (*env)->GetMethodID(env, cls, "dispatchSCMEvent", "(I)V");
    if(JNIError(env, "JNI error Cannot get dispatchSCMEvent method") || isNullValue(mid, "JNI error Cannot get dispatchSCMEvent method"))
    {
        goto finished;
    }
    (*env)->CallVoidMethod(env, jobj, mid, j);
    if(JNIError(env, "Cannot get dispatchSCMEvent method"))
    {
        goto finished;
    }

  finished:

    (*vm)->DetachCurrentThread(vm);
}

typedef void (*CEC)(void);
typedef void (*CEC2)(int);

// Here we intercept some console events of interest. Originally this was done
// only to bypass a JDK 1.3 issue concerning logoffs. However it also useful for 
// desktop-interactive services. The example Java application SrvcDemo.java
// demonstrates receiving events
BOOL WINAPI logoffHandler(DWORD dwCtrlType)
{
    switch (dwCtrlType){

        
        case CTRL_C_EVENT:
            // If a console run, hitting CTRL-C will will try to stop the app.
            // As a service in interactive desktop mode, it will be ignored
			if(TRUE == getConsoleMode()) 
				ServiceStop();
            return TRUE;

        case CTRL_CLOSE_EVENT:
            // If a console run, hitting the CLOSE icon will try to stop the app.
            // As a desktop-interactive service it will be passed to Java, although NT
            // will bring up the non-responding program warning (hit Cancel)
            if(0 != jobj){
                PassSCMEvent(JS_CLOSE);
            }
	        if(TRUE == getConsoleMode()){
                ServiceStop();
            }
            return TRUE;

        case CTRL_SHUTDOWN_EVENT:
			// This event can be passed to Java but since 
			// a ServiceStop call immediately follows, no
			// action need be taken by the listener.
            if(0 != jobj)
               PassSCMEvent(JS_SHUTDOWN);
			ServiceStop();
            return TRUE;

        case CTRL_LOGOFF_EVENT:
			// This is another event that can typically be ignored.
			// This lets Java survive a logoff.
            if(0 != jobj)
                PassSCMEvent(JS_LOGOFF);
            // But if we'ree running as a console app, stop
            if(TRUE == getConsoleMode()) 
				ServiceStop();
            return TRUE;
    }
    return FALSE;
}


void invokeJVM(void *noarg)
{
    char    szParamKey[1025];
    LONG   lLen = 1024;
    LONG   lMax = 1025L;
    char szJavaVersion[1025];
    char *vendors[] = { "JavaSoft\\Java Runtime Environment", "JavaSoft\\Java Development Kit",
                        "IBM\\Java3 Runtime Environment", "IBM\\Java3 Development Kit",
                        "IBM\\Java2 Runtime Environment", "IBM\\Java2 Development Kit",
                        "IBM\\Java Runtime Environment", "IBM\\Java Development Kit",
                        ""};
    char JVMPath[_MAX_PATH];

    jint            res;
    jclass          cls;
    jmethodID       mid;
    jstring         jstr;
    jobjectArray    args;
    JavaVMInitArgs  vm_args;
    JavaVMOption    options[MAX_OPTIONS];
    UINT            i;
    char            buf[256];
    jclass          cls2;
    jmethodID       mid2;
    HINSTANCE       handle;
    char dummy[2048];

    if (JVMPathParam != NULL) {
        strcpy(JVMPath, JVMPathParam);
    }
    if (*JVMPath == '\0') {
        for(i = 0; *vendors[i]; i++){
            sprintf(szParamKey, "Software\\%s",vendors[i]);
    
            if(0 == getStringValue(szJavaVersion, (LPDWORD)&lLen, HKEY_LOCAL_MACHINE, szParamKey, "CurrentVersion"))
            {
                break;
            }
            *szJavaVersion = 0;
        }
    
        if(!*szJavaVersion){
             AddToMessageLog(TEXT("Cannot locate JavaSoft or IBM registry information. Is the JRE properly installed?"));
            return;
        }
    
        sprintf(szParamKey, "Software\\%s\\%s",vendors[i], szJavaVersion);
    
    
        if(0 != getStringValue(JVMPath, (LPDWORD)&lMax, HKEY_LOCAL_MACHINE, szParamKey, "RuntimeLib"))
        {
            sprintf(dummy, "Cannot locate JRE %s registry information. Is the JRE properly installed?", szJavaVersion);
            AddToMessageLog(TEXT(dummy));
            return;
        }
    }
    if(NULL != wrkdir){
        if(0 != chdir(wrkdir)){
            sprintf(dummy, "Unable to change working directory: '%s'", wrkdir);
            AddToMessageLog(TEXT(dummy));
            return;
        }
    }

    AllocConsole();

    /* Load the Java VM DLL */
    if((handle = LoadLibrary(JVMPath)) == 0)
    {
        sprintf(dummy,"Error loading: %s\n", JVMPath);
        AddToMessageLog(TEXT(dummy));
        return;
    }

    /* Now get the function addresses */
    invoker.CreateJavaVM = (void *) GetProcAddress(handle, "JNI_CreateJavaVM");

    invoker.GetDefaultJavaVMInitArgs = (void *) GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs");

    if(invoker.CreateJavaVM == 0 || invoker.GetDefaultJavaVMInitArgs == 0)
    {
        sprintf(dummy, "Cannot find JNI interfaces in: %s\n", JVMPath);
        AddToMessageLog(TEXT(dummy));
        return;
    }

    if(dwJLen > MAX_OPTIONS)
    {
        sprintf(dummy, "Max. number of Java args (%d) exceeded.", MAX_OPTIONS);
        AddToMessageLog(TEXT(dummy));
        return;
    }

    /*
     * Assign the arguments for the JVM, such as the classpath,
     * RMI codebase, etc.
     */
    for(i = 0; i < dwJLen; i++)
    {
        options[i].optionString = lpszJavaArgs[i];
    }

    vm_args.version = JNI_VERSION_1_2;
    vm_args.options = options;
    vm_args.nOptions = dwJLen;
    vm_args.ignoreUnrecognized = TRUE;

    /* res =JNI_CreateJavaVM(&vm, (void **)&env, &vm_args); */
    res = invoker.CreateJavaVM(&vm, (void **) &env, &vm_args);

    if(res < 0)
    {
        AddToMessageLog(TEXT("Cannot create Java VM.\n"));
        return;
    }

    /* Get the main class */
    cls = (*env)->FindClass(env, SZMAINCLASS);
    if(JNIError(env, "JNI error finding main class.") || isNullValue(cls, "JNI error finding main class."))
    {
        return;
    }

    /* Get the method ID for the class's main(String[]) function. */
    mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
    if(JNIError(env, "JNI error finding main(String[]) method.") || isNullValue(mid, "JNI error finding main(String[]) method."))
    {
        return;
    }

    /*
     * If there are arguments, create an ObjectArray sized to contain the
     * argument list, and then scan the list, inserting each argument into
     * the ObjectArray.
     */
    if(dwSubALen > 0)
    {
      dwALen -= dwSubALen;
    }
    if(dwALen > 0)
    {
        args = (*env)->NewObjectArray(env, dwALen, (*env)->FindClass(env, "java/lang/String"), NULL);
        if(JNIError(env, "JNI error instantiating argument array.") || isNullValue(args, "JNI error instantiating argument array."))
        {
            return;
        }

        for(i = 0; i < dwALen; i++)
        {
            /* printf("\n%d= %s",i,lpszAppArgs[i]); */
            jstr = (*env)->NewStringUTF(env, lpszAppArgs[i]);
            if(JNIError(env, "JNI error instantiating argument.") || isNullValue(jstr,  "JNI error instantiating argument."))
            {
                return;
            }
            (*env)->SetObjectArrayElement(env, args, i , jstr);
            if(JNIError(env, "JNI error assigning argument."))
            {
                return;
            }
        }
    }

    /*
     * Otherwise, create an empty array. This is needed to avoid
     * creating an overloaded main that takes no arguments in the Java
     * app, and then getting a different method ID to the no-argument
     * main() method in this invoker code.
     */
    else
    {
        args = (*env)->NewObjectArray(env, 0, (*env)->FindClass(env, "java/lang/String"), NULL);
        if(JNIError(env, "JNI error instantiating empty argument array."))
        {
            return;
        }
    }
    if(dwSubALen > 0)
    {
      dwALen += dwSubALen;
    }

    if(strlen(SZSCMEVENTMANAGER) > 0)
    {
        /* Now, get the class of the java SCMEventManager */
        cls2 = (*env)->FindClass(env, SZSCMEVENTMANAGER);
        if(JNIError(env, "JNI error finding SCMEventManager.") || isNullValue(cls2, "JNI error finding SCMEventManager."))
        {
            //*szSCMEventManager = 0;
            jobj = NULL;
            goto finished;
        }

        /* Get the method ID for SCMEventManager.getInstance() */
        sprintf(buf, "()L%s;", SZSCMEVENTMANAGER);
        mid2 = (*env)->GetStaticMethodID(env, cls2, "getInstance", buf);
        if(JNIError(env, "Cannot find SCMEventManager.getInstance.") || isNullValue(mid2, "Cannot find SCMEventManager.getInstance."))
        {
            //*szSCMEventManager = 0;
            jobj = NULL;
            goto finished;
        }

        /*
         * Call SCMEventManager.getInstance() and save the returned object
         * We'll use this later on.
         */
        jobj = (*env)->NewGlobalRef(env, (*env)->CallStaticObjectMethod(env, cls2, mid2));
        if(JNIError(env, "Cannot call SCMEventManager.getInstance.") || isNullValue(jobj, "Cannot call SCMEventManager.getInstance."))
        {
            //*szSCMEventManager = 0;
            jobj = NULL;
            goto finished;
        }
    }

finished:
       
    SetConsoleCtrlHandler(logoffHandler, TRUE);

    /* Run the main class... */
    (*env)->CallStaticVoidMethod(env, cls, mid, args);
    JNIError(env, "Could not invoke main class");

    //This blocks until the JVM stops...
    (*vm)->DestroyJavaVM(vm);
}


VOID ServiceStop()
{
    UINT i;
    CEC callExitCalculation;
    CEC2 callExitCalculation2;
    HINSTANCE local_hLib;

    DWORD len = 1024-15;
    char path[1024];
    if (wrkdir == NULL){
      if (!getStringValue((unsigned char*)path, &len, HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\ZetaGrid", "InstallLocation") == 0) {
        strcpy(path, "c:\\zeta");
      }
    } else {
      strcpy(path, wrkdir);
    }
    strcat(path, "\\zeta_zeros.dll");
    local_hLib = LoadLibrary(path);
    if (local_hLib) {
      callExitCalculation2 = (CEC2)GetProcAddress((HMODULE)local_hLib, "exitCalculation2");
      if (callExitCalculation2) {
        (*callExitCalculation2)(1);
      } else {
        callExitCalculation = (CEC)GetProcAddress((HMODULE)local_hLib, "exitCalculation");
        if (callExitCalculation) {
          (*callExitCalculation)();
        }
      }
      FreeLibrary(local_hLib);
    }
    // ToDo: flexible for different tasks
/*    dwSubALen = 0;
    invokeJVM(NULL);
    dwSubALen = 1;*/

    if(0 != jobj)
        PassSCMEvent(JS_SERVICE_STOPPED);

    // Release any allocated data and pointers
    for(i=0;i<dwJLen;i++){
        GlobalFree((HGLOBAL)lpszJavaArgs[i]);
    }
    if(lpszJavaArgs > 0)
        GlobalFree((HGLOBAL)lpszJavaArgs);

    for(i=0;i<dwALen;i++){
        GlobalFree((HGLOBAL)lpszAppArgs[i]);
    }
    if(lpszAppArgs > 0)
        GlobalFree((HGLOBAL)lpszAppArgs);

    // Signal the stop event.
    if ( hServerStopEvent ){
        SetEvent(hServerStopEvent);
    }
}


// This method is called from ServiceMain() when NT starts the service
// or by runService() if run from the console.

VOID ServiceStart (DWORD dwArgc, LPTSTR *lpszArgv)
{


    // Let the service control manager know that the service is
    // initializing.
    if (!ReportStatus(
        SERVICE_START_PENDING,
        NO_ERROR,
        3000))
        //goto cleanup;
        return;


    // Create a Stop Event
    hServerStopEvent = CreateEvent(
        NULL,
        TRUE,
        FALSE,
        NULL);


    if ( hServerStopEvent == NULL)
        goto cleanup;


    lpszJavaArgs = getJavaArgs(lpszJavaArgs, &dwJLen, dwArgc, lpszArgv);
    lpszAppArgs = getAppArgs(lpszAppArgs, &dwALen, dwArgc, lpszArgv );
    wrkdir = getWorkingDirectory(dwArgc, lpszArgv);
    infoFilename = getInfoFilename(dwArgc, lpszArgv);
    JVMPathParam = getJVMPath(dwArgc, lpszArgv);
    processPriority = getPriority(dwArgc, lpszArgv);

    if (!ReportStatus(SERVICE_RUNNING,NO_ERROR,0)){
        goto cleanup;
    }

    // After the initialization is complete (we've checked for arguments) and
    // the service control manager has been told the service is running, invoke
    // the Java application. If clients are unable to access
    // the server, check the event log for messages that should indicate any errors
    // that may have occured while firing up Java...

    SetPriorityClass(GetCurrentProcess(), (strcmp(processPriority, "normal") == 0)? NORMAL_PRIORITY_CLASS : IDLE_PRIORITY_CLASS);

    _beginthread(invokeJVM, 0, NULL);


    // Wait for the stop event to be signalled.
    WaitForSingleObject(hServerStopEvent,INFINITE);

  cleanup:

    if (hServerStopEvent)
        CloseHandle(hServerStopEvent);
}
