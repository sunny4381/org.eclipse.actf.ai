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

package org.eclipse.actf.ai.xmlstore.nvdl.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The <code>DefaultErrorHandler</code> class is an ErrorHandler just
 * throwing any error or warning.
 */
public class DefaultErrorHandler implements ErrorHandler {
    static private DefaultErrorHandler defaultErrorHandlerInstance;
    static public ErrorHandler getErrorHandler() {
        if (defaultErrorHandlerInstance == null) {
            defaultErrorHandlerInstance = new DefaultErrorHandler();
        }
        return defaultErrorHandlerInstance;
    }
	
    public void warning(SAXParseException e) throws SAXException {
        throw e;
    }

    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }
}
