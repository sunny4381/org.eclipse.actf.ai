/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.fennec.treemanager;

/**
 * IAccessKeyList interface defines methods to be implemented by the collection
 * of the access keys in a document.
 */
public interface IAccessKeyList {
	/**
	 * @return the size of the collection.
	 */
	int size();

	/**
	 * @param index
	 *            the index of the access key.
	 * @return the code of the access key specified by the index.
	 */
	char getAccessKeyAt(int index);

	/**
	 * @param index
	 *            the index of the access key.
	 * @return the text information of the element on which the access key is
	 *         declared.
	 */
	String getUIStringAt(int index);
}
