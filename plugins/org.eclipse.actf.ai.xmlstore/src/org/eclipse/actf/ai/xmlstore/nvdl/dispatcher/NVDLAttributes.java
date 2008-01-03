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

import java.util.ArrayList;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAttributeSection;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLElement;
import org.xml.sax.Attributes;


/**
 * The <code>NVDLAttributes</code> is the proxy class for SAX Attributes.
 */
public class NVDLAttributes implements Attributes, NVDLAttributeSection {
    /********************************************
               Impl. for org.xml.sax.Attributes
    ********************************************/
    public int getLength() {
        int len = indexes.size();
        if (extAttrs == null) return len;
        return len + extAttrs.size();
    }

    public String getURI(int index) {
        if (isIndexBase(index)) {
            return base.getURI(baseIdx(index));
        } else if (isIndexExt(index)) {
            ExtAttr ea = getExtAttr(index);
            return ea.uri;
        }
        return null;
    }

    public String getLocalName(int index) {
        if (isIndexBase(index)) {
            return base.getLocalName(baseIdx(index));
        } else if (isIndexExt(index)) {
            ExtAttr ea = getExtAttr(index);
            return ea.localName;
        }
        return null;
    }

    public String getQName(int index) {
        if (isIndexBase(index)) {
            return base.getQName(baseIdx(index));
        } else if (isIndexExt(index)) {
            ExtAttr ea = getExtAttr(index);
            return ea.qName;
        }
        return null;
    }

    public String getType(int index) {
        if (isIndexBase(index)) {
            return base.getType(baseIdx(index));
        } else if (isIndexExt(index)) {
            return "CDATA";
        }
        return null;
    }

    public String getValue(int index) {
        if (isIndexBase(index)) {
            return base.getValue(baseIdx(index));
        } else if (isIndexExt(index)) {
            ExtAttr ea = getExtAttr(index);
            return ea.value;
        }
        return null;
    }

    public int getIndex(String uri, String localName) {
        int baseIdx = base.getIndex(uri, localName);
        if (baseIdx >= 0) {
            return getIdx(baseIdx);
        }
        int len = extAttrs.size();
        for (int i = 0; i < len; i++) {
            ExtAttr ea = extAttrs.get(i);
            if (uri.equals(ea.uri) && localName.equals(ea.localName)) return indexes.size() + i;
        }
        return -1;
    }

    public int getIndex(String qName) {
        int baseIdx = base.getIndex(qName);
        if (baseIdx >= 0) {
            return getIdx(baseIdx);
        }
        int len = extAttrs.size();
        for (int i = 0; i < len; i++) {
            ExtAttr ea = extAttrs.get(i);
            if (qName.equals(ea.qName)) return indexes.size() + i;
        }
        return -1;
    }

    public String getType(String uri, String localName) {
        int baseIdx = getIndex(uri, localName);
        if (baseIdx < 0) return null;
        return getType(baseIdx);
    }

    public String getType(String qName) {
        int baseIdx = getIndex(qName);
        if (baseIdx < 0) return null;
        return getType(baseIdx);
    }

    public String getValue(String uri, String localName) {
        int baseIdx = getIndex(uri, localName);
        if (baseIdx < 0) return null;
        return getValue(baseIdx);
    }

    public String getValue(String qName) {
        int baseIdx = getIndex(qName);
        if (baseIdx < 0) return null;
        return getValue(baseIdx);
    }

    /********************************************
               Impl. for NVDL
    ********************************************/

    // private NVDLMode mode;
    private final String sectionNS;
    private final NVDLElement baseElement;
    private final Attributes base;
    private final ArrayList<Integer> indexes = new ArrayList<Integer>();

    private ArrayList<ExtAttr> extAttrs;

    private static class ExtAttr {
        final String uri;
        final String localName;
        final String qName;
        final String value;
        ExtAttr(String uri, String localName, String qName, String value) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.value = value;
        }
    }

    private int baseIdx(int idx) {
        Integer iv = (Integer) indexes.get(idx);
        return iv.intValue();
    }

    private boolean isIndexBase(int idx) {
        return ((idx >= 0) && (idx < indexes.size()));
    }

    private boolean isIndexExt(int idx) {
        if (extAttrs == null) return false;
        int baseLen = indexes.size();
        return ((idx >= baseLen) && (idx < baseLen + extAttrs.size()));
    }

    private ExtAttr getExtAttr(int index) {
        return extAttrs.get(index - indexes.size());
    }

    private int getIdx(int i) {
        return indexes.indexOf(new Integer(i));
    }

    void addAttribute(int i) {
        indexes.add(new Integer(i));
    }

    void addAttributes(NVDLAttributes part) {
        assert part.base == this.base;
        int len = part.indexes.size();
        for (int i = 0; i < len; i++) {
            Integer idx = part.indexes.get(i);
            indexes.add(idx);
        }
    }

    public void addExtAttribute(String uri, String localName, String qName, String value) {
        ExtAttr ea = new ExtAttr(uri, localName, qName, value);
        if (extAttrs == null) extAttrs = new ArrayList<ExtAttr>();
        extAttrs.add(ea);
    }

    // --------------------------------------------------------------------------------
    //    NVDLAttributeSection interface impl.
    // --------------------------------------------------------------------------------
    
    public String getNamespace() {
        return sectionNS;
    }

    public NVDLElement getBaseElement() {
        return baseElement;
    }

    // --------------------------------------------------------------------------------
    //    Constructors.
    // --------------------------------------------------------------------------------

    public NVDLAttributes(NVDLElement baseElement, Attributes base) {
        this.base = base;
        this.baseElement = baseElement;
        this.sectionNS = null;
    }

    NVDLAttributes(NVDLElement baseElement, Attributes base, String sectionNS) {
        this.base = base;
        this.baseElement = baseElement;
        this.sectionNS = sectionNS;
    }
}
