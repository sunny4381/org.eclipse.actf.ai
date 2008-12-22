/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.internal.audio.description;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String AudioDescription_view_time;
	public static String AudioDescription_view_desc;
	public static String AudioDescription_view_enable;
	public static String AudioDescription_view_notEnable;
	public static String AudioDescription_preference_title;
	public static String AudioDescription_voice_engine;
	public static String Metadata_available;
	public static String AudioDescription_on;
	public static String AudioDescription_off;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}