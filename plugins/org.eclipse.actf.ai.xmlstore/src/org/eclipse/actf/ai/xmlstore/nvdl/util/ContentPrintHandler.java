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
import java.text.MessageFormat;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The <code>ContentPrintHandler</code> is a SAX content handler that dumps
 * all SAX events.
 */
public class ContentPrintHandler extends DefaultHandler {
    public final String name;

    private final PrintWriter out;
    private Locator locator;
    private int number;

    private void output(String mes) {
        out.println(mes);
        out.flush();
    }

    private String formatLocator() {
        number++;
        if (locator == null) {
            return "Locator:null";
        } else {
            String r;
            String systemId = locator.getSystemId();
            if (systemId != null) {
                r = MessageFormat.format("{0}|{1,number,integer}/{2}, Line:{3,number,integer}, Col:{4,number,integer}", 
                                         new Object[] {name,
                                                       new Integer(number),
                                                       locator.getSystemId(),
                                                       new Integer(locator.getLineNumber()),
                                                       new Integer(locator.getColumnNumber())});
            } else {
                r = MessageFormat.format("{0}|{1,number,integer}/Line:{2,number,integer}, Col:{3,number,integer}", 
                                         new Object[] {name,
                                                       new Integer(number),
                                                       new Integer(locator.getLineNumber()),
                                                       new Integer(locator.getColumnNumber())});
            }
            return r;
        }
    }

    private String formatAttribute(Attributes attrs, int i) {
        String uri = attrs.getURI(i);
        if (uri.length() > 0) {
            return MessageFormat.format("{0}/'{'{1}'}'{2}({3})={4},{5}",
                                        new Object[] {new Integer(i),
                                                      uri,
                                                      attrs.getLocalName(i),
                                                      attrs.getQName(i),
                                                      attrs.getValue(i),
                                                      attrs.getType(i)});
        } else {
            return MessageFormat.format("{0}/{1}({2})={3},{4}",
                                        new Object[] {new Integer(i),
                                                      attrs.getLocalName(i),
                                                      attrs.getQName(i),
                                                      attrs.getValue(i),
                                                      attrs.getType(i)});
        }
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        output(formatLocator() + ": setDocumentLocator");
    }

    public void startDocument() throws SAXException {
        output(formatLocator() + ": startDocument");
    }

    public void endDocument() throws SAXException {
        output(formatLocator() + ": endDocument");
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        output(formatLocator() + ": startPrefixMapping {" + prefix + "=" + uri + "}");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        output(formatLocator() + ": endPrefixMapping {" + prefix + "}");
    }

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attrs) throws SAXException {
        output(formatLocator() + ": startElement {" + uri + "}" + localName + "(" + qName + ") ");
        if (attrs.getLength() > 0) {
            output("  Attributes:");
            for(int i = 0; i < attrs.getLength(); i++){
                output("    " + formatAttribute(attrs, i));
            }
        }
    }
    

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        output(formatLocator() + ": endElement {" + uri + "}" + localName + "(" + qName + ") ");
    }
    
    public void characters(char ch[], int start, int length) throws SAXException {
        output(formatLocator() + ": characters [" + new String(ch, start, length) + "]");
    }
    
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        output(formatLocator() + ": ignorableWhitespace [" + new String(ch, start, length) + "]");
    }
    
    public void processingInstruction(String target, String data) throws SAXException {
        output(formatLocator() + ": processingInstruction [" + target + "=" + data + "]");
    }

    public void skippedEntity(String name) throws SAXException {
        output(formatLocator() + ": skippedEntity [" + name + "]");
    }

    public ContentPrintHandler(String name) {
        this(name, System.out);
    }

    public ContentPrintHandler(String name, OutputStream os) {
        this(name, new PrintWriter(os));
    }

    public ContentPrintHandler(String name, PrintWriter out) {
        this.name = name;
        this.out = out;
        this.number = 0;
    }
}
