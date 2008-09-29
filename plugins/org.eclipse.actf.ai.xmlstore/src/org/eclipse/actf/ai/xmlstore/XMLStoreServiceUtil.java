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
package org.eclipse.actf.ai.xmlstore;

import java.io.File;

import org.eclipse.actf.ai.xmlstore.local.XMLStoreLocal;
import org.eclipse.actf.ai.xmlstore.spi.XMLStoreServiceImpl;

/**
 * Utility class for IXMLStoreService
 */
public class XMLStoreServiceUtil {

	/**
	 * The extensions for the XML files used for addSystemStore method.
	 */
	public static final String[] XML_EXT = new String[] { ".xml", ".fnc" };

	/**
	 * The settings of the application should be contained in the system store.
	 * 
	 * @param location
	 *            the location to be added for searching XML files.
	 */
	public static void addSystemStore(File location) {
		XMLStoreServiceImpl.getInstance().addStore(
				new XMLStoreLocal(location, XMLStoreServiceUtil.XML_EXT));
	}

	/**
	 * @return the singleton instance of the IXMLStoreService.
	 */
	public static IXMLStoreService getXMLStoreService() {
		return XMLStoreServiceImpl.getInstance();
	}


}
