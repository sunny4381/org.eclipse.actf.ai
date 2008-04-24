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
 * TreeManagerException is thrown when the operation in ITreeManager is failed.
 */
public class TreeManagerException extends Exception {
	private static final long serialVersionUID = -715217633494025285L;

	private final int status;

	/**
	 * @return the status code of the exception.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status code of the exception.
	 * @param message
	 * @param cause
	 * @see Exception#Exception(String, Throwable)
	 */
	public TreeManagerException(int status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * @param status
	 * @param message
	 * @see Exception#Exception(String)
	 */
	public TreeManagerException(int status, String message) {
		super(message);
		this.status = status;
	}
}
