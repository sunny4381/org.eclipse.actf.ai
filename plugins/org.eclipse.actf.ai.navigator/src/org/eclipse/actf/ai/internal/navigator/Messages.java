/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.internal.navigator;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String NavigatorTreeView_property;
	public static String NavigatorTreeView_value;
	public static String DialogOpenURL_Open_URL_2;
	public static String IManipulator_TreeNavigaion;
	public static String IManipulator_FormInput;
	public static String IManipulator_Input;
	public static String FormInputDialog_Textarea;
	public static String FormInputDialog_Password;
	public static String FormInputDialog_Text;
	public static String FormInputDialog_Search;
	public static String FormInputDialog_Forward;
	public static String FormInputDialog_Backward;
	public static String FormInputDialog_Exact;
	public static String FormSelectDialog_Single;
	public static String FormSelectDialog_Multiple;
	public static String AccessKeyListDialog_Title;
	public static String AltInputDialog_Text;
	public static String UserInfo_PREFERENCES_NAME;
	public static String UserInfo_SAVE_ANNOTATION;
	public static String UserInfo_REFRESH_TREEVIEW;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}