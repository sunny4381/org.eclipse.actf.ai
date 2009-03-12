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
package org.eclipse.actf.ai.tts.sapi.engine;

import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

/*
 * COM Wrapper of ISpeechObjectTokens interface
 * 
 * @see "Microsoft Speech API ISpeechObjectTokens"
 */
public class SpeechObjectTokens {

	private OleAutomation automation;
	private int idCount;
	private int idItem;

	public SpeechObjectTokens(Variant varTokens) {
		automation = varTokens.getAutomation();
		idCount = getIDsOfNames("Count"); //$NON-NLS-1$
		idItem = getIDsOfNames("Item"); //$NON-NLS-1$
	}

	public static SpeechObjectTokens getTokens(Variant varTokens) {
		if (null == varTokens || OLE.VT_DISPATCH != varTokens.getType()) {
			return null;
		}
		return new SpeechObjectTokens(varTokens);
	}

	public int getCount() {
		try {
			return automation.getProperty(idCount).getInt();
		} catch (Exception e) {
		}
		return 0;
	}

	public Variant getItem(int index) {
		try {
			return automation.invoke(idItem,
					new Variant[] { new Variant(index) });
		} catch (Exception e) {
		}
		return null;
	}

	private int getIDsOfNames(String name) {
		int dispid[] = automation.getIDsOfNames(new String[] { name });
		if (null != dispid) {
			return dispid[0];
		}
		return 0;
	}
}
