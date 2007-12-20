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
package org.eclipse.actf.ai.fennec.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.actf.ai.fennec.INVM3Entry;
import org.eclipse.actf.ai.query.IQuery;
import org.eclipse.actf.ai.query.QueryService;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class NVM3DOMReader {
    public static final String NVM3_NAMESPACE_URI = "http://www.ibm.com/xmlns/prod/aiBrowser/fennec";
    public static final String NVM3_DOCUMENT_ELEMENT_NAME = "fennec";

    private NVM3ServiceImpl nvm3Service;

    private void error(String message) {
        System.err.println(message);
    }

    private IQuery parseQuery(Element e, IQuery parentQuery) {
        return QueryService.parse(e, parentQuery);
    }

    private NVM3Metadata parseNode(NVM3Mode mode, Element e, IQuery parentQuery) {
        NodeList nl = e.getChildNodes();
        IQuery q = parseQuery(e, parentQuery);
        if (q != null) parentQuery = q;
        NVM3Metadata[] mds = parseInternal(mode, nl, parentQuery);
        return new NVM3GroupMetadata(nvm3Service, q, mode, mds);
    }

    private NVM3Metadata parseHeader(NVM3Mode mode, Element e, short level, IQuery parentQuery) {
        // IQuery q = parseQuery(e, parentQuery);
        return NVM3SimpleMetadata.newHeader(nvm3Service, null, mode, level);
    }

    private List parseTextFormat(Element e, IQuery parentQuery) {
        NodeList nl = e.getChildNodes();
        int len = nl.getLength();
        if (len == 0) return null;
        ArrayList al = new ArrayList();
        for (int i = 0; i < len; i++) {
            Node n = nl.item(i);
            switch (n.getNodeType()) {
            case Node.TEXT_NODE:
                al.add(n.getNodeValue());
                break;
            case Node.ELEMENT_NODE:
                Element ce = (Element) n;
                if ("ref".equals(ce.getLocalName())) {
                    IQuery q = parseQuery((Element) n, parentQuery);
                    al.add(q);
                } else {
                    error("Invalid element in text format:" + n.getNodeName());
                }
                break;
            }
        }
        return al;
    }

    private NVM3Metadata parseAltText(NVM3Mode mode, Element e, IQuery parentQuery) {
        IQuery q = parseQuery(e, parentQuery);
        if (q != null) parentQuery = q;
        List l = parseTextFormat(e, parentQuery);
        return NVM3SimpleMetadata.newAltText(nvm3Service, q, mode, l);
    }

    private NVM3Metadata parseDescription(NVM3Mode mode, Element e, IQuery parentQuery) {
        IQuery q = parseQuery(e, parentQuery);
        if (q != null) parentQuery = q;
        List l = parseTextFormat(e, parentQuery);
        return NVM3SimpleMetadata.newDescription(nvm3Service, mode, q, l);
    }

    private boolean parseBoolean(String str) {
        String str2 = str.trim();
        if (str2.equals("0")) {
            return false;
        } else if (str2.equals("false")) {
            return false;
        } else if (str2.equals("1")) {
            return true;
        } else if (str2.equals("true")) {
            return true;
        }
        error("Invalide boolean value:" + str2);
        return false;
    }

    private int parseTrigger(String str) {
        String str2 = str.trim();
        if ("always".equals(str2)) {
            return NVM3Mode.TRIGGER_ALWAYS;
        } else if ("move".equals(str2)) {
            return NVM3Mode.TRIGGER_MOVE;
        } else if ("click".equals(str2)) {
            return NVM3Mode.TRIGGER_CLICK;
        } else if (str.length() == 0) {
            return NVM3Mode.TRIGGER_ALWAYS;
        } else {
            error("Invalid trigger attribute:" + str2);
        }
        return NVM3Mode.TRIGGER_MOVE;
    }

    private NVM3Metadata parseInternalForElement(NVM3Mode mode, Element e, IQuery parentQuery) {
        String name = e.getLocalName();
        if (name.equals("node")) {
            return parseNode(mode, e, parentQuery);
        } else if (name.equals("table")) {
            // TODO
        } else if (name.equals("ul")) {
            // TODO
        } else if (name.equals("ol")) {
            // TODO
        } else if (name.equals("h-")) {
            return parseHeader(mode, e, (short) -1, parentQuery);
        } else if (name.equals("h1")) {
            return parseHeader(mode, e, (short) 1, parentQuery);
        } else if (name.equals("h2")) {
            return parseHeader(mode, e, (short) 2, parentQuery);
        } else if (name.equals("h3")) {
            return parseHeader(mode, e, (short) 3, parentQuery);
        } else if (name.equals("h4")) {
            return parseHeader(mode, e, (short) 4, parentQuery);
        } else if (name.equals("h5")) {
            return parseHeader(mode, e, (short) 5, parentQuery);
        } else if (name.equals("h6")) {
            return parseHeader(mode, e, (short) 6, parentQuery);
        } else if (name.equals("altText")) {
            return parseAltText(mode, e, parentQuery);
        } else if (name.equals("description")) {
            return parseDescription(mode, e, parentQuery);
        } else if (name.equals("metadata")) {
            // TODO
        } else if (name.equals("keyDescription")) {
            // TODO
        } else if (name.equals("attach")) {
            NodeList nl = e.getChildNodes();
            IQuery q = parseQuery(e, parentQuery);
            if (q == null) {
                // TODO;
            }

            String triggerStr = e.getAttributeNS(null, "trigger");
            int trigger = parseTrigger(triggerStr);

            String autoStr = e.getAttributeNS(null, "auto");
            boolean auto;
            if (autoStr.length() == 0) {
                auto = true;
            } else {
                auto = parseBoolean(autoStr);
            }
            String changelessStr = e.getAttributeNS(null, "changeless");
            boolean changeless;
            if (changelessStr.length() == 0) {
                changeless = false;
            } else {
                changeless = parseBoolean(changelessStr);
            }
            String waitContentsStr = e.getAttributeNS(null, "waitContents");
            boolean waitContents;
            if (waitContentsStr.length() == 0) {
                waitContents = false;
            } else {
                waitContents = parseBoolean(waitContentsStr);
            }

            NVM3Mode nextMode = new NVM3Mode(NVM3Mode.TYPE_ATTACH, trigger,
                                             auto, changeless, waitContents);

            NVM3Metadata[] mds = parseInternal(nextMode, nl, q);
            return NVM3RecombinantMetadata.newAttach(nvm3Service, q, mode, nextMode, mds);
        } else if (name.equals("unwrap")) {
            NodeList nl = e.getChildNodes();
            IQuery q = parseQuery(e, parentQuery);
            if (q == null) {
                // TODO;
            }

            String triggerStr = e.getAttributeNS(null, "trigger");
            int trigger = parseTrigger(triggerStr);

            NVM3Mode nextMode = new NVM3Mode(NVM3Mode.TYPE_UNWRAP, trigger, false, false, false);

            NVM3Metadata[] mds = parseInternal(nextMode, nl, q);
            return NVM3RecombinantMetadata.newUnwrap(nvm3Service, q, mode, nextMode, mds);
        }
        return null;
    }

    private NVM3Metadata[] parseInternal(NVM3Mode mode, NodeList nl, IQuery parentQuery) {
        ArrayList al = new ArrayList();
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String ns = n.getNamespaceURI();
                if (ns.equals(NVM3_NAMESPACE_URI)) {
                    Element e = (Element) n;
                    NVM3Metadata md = parseInternalForElement(mode, e, parentQuery);
                    if (md != null) al.add(md);
                }
            }
        }
        return (NVM3Metadata[]) al.toArray(new NVM3Metadata[0]);
    }

    private NVM3Metadata parseTop(Element e) {
        String namespaceURI = e.getNamespaceURI();
        if (!namespaceURI.equals(NVM3_NAMESPACE_URI)) {
            error("The namespace URI of the document element must be " + NVM3_NAMESPACE_URI);
            return null;
        }
        String localName = e.getLocalName();
        if (!(NVM3_DOCUMENT_ELEMENT_NAME.equals(localName))) {
            error("The document element is not nvm3");
        }
        NVM3Mode rootMode = new NVM3Mode(NVM3Mode.TYPE_SIMPLE);

        IQuery q = parseQuery(e, null);
        NVM3Metadata[] mds = parseInternal(rootMode, e.getChildNodes(), q);
        if (mds.length == 0) {
            return null;
        }
        
        return new NVM3GroupMetadata(nvm3Service, q, rootMode, mds);
    }

    public NVM3Metadata parse(INVM3Entry entry) throws XMLStoreException {
        NVM3EntryImpl ei = (NVM3EntryImpl) entry;
        IXMLInfo info = ei.getIXMLInfo();
        Node n = info.getRootNode();
        Element e;
        if (n instanceof Document) {
            e = ((Document) n).getDocumentElement();
        } else if (n instanceof Element) {
            e = (Element) n;
        } else {
            throw new XMLStoreException("Failed to load NVM3.", null);
        }
        return parseTop(e);
    }

    NVM3DOMReader(NVM3ServiceImpl nvm3Service) {
        this.nvm3Service = nvm3Service;
    }
}
