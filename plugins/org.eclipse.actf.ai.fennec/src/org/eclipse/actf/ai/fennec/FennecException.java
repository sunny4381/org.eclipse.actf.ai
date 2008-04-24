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

/**
 * FennecException is the super class of the exception on processing Fennec
 * metadata. The message text provide a further description of the problem.
 */
public class FennecException extends Exception {
	private static final long serialVersionUID = -4473389024467270039L;

	/**
	 * @param message
	 * @param cause
	 * @see Exception#Exception(String, Throwable)
	 */
	public FennecException(String message, Throwable cause) {
		super(message, cause);
	}
}
