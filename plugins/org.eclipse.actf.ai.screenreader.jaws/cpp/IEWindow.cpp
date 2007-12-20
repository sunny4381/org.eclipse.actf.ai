/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/
#include <atlbase.h>
#include <atlconv.h>
#include <shlguid.h>
#include <tchar.h>
#include <windows.h>
#include "org_eclipse_actf_ai_screenreader_jaws_JawsAPI.h"

WNDPROC original_winproc;

static const TCHAR IE_window_class_name[] = _T("Internet Explorer_Server");
static UINT wm_html_getobject_msgid;

static HWND
getBrowserHWND(IWebBrowser2* iwebUnk)
{
    IOleWindow* iOleWindow;
    HWND hwnd;
    HRESULT hr = iwebUnk->get_HWND((LONG_PTR*) &hwnd);
    if (SUCCEEDED(hr)) return hwnd;
    hr = iwebUnk->QueryInterface(IID_IOleWindow, (void **)&iOleWindow);
    if (FAILED(hr)) return NULL;
    hr = iOleWindow->GetWindow(&hwnd);
    iOleWindow->Release();
    if (FAILED(hr)) return NULL;
    return hwnd;
}

static void walkThroughWindow(HWND top);
static void overrideWindowProc(HWND hwnd);
static LRESULT CALLBACK dummy_winproc(HWND hWnd, UINT cmd, WPARAM wParam, LPARAM lParam);

static void checkIEWindow(HWND hwnd)
{
    TCHAR className[sizeof(IE_window_class_name) + 1 + 1];
    if (GetClassName(hwnd, className, sizeof(className))) {
#if 0
	fprintf(stderr, "HWND:%x, %s\n", hwnd, className);
	fflush(stderr);
#endif
	if (_tcscmp(IE_window_class_name, className) == 0) {
	    overrideWindowProc(hwnd);
	}
    }
    walkThroughWindow(hwnd);
}

static void overrideWindowProc(HWND ie_hWnd)
{
    if (!ie_hWnd) return;
  
    LONG_PTR ptr = GetWindowLongPtr(ie_hWnd, GWLP_WNDPROC);
#if 0
    printf("BEFORE : %x\n", ptr);
#endif
  
    if (ptr != (LONG_PTR) dummy_winproc) {
	if ((LONG_PTR) original_winproc != ptr) {
	    original_winproc = (WNDPROC) ptr;
	    WNDCLASS wc;
	    wc.lpfnWndProc = dummy_winproc;
	    wc.lpszClassName = _T("Eclipse ACTF Accessibility Internet Explorer_Server");
	    RegisterClass(&wc);
	}
	SetWindowLongPtr(ie_hWnd, GWLP_WNDPROC, (LONG_PTR) dummy_winproc);
    }
  
    ptr = GetWindowLongPtr(ie_hWnd, GWLP_WNDPROC);
#if 0
    printf("AFTER : %x\n", ptr);
    fflush(stdout);
#endif
}

static void
walkThroughWindow(HWND top)
{
    HWND nextChild = GetWindow(top, GW_CHILD);
    while (nextChild) {
	checkIEWindow(nextChild);
	nextChild = GetWindow(nextChild, GW_HWNDNEXT);
    }
}


static LRESULT CALLBACK 
dummy_winproc(HWND hWnd, UINT cmd, WPARAM wParam, LPARAM lParam)
{
    if (cmd == wm_html_getobject_msgid) {
	// fprintf(stderr, "JAWS tries to bite!!\n");
	// fflush(stderr);
	return 0;
    } else if (cmd == WM_GETOBJECT) {
	return 0;
    } else if (original_winproc) {
	return CallWindowProc(original_winproc,
			      hWnd, cmd, wParam,
			      lParam);
    }
    return 0;
}

/*
 * Class:     org_eclipse_actf_ai_screenreader_jaws_JawsAPI
 * Method:    _TakeBackControl
 * Signature: (J)Z
 */
jboolean JNICALL
Java_org_eclipse_actf_ai_screenreader_jaws_JawsAPI__1TakeBackControl
(JNIEnv *env, jclass clazz, jlong browser)
{
    wm_html_getobject_msgid = RegisterWindowMessage(_T("WM_HTML_GETOBJECT"));
#if 0
    HWND ie_hWnd = getBrowserHWND((IWebBrowser2 *) browser);
    overrideWindowProc(HWND ie_hWnd);
#else
    walkThroughWindow((HWND) browser);
#endif
    return JNI_TRUE;
}

