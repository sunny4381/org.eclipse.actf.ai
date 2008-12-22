/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.sapi;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String tts_sapi_description;
	public static String tts_sapi_voicename;
	public static String tts_sapi_audiooutput;
	public static String tts_sapi_notAvailable;
	public static String tts_sapi_test;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}