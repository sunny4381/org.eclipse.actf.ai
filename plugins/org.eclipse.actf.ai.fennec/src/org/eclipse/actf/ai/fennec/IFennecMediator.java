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

import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;

/**
 * IFennecMediator interface defines the methods implemented by the mediator
 * class between the web browser and the Fennec service.
 */
public interface IFennecMediator {
	/**
	 * This method creates an instance of ITreeManager by using the metadata
	 * entry and Fennec service created from the document obtained from the web
	 * browser and the metadata entry.
	 * 
	 * @param entry
	 *            the instance of the Fennec metadata entry.
	 * @return the new instance of ITreeManager.
	 */
	ITreeManager newTreeManager(IFennecEntry entry);

	/**
	 * It returns the default Fennec metadta for the document opened in the web
	 * browser. If there is no metadata for the doument, it returns null.
	 * 
	 * @return the Fennec metadata entry.
	 */
	IFennecEntry getDefaultFennecEntry();

	/**
	 * It returns all Fennec metadata for the document opened in the web
	 * browser.
	 * 
	 * @return An array of the Fennec metadata entries.
	 */
	IFennecEntry[] getFennecEntries();

	/**
	 * If the mediator is not needed anymore, this method should be called.
	 */
	void release();
}
