/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.sapi.engine;

import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

/*
 * COM Wrapper of SpObjectToken interface
 * 
 * @see "Microsoft Speech API SpObjectToken"
 */
public class SpObjectToken {

	private OleAutomation automation;
	private int idGetDescription;
	private int idGetAttribute;

	public SpObjectToken(Variant varToken) {
		automation = varToken.getAutomation();
		idGetDescription = getIDsOfNames("GetDescription"); //$NON-NLS-1$
		idGetAttribute = getIDsOfNames("GetAttribute"); //$NON-NLS-1$
	}

	public static SpObjectToken getToken(Variant varToken) {
		if (null == varToken || OLE.VT_DISPATCH != varToken.getType()) {
			return null;
		}
		return new SpObjectToken(varToken);
	}

	public String getDescription(int locale) {
		try {
			return automation
					.invoke(idGetDescription,
							new Variant[] { new Variant(locale) }).getString()
					.trim();
		} catch (Exception e) {
		}
		return null;
	}

	public String getAttribute(String attr) {
		try {
			return automation.invoke(idGetAttribute,
					new Variant[] { new Variant(attr) }).getString();
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
