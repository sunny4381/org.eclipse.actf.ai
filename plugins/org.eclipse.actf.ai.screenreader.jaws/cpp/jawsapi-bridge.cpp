/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *    Daisuke SATO
 *    Yevgen Borodin - [Bug 226468]
 *******************************************************************************/
//#define UNICODE
#include <stdlib.h>
#include <malloc.h>
#include <tchar.h>
#include <windows.h>
//#include "stdafx.h"
#include "common.h"
#include "org_eclipse_actf_ai_screenreader_jaws_JawsAPI.h"
#include "org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil.h"

typedef BOOL (WINAPI *JFWRunFunctionType)(LPCTSTR lpszFuncName);
typedef BOOL (WINAPI *JFWSayStringType)(LPCTSTR lpszStringToSpeak,BOOL bInterrupt);
typedef BOOL (WINAPI *JFWStopSpeechType)(void);
typedef BOOL (WINAPI *JFWRunScriptType)(LPCTSTR lpszScriptName);

static JFWRunFunctionType JFWRunFunctionProc;
static JFWSayStringType JFWSayStringProc;
static JFWStopSpeechType JFWStopSpeechProc;
static JFWRunScriptType JFWRunScriptProc;

static const TCHAR jaws_registry_path[] = "SOFTWARE\\Freedom Scientific\\JAWS";
static const TCHAR jaws_api_dllname[] = "jfwapi.dll";

static JavaVM *jvm;
static jclass class_JawsWindowUtil;
static jmethodID callBackMethodID;

static LPTSTR
findJFWAPIDLL()
{
    DWORD size;
    HKEY hjawsKey, hjawsVerKey;
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, jaws_registry_path,
		     0, KEY_READ, &hjawsKey) != ERROR_SUCCESS) {
	return NULL;
    }
    TCHAR buf[MAX_PATH];
    TCHAR verKeyPath[MAX_PATH];
    verKeyPath[0] = '\0';
    int i;
    size = sizeof(buf) / sizeof(TCHAR);
    for (i = 0; ; i++) {
	if (RegEnumKeyEx(hjawsKey, i, buf, &size,
			 NULL, NULL, NULL, NULL) != ERROR_SUCCESS) break;
	//fprintf(stderr, "%s\n", buf);
	if (_tcscmp(buf, verKeyPath) > 0) {
	    _tcscpy(verKeyPath, buf);
	}
    }
    LPTSTR verKeyFullPath = (LPTSTR) alloca(sizeof(TCHAR) * (size + 2) + sizeof(jaws_registry_path));
    _tcscpy(verKeyFullPath, jaws_registry_path);
    _tcscat(verKeyFullPath, "\\");
    _tcscat(verKeyFullPath, verKeyPath);
    //fprintf(stderr, "%s\n", verKeyFullPath);
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, verKeyFullPath,
		     0, KEY_READ, &hjawsVerKey) != ERROR_SUCCESS) {
	goto error;
    }
    DWORD dwType;
    if (RegQueryValueEx(hjawsVerKey, "Target", NULL, &dwType, NULL, &size) != ERROR_SUCCESS) {
	goto error2;
    }
    if (dwType != REG_SZ) {
	goto error2;
    }
    LPTSTR ret = (LPTSTR) malloc((size + 1 + 1) * sizeof(TCHAR) + sizeof(jaws_api_dllname));
    if (RegQueryValueEx(hjawsVerKey, "Target", NULL, &dwType, (LPBYTE) ret, &size) != ERROR_SUCCESS) {
	free(ret);
	goto error2;
    }
    int len = _tcslen(ret);
    if (ret[len - 1] != '\\') {
	_tcscat(ret, "\\");
    }
    _tcscat(ret, jaws_api_dllname);
    // fprintf(stderr, "%s\n", ret);
    RegCloseKey(hjawsVerKey);
    RegCloseKey(hjawsKey);
    return ret;

error2:
    RegCloseKey(hjawsVerKey);
error:
    RegCloseKey(hjawsKey);
    return NULL;
}

static LPTSTR
convertJavaString(JNIEnv* env, jstring jstr)
{
    int len = env->GetStringLength(jstr);
    int bufsize = len * 2 + 10;
    LPCWSTR wstr = (LPWSTR) env->GetStringChars(jstr, NULL);
    LPTSTR tstr = (LPTSTR) malloc(bufsize);
    int idx = WideCharToMultiByte(CP_ACP, 0, wstr, len, tstr, bufsize, NULL, NULL);
    tstr[idx] = '\0';
    env->ReleaseStringChars(jstr, (jchar*) wstr);
#if 0
    {
	int i;
	printf("%s\n", tstr);
	for (i = 0; i < idx; i++) {
	    int c = ((unsigned char*) tstr)[i];
	    printf("%X ", c);
	}
	printf("\n", tstr[i]);
    }
#endif
    return tstr;
}


/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil
 * Method:    _initializeWindow
 * Signature: ()Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil__1initializeWindow
(JNIEnv* env, jclass jcls, jint topwnd)
{
    LPTSTR jfwapidllpath = findJFWAPIDLL();
    if (!jfwapidllpath) {
	fprintf(stderr, "Could not find out jfwapi.dll\n");
	return JNI_FALSE;
    }
    
    env->GetJavaVM(&jvm);
    class_JawsWindowUtil = (jclass) env->NewGlobalRef(jcls);
    callBackMethodID = env->GetStaticMethodID(class_JawsWindowUtil, "callBack", "(I)Z");

    init_jaws_window((HWND) topwnd);
    
    return JNI_TRUE;
}


/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsAPI
 * Method:    _initialize
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1initialize
(JNIEnv* env, jclass jcls)
{
    HMODULE h;
    LPTSTR jfwapidllpath = findJFWAPIDLL();
    if (!jfwapidllpath) {
	fprintf(stderr, "Could not find out jfwapi.dll\n");
	return JNI_FALSE;
	
    }
    h = LoadLibrary(jfwapidllpath);
    free(jfwapidllpath);
	
    if (!h) {
        fprintf(stderr, "Can't load the \"jfwapi.dll\"\n");
	return JNI_FALSE;
    }
    


    JFWRunFunctionProc = (JFWRunFunctionType) GetProcAddress(h, "JFWRunFunction");
    if (!JFWRunFunctionProc) {
	fprintf(stderr, "Could not obtain JFWRunFunction entry pointer.\n");
	//return JNI_FALSE;
    }
    JFWSayStringProc = (JFWSayStringType) GetProcAddress(h, "JFWSayString");
    if (!JFWSayStringProc) {
	fprintf(stderr, "Could not obtain JFWSayString entry pointer.\n");
	//return JNI_FALSE;
    }
    JFWStopSpeechProc = (JFWStopSpeechType) GetProcAddress(h, "JFWStopSpeech");
    if (!JFWStopSpeechProc) {
	fprintf(stderr, "Could not obtain JFWStopSpeech entry pointer.\n");
	//return JNI_FALSE;
    }
    JFWRunScriptProc = (JFWRunScriptType)  GetProcAddress(h, "JFWRunScript");
    if (!JFWRunScriptProc) {
	fprintf(stderr, "Could not obtain JFWRunScript entry pointer.\n");
	//return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsAPI
 * Method:    _isAvailable
 * Signature: ()Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1isAvailable
(JNIEnv *, jclass)
{
    //HWND hwnd = FindWindow("JFWUI2", NULL);
    HWND hwnd = FindWindow(NULL, "JAWS");
    if (hwnd == NULL) return JNI_FALSE;
    return JNI_TRUE;
}

/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsAPI
 * Method:    _JawsRunFunction
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1JawsRunFunction
  (JNIEnv *env, jclass jcls, jstring jstringFuncName)
{
  if (!JFWRunFunctionProc) 
    return JNI_FALSE;
  LPTSTR str = convertJavaString(env, jstringFuncName);
  jboolean b = (JFWRunFunctionProc)(str);
  free(str);
  return b;
}

/*
 * Class:     Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI_JawsRunFunction
 * Method:    JawsSayString
 * Signature: (Ljava/lang/String;Z)Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1JawsSayString
(JNIEnv *env, jclass jcls, jstring jstringToSpeak, jboolean bInterrupt)
{
    if (!JFWSayStringProc)
      return JNI_FALSE;
    LPTSTR str = convertJavaString(env, jstringToSpeak);
    jboolean b = (JFWSayStringProc)(str, bInterrupt);
    free(str);
    return b;
}


/*
 * Class:     Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI_JawsRunFunction
 * Method:    JawsStopSpeech
 * Signature: ()Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1JawsStopSpeech
(JNIEnv *env, jclass jcls)
{
    if (!JFWStopSpeechProc)
      return JNI_FALSE;
    return (JFWStopSpeechProc)();
}

/*
 * Class:     Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI_JawsRunFunction
 * Method:    JawsRunScript
 * Signature: (Ljava/lang/String;)Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1JawsRunScript
(JNIEnv *env, jclass jcls, jstring jscriptName)
{
    if (!JFWRunScriptProc)
      return JNI_FALSE;
    LPTSTR str = convertJavaString(env, jscriptName);
    jboolean b = (JFWRunScriptProc)(str);
    free(str);

    return b;
}

/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil
 * Method:    _setJawsWindowText
 * Signature: (Ljava/lang/String;)Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil__1setJawsWindowText
(JNIEnv *env, jclass jcls, jstring text)
{
    LPTSTR str = convertJavaString(env, text);
    set_jaws_window_text(str);
    free(str);
    return JNI_TRUE;
}

/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil
 * Method:    _resetJawsWindowText
 * Signature: ()Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsWindowUtil__1resetJawsWindowText
(JNIEnv *env, jclass jcls)
{
    reset_jaws_window();
    return JNI_TRUE;
}

void attachThread()
{
    JNIEnv *env;
    jvm->AttachCurrentThread((void**) &env, NULL);
}

int callback(int param)
{
    JNIEnv *env;
    if (jvm->GetEnv((void**) &env, JNI_VERSION_1_2) != JNI_OK) return 0;
    return env->CallStaticBooleanMethod(class_JawsWindowUtil,
					callBackMethodID,
					(jint) param);
}
