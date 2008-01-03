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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * The <code>ContentPrintHandler</code> is a SAX content handler that dumps
 * all SAX events.
 */
public class WritingContentHandler extends DefaultHandler2 {
    private final Writer out;
    private final boolean requireClose;
    // private Locator locator;
    private final HashMap<String, String> prefixMapping = new HashMap<String, String>();

    private void outputEscape(char[] ch, int start, int length) throws IOException {
        int end = start + length;
        for ( ; start < end; start++) {
            char c = ch[start];
            switch (c) {
            case '<':
                out.append("&lt;");
                break;
            case '>':
                out.append("&gt;");
                break;
            case '&':
                out.append("&amp;");
                break;
            default:
                out.append(c);
            }
        }
    }

    private void outputEscapeAttrVal(String val) throws IOException {
        int len = val.length();
        int i, j;

        outer:
        for (i = 0; i < len; i = j) {
            for (j = i; j < len; j++) {
                char c = val.charAt(j);
                switch (c) {
                case '<':
                    out.append(val, i, j);
                    out.append("&lt;");
                    continue outer;
                case '>':
                    out.append(val, i, j);
                    out.append("&gt;");
                    continue outer;
                case '&':
                    out.append(val, i, j);
                    out.append("&amp;");
                    continue outer;
                case '\'':
                    out.append(val, i, j);
                    out.append("&apos;");
                    continue outer;
                }
            }
            out.append(val, i, j);
        }
    }

    public void setDocumentLocator(Locator locator) {
        // this.locator = locator;
    }

    public void startDocument() throws SAXException {
        try {
            if (out instanceof OutputStreamWriter) {
                Charset encoding = Charset.forName(((OutputStreamWriter) out).getEncoding());
                out.append("<?xml version=\"1.0\" encoding=\"");
                out.append(encoding.name());
                out.append("\"?>\n");
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endDocument() throws SAXException {
        try {
            if (requireClose) out.close();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMapping.put(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attrs) throws SAXException {
        boolean firstAttr = true;
        try {
            out.append('<');
            out.append(qName);
            Iterator<String> it = prefixMapping.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String aName;
                if (key.length() > 0) {
                    aName = "xmlns:" + key;
                } else {
                    aName = "xmlns";
                }
                if (attrs.getIndex(aName) >= 0) continue;
                if (firstAttr) {
                    out.append(' ');
                    firstAttr = false;
                }
                out.append(aName);
                out.append("=\"");
                out.append(prefixMapping.get(key));
                out.append("\" ");
            }
            prefixMapping.clear();

            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                String qNameA = attrs.getQName(i);
                String value = attrs.getValue(i);
                if (firstAttr) {
                    out.append(' ');
                    firstAttr = false;
                }
                out.append(qNameA);
                out.append("=\"");
                outputEscapeAttrVal(value);
                out.append("\" ");
            }
            out.append('>');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    

    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        try {
            out.append("</");
            out.append(qName);
            out.append('>');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            outputEscape(ch, start, length);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            out.write(ch, start, length);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    public void processingInstruction(String target, String data) throws SAXException {
        try {
            out.append("<?");
            out.append(target);
            out.append(' ');
            out.append(data);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void skippedEntity(String name) throws SAXException {
    }
    
    @Override
	public void comment(char[] ch, int start, int length) throws SAXException {
        try {
        	out.append("<!-- ");
        	outputEscape(ch, start, length);
        	out.append("-->");
        } catch (IOException e) {
            throw new SAXException(e);
        }
	}

	public WritingContentHandler(String name) {
        this.out = new OutputStreamWriter(System.out);
        this.requireClose = false;
    }

    public WritingContentHandler(OutputStream os) {
        this.out = new OutputStreamWriter(os);
        this.requireClose = true;
    }

    public WritingContentHandler(Writer out) {
        this.out = out;
        this.requireClose = true;
    }
}
