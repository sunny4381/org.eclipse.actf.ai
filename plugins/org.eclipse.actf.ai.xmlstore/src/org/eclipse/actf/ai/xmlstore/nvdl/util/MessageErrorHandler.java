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

import java.io.OutputStream;
import java.io.PrintWriter;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The <code>MessageErrorHandler</code> class is an ErrorHandler printing
 * fancy error messages.
 */
public class MessageErrorHandler implements ErrorHandler {
    private final PrintWriter out;

    private void output(String mes) {
        out.println(mes);
        out.flush();
    }

    public void warning(SAXParseException e)
        throws SAXException {
        output(MessageFormatter.exception(e));
    }

    public void error(SAXParseException e)
        throws SAXException {
        output(MessageFormatter.exception(e));
    }

    public void fatalError(SAXParseException e)
        throws SAXException {
        output(MessageFormatter.exception(e));
        throw e;
    }

    public MessageErrorHandler() {
        this(System.err);
    }

    public MessageErrorHandler(OutputStream os) {
        this(new PrintWriter(os));
    }

    public MessageErrorHandler(PrintWriter out) {
        this.out = out;
    }
}
