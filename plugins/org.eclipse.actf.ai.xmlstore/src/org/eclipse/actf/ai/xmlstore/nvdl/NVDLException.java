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

package org.eclipse.actf.ai.xmlstore.nvdl;

/**
 * The <code>NVDLException</code> is the base exception class for NVDL.
 */
public class NVDLException extends Exception {
    private static final long serialVersionUID = 155415531522970901L;

    private Exception e;

    private Object[] messageArguments;

    public Exception getException() {
        return e;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    public NVDLException(String message, Object[] messageArguments) {
        super(message);
        this.messageArguments = messageArguments;
    }

    public NVDLException(String message) {
        this(message, null);
    }

    public NVDLException(Exception e) {
        super(e);
        this.e = e;
    }
}
