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

package org.eclipse.actf.ai.xmlstore;

import java.util.Iterator;

/**
 * IXMLStore interface defines the methods to be implemented by the
 * implementation of the storage of the XML.
 */
public interface IXMLStore {
	/**
	 * @param selector
	 *            the selector to be used for selecting XML files from the
	 *            storage.
	 * @return new IXMLStore contains specified files.
	 */
	IXMLStore specify(IXMLSelector selector);

	/**
	 * Reload all files which is contained in the storage.
	 */
	void refleshAll();

	/**
	 * @return the Iterator of the XML files.
	 */
	Iterator<IXMLInfo> getInfoIterator();
}
