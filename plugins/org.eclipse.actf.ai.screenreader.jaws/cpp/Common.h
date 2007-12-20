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
#ifndef __COMMON_H__
#define __COMMON_H__
extern void init_jaws_window(HWND top);
extern void set_jaws_window_text(LPTSTR text);
extern void reset_jaws_window();
extern void attachThread();
extern int callback(int param);
#endif /* not __COMMON_H__ */
