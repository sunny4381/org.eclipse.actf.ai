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

import org.eclipse.actf.ai.xmlstore.nvdl.util.EmptyAttributes;
import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RELAXNGAttributeSchemaRewriter implements SchemaRewriter {
    private static final String RELAX_NG_DEFAULT_PREFIX = "relaxnginstanceForSchemaRewriter";
    public static final String RELAX_NG_NAMESPACE_URI = "http://relaxng.org/ns/structure/1.0";

    private ContentHandler h;
    private PrefixMapper prefixMapper;

    private PrefixMapper.StartElementToken setFor1stElement;

    private int level = 0;

    public void setup(ContentHandler h,
                      PrefixMapper prefixMapper) {
        this.h = h;
        this.prefixMapper = prefixMapper;
    }

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attrs) throws SAXException {
        if (level == 0) {
            setFor1stElement =
                prefixMapper.sendStartElement(h,
                                              RELAX_NG_NAMESPACE_URI,
                                              "element",
                                              RELAX_NG_DEFAULT_PREFIX,
                                              EmptyAttributes.getInstance());
            prefixMapper.sendEmptyElement(h,
                                          RELAX_NG_NAMESPACE_URI,
                                          "anyName",
                                          RELAX_NG_DEFAULT_PREFIX,
                                          EmptyAttributes.getInstance());
        }
        h.startElement(uri, localName, qName, attrs);
        level++;
    }

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        h.endElement(uri, localName, qName);
        level--;
        if (level == 0) {
            prefixMapper.sendEndElement(h, setFor1stElement);
        }
        return;
    }

    public void characters(char[] ch,
                           int start,
                           int length)
        throws SAXException {
        h.characters(ch, start, length);
    }

    public void endDocument() throws SAXException {
        h.endDocument();
    }

    public void endPrefixMapping(String prefix)
        throws SAXException {
        h.endPrefixMapping(prefix);
    }

    public void ignorableWhitespace(char[] ch,
                                    int start,
                                    int length)
        throws SAXException {
        h.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target,
                                      String data) throws SAXException {
        h.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator l) {
        h.setDocumentLocator(l);
    }

    public void skippedEntity(String name) throws SAXException {
        h.skippedEntity(name);
    }

    public void startDocument() throws SAXException {
        h.startDocument();
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        h.startPrefixMapping(prefix, uri);
    }

}
