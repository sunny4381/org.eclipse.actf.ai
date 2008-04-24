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

package org.eclipse.actf.ai.fennec;

import org.eclipse.actf.ai.fennec.mediator.FennecMediatorImpl;
import org.eclipse.actf.model.IWebBrowserACTF;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FennecPlugin extends Plugin {

	// The shared instance.
	private static FennecPlugin plugin;

	/**
	 * The constructor.
	 */
	public FennecPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static FennecPlugin getDefault() {
		return plugin;
	}

	/**
	 * This is the factory method to create a instance of IFennecMediator
	 * 
	 * @param webBrowser
	 *            An instance of {@link IWebBrowserACTF} to mediate.
	 * @return New instance of IFennecMediator.
	 */
	public IFennecMediator newFennecMediator(IWebBrowserACTF webBrowser) {
		return new FennecMediatorImpl(webBrowser);
	}

}
