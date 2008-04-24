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

package org.eclipse.actf.ai.audio.io;

/**
 * AudioIOException is thrown when some exception occurs in this utilities.
 */
public class AudioIOException extends Exception {

	/**
	 * @param string
	 *            the further message of the exception.
	 * @param cause
	 * 			  the reason of the exception.
	 */
	public AudioIOException(String string, Throwable cause) {
		super(string, cause);
	}

	private static final long serialVersionUID = 1L;

}
