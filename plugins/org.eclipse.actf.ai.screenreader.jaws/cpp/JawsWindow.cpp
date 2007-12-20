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
#include <tchar.h>
#include <windows.h>
#include "common.h"

static const TCHAR Jaws_window_class_name[] = _T("Jaws-aiBrowser-Communication");
static HWND jaws_window;
static HWND top;
static HANDLE jaws_window_event_handle;
static HANDLE jaws_window_thread;
static UINT wm_jaws_aibrowser_msgid;

static WNDPROC base_edit_proc;

static int jaws_window_show_state;

#define WM_JW_SETUP (WM_USER + 100)
#define WM_JW_RESET (WM_USER + 101)

static int
adjust_jaws_window(HWND hwnd, int x, int y, int width, int height)
{
    if (!IsWindowVisible(hwnd)) {
	ShowWindow(hwnd, SW_SHOW);
	// -1 means the space for thickframe.
	SetWindowPos(hwnd, HWND_TOP, 0, y, width - 1, height - 1, 0);
    }
    return 1;
}

static LRESULT CALLBACK 
jaws_window_wndproc(HWND hWnd, UINT cmd, WPARAM wParam, LPARAM lParam)
{
    if (cmd == wm_jaws_aibrowser_msgid) {
	//fprintf(stderr, "JAWS sent message, %d!!!\n", wParam);
	//fflush(stderr);
	callback((int) wParam);
	return 1;
    } else if (cmd == WM_JW_SETUP) {
	RECT baserect;
	if (!GetClientRect(top, &baserect)) return 0;
	int width = baserect.right - baserect.left;
	//int y = baserect.bottom - baserect.top - 200;
	int y = 0;
	int height = baserect.bottom - baserect.top;
	return adjust_jaws_window(hWnd, 0, y, width, height);
    } else if (cmd == WM_JW_RESET) {
	if (IsWindowVisible(hWnd)) {
	    ShowWindow(hWnd, SW_HIDE);
	    return 1;
	}
	return 0;
    }
    return CallWindowProc(base_edit_proc, hWnd, cmd, wParam, lParam);
}

static DWORD WINAPI
init_jaws_window_internal(LPVOID topwv)
{
    attachThread();
    top = (HWND) topwv;
    WNDCLASS wc;
    // HINSTANCE hInst = (HINSTANCE) GetModuleHandle(0);
    HINSTANCE hInst = NULL;

#if 1
    GetClassInfo(NULL, "Edit", &wc);

    base_edit_proc = wc.lpfnWndProc;
    wc.lpfnWndProc = jaws_window_wndproc;
    wc.lpszClassName = Jaws_window_class_name;
    wc.hInstance = hInst;
    // wc.style &= (~CS_NOCLOSE);
    // wc.style = 0;
#else
    wc.style = CS_BYTEALIGNCLIENT;
    wc.lpfnWndProc = DefWindowProc;
    wc.cbClsExtra = 0;
    wc.cbWndExtra = 0;
    wc.hInstance = hInst;
    wc.hIcon = (HICON) NULL;
    wc.hCursor = (HCURSOR) NULL;
    wc.hbrBackground = (HBRUSH)(COLOR_BACKGROUND + 1);
    wc.lpszMenuName = NULL;
    wc.lpszClassName = Jaws_window_class_name;
#endif

    RegisterClass(&wc);

    jaws_window = CreateWindowEx(
	//WS_EX_TOPMOST | WS_EX_PALETTEWINDOW | WS_EX_OVERLAPPEDWINDOW,
	0,
	Jaws_window_class_name,
	NULL,
	ES_LEFT | ES_MULTILINE | ES_NOHIDESEL | ES_AUTOVSCROLL
	| WS_CHILDWINDOW | WS_THICKFRAME,
//	WS_CHILD,
	0, 0, 1, 1,
	top, NULL, hInst, NULL);
    SetEvent(jaws_window_event_handle);
    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0)) {
	TranslateMessage(&msg);
	DispatchMessage(&msg);
    }
    return 0;
}

void
init_jaws_window(HWND top)
{
    wm_jaws_aibrowser_msgid = RegisterWindowMessage(_T("WM_JAWS_AIBROWSER_MESSAGE"));
    jaws_window_event_handle = CreateEvent(NULL, TRUE, FALSE, NULL);
    jaws_window_thread = CreateThread(NULL, 1024, init_jaws_window_internal, top, 0, NULL);
}

void
set_jaws_window_text(LPTSTR text)
{
    while (!jaws_window) {
	Sleep(1);
    }
    HWND hwnd = jaws_window;
#if 1
    SendMessage(hwnd, WM_JW_SETUP, 0, 0);
    int len = lstrlen(text);
    LPTSTR newtext = (LPTSTR) malloc(len * sizeof(TCHAR) + 20);
    lstrcpy(newtext, "\r\n ");
    lstrcat(newtext, text);
    lstrcat(newtext, " \r\n\r\n");
    SendMessage(hwnd, WM_SETTEXT, 0, (LPARAM) newtext);
    SendMessage(hwnd, EM_SETSEL, 2, 2);
    free(newtext);
#else
    SetWindowText(hwnd, text);
#endif
}

void
reset_jaws_window()
{
    while (!jaws_window) {
	Sleep(1);
    }
    if (SendMessage(jaws_window, WM_JW_RESET, 0, 0)) {
	SetFocus(top);
    }
}

