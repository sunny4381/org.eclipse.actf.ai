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
import java.net.URL;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.Location;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLValidateAction;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * The <code>SchemaLoader</code> is a schema loader class.
 */
class SchemaLoader implements NVDLValidateAction.SchemaLoader {
    private final String baseIRI;
    private String schemaNS;
    // private Location loc;

    private void detectSchema(URL schemaURL) throws NVDLException {
        try {
            schemaNS = null;
            XMLReader r = XMLReaderFactory.createXMLReader();
            r.setContentHandler(new DefaultHandler() {
                public void startElement(String uri,
                                         String localName,
                                         String qName,
                                         Attributes attributes)
                    throws SAXException {
                    schemaNS = uri;
                    throw new SAXException("stop");
                }
            });
            r.parse(schemaURL.toString());
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw new NVDLException("SchemaLoader.OpenSchemaIOError",
                                        new Object[] {e.getLocalizedMessage()});
            }
        }
        if (schemaNS == null) {
            throw new NVDLException("SchemaLoader.SchemaTypeDetectionError",
                                    new Object[] {schemaURL});
        }
        Log.debug("Detected the schema type:" + schemaNS + " in " + schemaURL);
    }

    private static class SAXLoader implements ContentHandler {
        private final SchemaReaderProxy schemaReaderProxy;

        Schema getSchema(SchemaFactory sf, URL schemaURL, String schemaNS, boolean forAttribute)
            throws SAXException, IOException {
            String systemId = schemaURL.toString();
            schemaReaderProxy.begin(sf, new InputSource(systemId), schemaNS, forAttribute);
            XMLReader r = XMLReaderFactory.createXMLReader();
            r.setFeature("http://xml.org/sax/features/namespaces", true);
            r.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            r.setContentHandler(this);
            r.parse(systemId);
            schemaReaderProxy.end();
            return schemaReaderProxy.getSchema();
        }

        SAXLoader(SchemaReaderProxy proxy) {
            this.schemaReaderProxy = proxy;
        }

        public void characters(char[] ch,
                               int start,
                               int length)
            throws SAXException {
            schemaReaderProxy.characters(ch, start, length);
        }

        public void endDocument() throws SAXException {
        }

        public void endElement(String uri,
                               String localName,
                               String qName) throws SAXException {
            schemaReaderProxy.endElement(uri, localName, qName);
        }

        public void endPrefixMapping(String prefix)
            throws SAXException {
            schemaReaderProxy.endPrefixMapping(prefix);
        }

        public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        }

        public void processingInstruction(String target,
                                          String data) 
            throws SAXException {
            schemaReaderProxy.processingInstruction(target, data);
        }

        public void setDocumentLocator(Locator locator) {
            schemaReaderProxy.setDocumentLocator(locator);
        }

        public void skippedEntity(String arg0) throws SAXException {
        }

        public void startDocument() throws SAXException {
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            schemaReaderProxy.startElement(uri, localName, qName, attributes);
        }

        public void startPrefixMapping(String prefix,
                                       String uri) 
            throws SAXException {
            schemaReaderProxy.startPrefixMapping(prefix, uri);
        }
    }

    public Schema load(NVDLValidateAction validateAction, boolean forAttribute)
        throws NVDLException {
        String schemaIRI = validateAction.getSchemaIRI();
        // String schemaType = validateAction.getSchemaType();
        if (schemaIRI == null) return null;
        URL schemaURL;
        try {
            URL baseURL = new URL(baseIRI);
            schemaURL = new URL(baseURL, schemaIRI);

            detectSchema(schemaURL);
            SchemaFactory sf = SchemaFactory.newInstance(schemaNS);
            Log.debug("Try to open schema:" + schemaURL);

            PrefixMapper prefixMapper = new PrefixMapper();
            SchemaReaderProxy schemaReaderProxy = SchemaReaderProxy.newProxy(null, prefixMapper);

            SAXLoader loader = new SAXLoader(schemaReaderProxy);
            
            return loader.getSchema(sf, schemaURL, schemaNS, forAttribute);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new NVDLException("No appropriate validator implementation for "
                                        + schemaNS);
            } else {
                throw new NVDLException(e);
            }
        }
    }

    SchemaLoader(String baseIRI, Location loc) {
        this.baseIRI = baseIRI;
        // this.loc = loc;
    }
}

