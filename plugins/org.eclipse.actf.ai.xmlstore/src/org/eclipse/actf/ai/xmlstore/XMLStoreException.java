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

package org.eclipse.actf.ai.xmlstore;

public class XMLStoreException extends Exception {
    private static final long serialVersionUID = -4630681290620194750L;
    
    public XMLStoreException(String message, Throwable t) {
        super(message, t);
    }

    public XMLStoreException(String message) {
        super(message);
    }
}
