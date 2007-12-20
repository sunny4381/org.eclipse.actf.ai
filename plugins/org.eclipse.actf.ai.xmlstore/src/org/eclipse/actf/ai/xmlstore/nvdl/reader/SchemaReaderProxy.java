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

package org.eclipse.actf.ai.xmlstore.nvdl.reader;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;


/**
 * The <code>SchemaReaderProxy</code> works as a proxy to
 * delegate SAX events to SchemaFactory's source.
 */
class SchemaReaderProxy
    extends Thread implements XMLReader {

    /**************************************************
                    XMLReader Proxy
    **************************************************/

    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }
    public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    ContentHandler h;
    synchronized public void setContentHandler(ContentHandler h) {
        this.h = h;
        if (schemaRewriter != null) {
            schemaRewriter.setup(h, prefixMapper);
        }
    }

    public ContentHandler getContentHandler() {
        return h;
    }

    ErrorHandler schemaReaderProxyErrorHandler = null;
    public void setErrorHandler(ErrorHandler eh) {
        if (eh == null) throw new NullPointerException();
        this.schemaReaderProxyErrorHandler = eh;
    }

    public ErrorHandler getErrorHandler() {
        return schemaReaderProxyErrorHandler;
    }

    public void parse(InputSource input)
        throws IOException, SAXException {
        startParse();
    }

    public void parse(String systemId)
        throws IOException, SAXException {
        startParse();            
    }

    /**************************************************
                            Proxy Handler
    **************************************************/
    static private final int CMD_SETDOCUMENTLOCATOR = 1;
    static private final int CMD_STARTELEMENT = 2;
    static private final int CMD_ENDELEMENT = 3;
    static private final int CMD_CHARACTERS = 4;
    static private final int CMD_STARTPREFIXMAPPING = 5;
    static private final int CMD_ENDPREFIXMAPPING = 6;
    static private final int CMD_PROCESSINGINSTRUCTION = 7;
    static private final int CMD_END = 8;

    private int command;
    private int exitStatus;
    private boolean started = false;

    private SAXException exception;
    private Locator locator;
    private PrefixMapper prefixMapper;

    private SchemaRewriter schemaRewriter;

    private SchemaFactory schemaFactory;
    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    private String uri;
    private String localName;
    private String qName;
    private Attributes attributes;
    private char[] ch;
    private int start;
    private int length;
    private String prefix;
    private String target;
    private String data;

    synchronized private void sendCmd(int cmd) {
        if (!started) return;
        if (exception != null) return;
        command = cmd;
        exitStatus = 0;
        notify();
        while (exitStatus == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    private void checkException() throws SAXException {
        if (exception != null) throw exception;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        sendCmd(CMD_SETDOCUMENTLOCATOR);
    }

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.attributes = attributes;
        sendCmd(CMD_STARTELEMENT);
        checkException();
    }

    public void characters(char[] ch,
                           int start,
                           int length)
        throws SAXException {
        this.ch = ch;
        this.start = start;
        this.length = length;
        sendCmd(CMD_CHARACTERS);
        checkException();
    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        sendCmd(CMD_ENDELEMENT);
        checkException();
    }

    public void startPrefixMapping(String prefix,
                                   String uri)
        throws SAXException {
        this.prefix = prefix;
        this.uri = uri;
        sendCmd(CMD_STARTPREFIXMAPPING);
        checkException();
    }

    public void endPrefixMapping(String prefix)
        throws SAXException {
        this.prefix = prefix;
        sendCmd(CMD_ENDPREFIXMAPPING);
        checkException();
    }

    public void processingInstruction(String target,
                                      String data)
        throws SAXException {
        this.target = target;
        this.data = data;
        sendCmd(CMD_PROCESSINGINSTRUCTION);
        checkException();
    }


    // --------------------------------------------------------------------------------
    // Proxy Content Handlers (private)
    // --------------------------------------------------------------------------------

    private void startElementI(String uri,
                               String localName,
                               String qName,
                               Attributes attrs) throws SAXException {
        if (schemaRewriter == null) {
            h.startElement(uri, localName, qName, attrs);
        } else {
            schemaRewriter.startElement(uri, localName, qName, attrs);
        }
    }

    private void endElementI(String uri,
                             String localName,
                             String qName) throws SAXException {
        if (schemaRewriter == null) {
            h.endElement(uri, localName, qName);
        } else {
            schemaRewriter.endElement(uri, localName, qName);
        }
    }

    private void charactersI(char[] ch,
                             int start,
                             int length)
        throws SAXException {
        if (schemaRewriter == null) {
            h.characters(ch, start, length);
        } else {
            schemaRewriter.characters(ch, start, length);
        }
    }

    private void endDocumentI() throws SAXException {
        if (schemaRewriter == null) {
            h.endDocument();
        } else {
            schemaRewriter.endDocument();
        }
    }

    private void endPrefixMappingI(String prefix)
        throws SAXException {
        if (schemaRewriter == null) {
            h.endPrefixMapping(prefix);
        } else {
            schemaRewriter.endPrefixMapping(prefix);
        }
    }

    @SuppressWarnings("unused")
    private void ignorableWhitespaceI(char[] ch,
                                      int start,
                                      int length)
        throws SAXException {
        if (schemaRewriter == null) {
            h.ignorableWhitespace(ch, start, length);
        } else {
            schemaRewriter.ignorableWhitespace(ch, start, length);
        }
    }

    private void processingInstructionI(String target,
                                        String data) throws SAXException {
        if (schemaRewriter == null) {
            h.processingInstruction(target, data);
        } else {
            schemaRewriter.processingInstruction(target, data);
        }
    }

    private void setDocumentLocatorI(Locator l) {
        if (schemaRewriter == null) {
            h.setDocumentLocator(l);
        } else {
            schemaRewriter.setDocumentLocator(l);
        }
    }

    @SuppressWarnings("unused")
    private void skippedEntityI(String name) throws SAXException {
        if (schemaRewriter == null) {
            h.skippedEntity(name);
        } else {
            schemaRewriter.skippedEntity(name);
        }
    }

    private void startDocumentI() throws SAXException {
        if (schemaRewriter == null) {
            h.startDocument();
        } else {
            schemaRewriter.startDocument();
        }
    }

    private void startPrefixMappingI(String prefix, String uri)
        throws SAXException {
        if (schemaRewriter == null) {
            h.startPrefixMapping(prefix, uri);
        } else {
            schemaRewriter.startPrefixMapping(prefix, uri);
        }
    }

    // --------------------------------------------------------------------------------
    // Input side 
    // --------------------------------------------------------------------------------

    synchronized private void startParse() throws SAXException {
        if (h == null) throw new SAXException("Internal Error");
        if (locator != null) setDocumentLocatorI(locator);
        startDocumentI();
        Object effectivePrefixMapping = prefixMapper.startEffectivePrefixMappings(h);
        mainloop:
        for (;;) {
            while (exitStatus != 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }
            try {
                switch (command) {
                case CMD_STARTELEMENT:
                    startElementI(uri, localName, qName, attributes);
                    break;
                case CMD_ENDELEMENT:
                    endElementI(uri, localName, qName);
                    break;
                case CMD_CHARACTERS:
                    charactersI(ch, start, length);
                    break;
                case CMD_STARTPREFIXMAPPING:
                    startPrefixMappingI(prefix, uri);
                    break;
                case CMD_ENDPREFIXMAPPING:
                    endPrefixMappingI(prefix);
                    break;
                case CMD_PROCESSINGINSTRUCTION:
                    processingInstructionI(target, data);
                    break;
                case CMD_SETDOCUMENTLOCATOR:
                    setDocumentLocatorI(locator);
                    break;
                case CMD_END:
                    break mainloop;
                }
            } catch (SAXException e) {
                exception = e;
            }
            exitStatus = 1;
            notify();
        }
        prefixMapper.endEffectivePrefixMappings(effectivePrefixMapping, h);
        endDocumentI();
    }

    /**************************************************
                     Thread Entry and Control Interface
    **************************************************/
    private SAXSource source;
            
    public void run() {
        synchronized (this) {
            try {
                schema = schemaFactory.newSchema(source);
            } catch (SAXException e) {
                exception = e;
            }
            exitStatus = 2;
            notify();
        }
    }

    void begin(SchemaFactory schemaFactory, InputSource is,
               String ns, boolean forAttribute) {
        this.schemaFactory = schemaFactory;
        source = new SAXSource(this, is);

        if (forAttribute
            && RELAXNGAttributeSchemaRewriter.RELAX_NG_NAMESPACE_URI.equals(ns)) {
            schemaRewriter = new RELAXNGAttributeSchemaRewriter();
        }

        start();
        started = true;
    }

    void end() throws SAXException {
        sendCmd(CMD_END);
        started = false;
        checkException();
    }

    private SchemaReaderProxy(Locator locator, PrefixMapper prefixMapper) {
        super("SchemaReaderProxy");
        this.locator = locator;
        this.schema = null;
        this.prefixMapper = prefixMapper;
    }

    public static SchemaReaderProxy newProxy(Locator locator,
                                             PrefixMapper prefixMapper) {
        return new SchemaReaderProxy(locator, prefixMapper);
    }
}
