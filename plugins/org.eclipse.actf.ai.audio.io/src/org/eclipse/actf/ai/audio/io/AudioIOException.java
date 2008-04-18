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
 * This represents an exception caused by the reason about this utilities.
 */
public class AudioIOException extends Exception{

    /**
     * @param string The message of the exception.
     */
    public AudioIOException(String string) {
        super(string);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
