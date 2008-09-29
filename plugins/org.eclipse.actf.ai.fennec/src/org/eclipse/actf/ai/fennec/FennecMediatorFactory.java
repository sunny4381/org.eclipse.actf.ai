/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.fennec;

import org.eclipse.actf.ai.fennec.mediator.FennecMediatorImpl;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;

/**
 * Factory to create a instance of IFennecMediator
 */
public class FennecMediatorFactory {

	/**
	 * This is the factory method to create a instance of IFennecMediator
	 * 
	 * @param webBrowser
	 *            An instance of {@link IWebBrowserACTF} to mediate.
	 * @return New instance of IFennecMediator.
	 */
	public static IFennecMediator newFennecMediator(IWebBrowserACTF webBrowser) {
		return new FennecMediatorImpl(webBrowser);
	}

}
