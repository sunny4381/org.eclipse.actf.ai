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
 * This Fennec exception is thrown when a fennec processing is interrupted by
 * some reason. For example, metadata is updated during the processing.
 */
public class FennecInterruptedException extends FennecException {
	private static final long serialVersionUID = 503781748413609768L;

	/**
	 * @param message
	 * @param cause
	 * @see FennecException#FennecException(String, Throwable)
	 */
	public FennecInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @see FennecException#FennecException(String, Throwable)
	 */
	public FennecInterruptedException(String message) {
		super(message, null);
	}
}
