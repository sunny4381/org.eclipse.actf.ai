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

package org.eclipse.actf.ai.xmlstore.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.ai.xmlstore.spi.XMLSelectorInfo;
import org.eclipse.actf.ai.xmlstore.spi.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class XMLFile implements IXMLInfo {
    protected final File file;

    private XMLSelectorInfo selectorInfo;

    private XMLReader reader;

    protected Document doc;

    XMLSelectorInfo getSelectorInfo() {
        return selectorInfo;
    }

    private long lastModified;

    XMLFile(File file) throws XMLStoreException {
        this.file = file;
        reset();
    }

    XMLFile(File file, boolean flag) throws XMLStoreException {
        this.file = file;
        if (flag)
            reset();
    }

    private boolean isNotLatest() {
        return (file.lastModified() > lastModified);
    }

    public String getDocumentation() {
        return selectorInfo.getDocumentation();
    }

    public boolean isUserEntry() {
        return selectorInfo.isUserEntry();
    }

    public int getPriority() {
        return selectorInfo.getPriority();
    }

    private InputSource getInputSource() throws FileNotFoundException {
        return new InputSource(new FileInputStream(file));
    }

    public Node getRootNode() throws XMLStoreException {
        if (isNotLatest())
            reset();
        if (doc == null) {
            DocumentBuilder builder;
            try {
                builder = XMLUtil.newDocumentBuilder();
                InputSource is = getInputSource();
                doc = builder.parse(is);
            } catch (Exception e) {
                throw new XMLStoreException("DOM initialization is failed.", e);
            }
        }
        return doc.getDocumentElement();
    }

    private void initReader() throws SAXException {
        if (reader == null) {
            reader = XMLReaderFactory.createXMLReader();
        }
    }

    public void setContentHandler(ContentHandler h) {
        try {
            initReader();
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }
        reader.setContentHandler(h);
    }

    public void startSAX() throws XMLStoreException, SAXException {
        try {
            initReader();
            InputSource is = getInputSource();
            reader.parse(is);
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLStoreException("SAX startup is failed", e);
        }
    }

    public void reset() throws XMLStoreException {
        this.reader = null;
        this.doc = null;
        selectorInfo = XMLSelectorInfo.parse(this);
        this.lastModified = file.lastModified();
    }

}
