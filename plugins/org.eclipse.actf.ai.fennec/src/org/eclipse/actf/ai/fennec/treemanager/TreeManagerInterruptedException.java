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
package org.eclipse.actf.ai.fennec.treemanager;

/**
 * TreeManagerInterruptedException is thrown when the tree operation is
 * interrupted by some reason.
 */
public class TreeManagerInterruptedException extends TreeManagerException {
	private static final long serialVersionUID = -1029176053941063206L;

	/**
	 * @param status
	 * @param message
	 * @param cause
	 * @see TreeManagerException#TreeManagerException(int, String, Throwable)
	 */
	public TreeManagerInterruptedException(int status, String message,
			Throwable cause) {
		super(status, message, cause);
	}

	/**
	 * @param status
	 * @param message
	 * @see TreeManagerException#TreeManagerException(int, String)
	 */
	public TreeManagerInterruptedException(int status, String message) {
		super(status, message);
	}
}
