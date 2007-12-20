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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * The <code>PrefixMapper</code> is a utility class that remembers
 * effective prefix mappings and properly dispatch startPrefixMappings
 * and endPrefixMappings.
 */
public class PrefixMapper {
    private class PrefixDef {
        PrefixDef next;
        PrefixDef previous;
        final String prefix;
        final String uri;

        PrefixDef(String prefix, String uri, PrefixDef next) {
            this.prefix = prefix;
            this.uri = uri;
            this.next = next;
            if (next != null) {
                next.previous = this;
            }
        }
    }

    private PrefixDef latest;
    private PrefixDef oldest;

    public void reset() {
        latest = oldest = null;
    }

    public void startPrefixMapping(String prefix, String uri) {
        PrefixDef pd = new PrefixDef(prefix, uri, latest);
        latest = pd;
        if (oldest == null) {
            oldest = pd;
        }
        assert (oldest == null) || (oldest.next == null);
    }

    public void endPrefixMapping(String prefix) {
        for (PrefixDef pd = latest; pd != null; pd = pd.next) {
            if (pd.prefix.equals(prefix)) {
                if (pd.previous == null) {
                    latest = pd.next;
                    if (latest == null) {
                        oldest = null;
                    }
                } else if (pd.next == null) {
                    pd.previous.next = null;
                    oldest = pd.previous;
                } else {
                    pd.previous.next = pd.next;
                    pd.next.previous = pd.previous;
                }
                break;
            }   
        }
        assert (oldest == null) || (oldest.next == null);
    }

    public static class PrefixReturnVal {
        public final String prefix;
        public final boolean requireDecl;
        PrefixReturnVal(String prefix, boolean requireDecl) {
            this.prefix = prefix;
            this.requireDecl = requireDecl;
        }
    }

    public PrefixReturnVal uniquePrefix(String prefixBase, String ns) {
        String tryPrefix = prefixBase;
        int tryCount = 0;
        retry:
        for (;;) {
            for (PrefixDef pd = oldest; pd != null; pd = pd.previous) {
                if (pd.uri.equals(ns)) {
                    return new PrefixReturnVal(pd.prefix, false);
                }
                if (pd.prefix.equals(tryPrefix)) {
                    tryCount++;
                    tryPrefix = prefixBase + tryCount;
                    continue retry;
                }
            }
            return new PrefixReturnVal(tryPrefix, true);
        }
    }

    public Object startEffectivePrefixMappings(ContentHandler h)
        throws SAXException {
        PrefixDef last = null;
        for (PrefixDef pd = oldest; pd != null; pd = pd.previous) {
            h.startPrefixMapping(pd.prefix, pd.uri);
            last = pd;
        }
        return last;
    }

    public void endEffectivePrefixMappings(Object effectiveMapping,
                                           ContentHandler h)
        throws SAXException {
        for (PrefixDef pd = (PrefixDef) effectiveMapping;
             pd != null;
             pd = pd.next) {
            h.endPrefixMapping(pd.prefix);
        }
    }

    public static class StartElementToken {
        final String localName;
        final String ns;
        final String qName;
        final String prefix;
        final boolean requireDecl;

        StartElementToken(String localName,
                          String ns,
                          String qName,
                          String prefix,
                          boolean requireDecl) {
            this.localName = localName;
            this.ns = ns;
            this.qName = qName;
            this.prefix = prefix;
            this.requireDecl = requireDecl;
        }
    }

    public StartElementToken sendStartElement(ContentHandler h,
                                              String ns,
                                              String localName,
                                              String preferredPrefix,
                                              Attributes attrs) throws SAXException {
        PrefixReturnVal prv = uniquePrefix(preferredPrefix, ns);
        if (prv.requireDecl) {
            h.startPrefixMapping(prv.prefix, ns);
        }
        String qName = prv.prefix + ":" + localName;
        h.startElement(ns, localName, qName, attrs);
        return new StartElementToken(localName, ns, qName,
                                     prv.prefix, prv.requireDecl);
    }

    public void sendEndElement(ContentHandler h,
                               StartElementToken set) throws SAXException {
        h.endElement(set.ns, set.localName, set.qName);
        if (set.requireDecl) {
            h.endPrefixMapping(set.prefix);
        }
    }

    public void sendEmptyElement(ContentHandler h,
                                 String ns,
                                 String localName,
                                 String preferredPrefix,
                                 Attributes attrs) throws SAXException {
        PrefixReturnVal prv = uniquePrefix(preferredPrefix, ns);
        if (prv.requireDecl) {
            h.startPrefixMapping(prv.prefix, ns);
        }
        String qName = prv.prefix + ":" + localName;
        h.startElement(ns, localName, qName, attrs);
        h.endElement(ns, localName, qName);
        if (prv.requireDecl) {
            h.endPrefixMapping(prv.prefix);
        }
    }

    public PrefixMapper() {
        this.latest = this.oldest = null;
    }
}

