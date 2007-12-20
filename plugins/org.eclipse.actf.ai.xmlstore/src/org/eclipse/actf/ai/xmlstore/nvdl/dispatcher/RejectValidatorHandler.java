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

package org.eclipse.actf.ai.xmlstore.nvdl.dispatcher;

import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class RejectValidatorHandler extends ValidatorHandler {
    private ErrorHandler errorHandler = null;
    public void setErrorHandler(ErrorHandler errorHandler) {
	this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
	return errorHandler;
    }

    private Locator loc;

    private void error(String disallowedThing) throws SAXException {
	if (errorHandler != null) {
	    errorHandler.error(new SAXParseException(disallowedThing + " is not allowed here.", loc));
	}
    }

    public void setDocumentLocator(Locator loc) {
	this.loc = loc;
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }
    public void startPrefixMapping(String arg0, String arg1) throws SAXException {
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    public void startElement(String url,
			     String localName,
			     String qName,
			     Attributes attrs) throws SAXException {
	error(qName);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
	error(qName);
    }

    public void characters(char[] ch, int start, int length)
	throws SAXException {
	error(new String(ch, start, length));
    }

    public void ignorableWhitespace(char[] ch,
				    int start,
				    int length) throws SAXException {
	error(new String(ch, start, length));
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    public void setContentHandler(ContentHandler arg0) {
    }

    public ContentHandler getContentHandler() {
	return null;
    }

    public void setResourceResolver(LSResourceResolver arg0) {
    }

    public LSResourceResolver getResourceResolver() {
	return null;
    }

    public TypeInfoProvider getTypeInfoProvider() {
	return null;
    }
        
}
