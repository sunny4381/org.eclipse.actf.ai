/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.internal.xmlstore;

import java.io.File;

import org.eclipse.actf.ai.xmlstore.XMLStoreServiceUtil;
import org.eclipse.actf.ai.xmlstore.local.UserXMLStore;
import org.eclipse.actf.ai.xmlstore.local.XMLStoreLocal;
import org.eclipse.actf.ai.xmlstore.spi.XMLStoreServiceImpl;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class XMLStorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.actf.ai.xmlstore";

	// The shared instance
	private static XMLStorePlugin plugin;

	/**
	 * The constructor
	 */
	public XMLStorePlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static XMLStorePlugin getDefault() {
		return plugin;
	}

	/**
	 * @param subDirectory
	 *            the sub directory to be read.
	 * @return the instance of the File which is the sub directory of the
	 *         default user directory of the application.
	 */
	public File getLocalDir(String subDirectory) {
		return new File(getLocalDir(), subDirectory);
	}

	private File getLocalDir() {
		Bundle bundle = getBundle();
		IPath stateLocationPath = Platform.getStateLocation(bundle);
		return stateLocationPath.toFile();
	}

	private void initialize() {
		XMLStoreServiceImpl.getInstance().setUserStore(
				new UserXMLStore(getLocalDir(UserXMLStore.TEMP_DIR_NAME),
						XMLStoreServiceUtil.XML_EXT));
		XMLStoreServiceImpl.getInstance().addStore(
				new XMLStoreLocal(getLocalDir(XMLStoreLocal.SYSTEM_DIR_NAME),
						XMLStoreServiceUtil.XML_EXT));
	}
}
