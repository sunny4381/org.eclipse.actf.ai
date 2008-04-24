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

import org.eclipse.actf.ai.fennec.impl.FennecServiceImpl;
import org.eclipse.actf.model.dom.dombycom.IDocumentEx;

/**
 * This is the factory class of the IFennecService. This class has methods to
 * create a IFennecService instance from a {@link IFennecEntry} and a
 * {@link IDocumentEx}.
 */
public class FennecServiceFactory {
	/**
	 * @param entry
	 *            the instance of entry of Fennec metadata.
	 * @param doc
	 *            the instance of IDocumentEx which will be applied the Fennec
	 *            metadata.
	 * @return a new {@link IFennecService} instance.
	 * @throws FennecException
	 *             it will be thrown when the specified entry can't be loaded.
	 */
	public static IFennecService newFennecService(IFennecEntry entry,
			IDocumentEx doc) throws FennecException {
		return new FennecServiceImpl(entry, doc);
	}

	/**
	 * This creates a instance of {@link IFennecService} with default Fennec
	 * meatadata. Default metadata has an attach element for the root element of
	 * the specified document.
	 * 
	 * @param doc
	 *            the instance of IDocumentEx which will be applied the Fennec
	 *            metadata.
	 * @return a new {@link IFennecService} instance.
	 */
	public static IFennecService newFennecServiceWithDefaultMetadata(
			IDocumentEx doc) {
		return new FennecServiceImpl(doc);
	}
}
