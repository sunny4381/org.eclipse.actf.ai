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

package org.eclipse.actf.ai.xmlstore.nvdl.rec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.actf.ai.xmlstore.nvdl.dispatcher.NVDLAttributes;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.ActionList;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.PDA;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.ActionList.InvalidIdException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLConst;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLElement;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper;
import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper.PrefixReturnVal;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The <code>NVDLSAXReconstructor</code> is a reconstructor for SAX interface.
 */

public class SAXReconstructor {
    private final NVDLRules rules;
    private final PDA pda;
    private ContentHandler output;

    public void reset() {
        currentElement = null;
        pda.reset();
    }

    private ArrayList<Input> inputs = new ArrayList<Input>();

    private NVDLElement currentElement;

    private ActionList requestActionList;

    private String waitingASNID;
    private ArrayList<Attr> resultASNAttributes;

    private NVDLElement createNVDLElement(String ns, String localName) {
        return new NVDLElement(ns, localName,
                               currentElement,
                               rules.getTriggerManager(),
                               0);
    }

    private PrefixMapper prefixMapper = new PrefixMapper();

    private Input responsedInput;

    private boolean finished;

    private boolean checkAndWaitInactive() {
        int size = inputs.size();
        for (int i = 0; i < size; i++) {
            Input input = inputs.get(i);
            if (input.active) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                return false;
            }
        }
        return true;
    }

    private synchronized void waitForInputReady() {
        for (;;) {
            if (checkAndWaitInactive()) return;
        }
    }

    private Input selectInput() {
        int size = inputs.size();
        Input selectedInput = null;
        int score = -1;
        for (int i = 0; i < size; i++) {
            Input input = inputs.get(i);

            if (waitingASNID != null) {
                if (!waitingASNID.equals(input.getExposingASNID())) continue;
                resultASNAttributes = input.getASNAttributes();
                waitingASNID = null;
                // TODO: score!
                return responsedInput;
            }

            ActionList al = input.getActionList();
            if (al == null) continue;

            if (requestActionList != null) {
                if (requestActionList.getCount() != al.getCount()) continue;
            }

            NVDLElement tryElement = input.retrieveNextElement();
            PDA.MatchResult mr = pda.matchActionList(tryElement, al);
            if (mr == PDA.MatchResult.MATCH) {
                int cscore = al.getPrecedence();
                if (score < cscore) {
                    selectedInput = input;
                    score = cscore;
                    // TODO
                    System.err.println("Select(" + tryElement.ns + "):" + tryElement.localName);
                    System.err.println(al);
                    break;
                }
            }
        }
        return selectedInput;
    }

    // Stack list for currently active inputs.
    private ArrayList<Input> activeInputList = new ArrayList<Input>();

    private Input popActiveInput() {
        int idx = activeInputList.size();
        if (idx == 1) return null;
        idx--;
        activeInputList.remove(idx);
        idx--;
        return activeInputList.get(idx);
    }

    private Input getLastActiveInput() {
        int idx = activeInputList.size();
        if (idx == 0) return null;
        return activeInputList.get(0);
    }

    private synchronized void startWorkerManage() throws NVDLReconstructionException {
        for (;;) {
            Input input;
            if (finished) {
                input = popActiveInput();
            } else {
                input = selectInput();
                if (input != null) {
                    activeInputList.add(input);
                } else {
                    StringBuffer buf = new StringBuffer("No Reconstruction Candidate:");

                    if (requestActionList != null) {
                        buf.append(requestActionList.toString());
                    }
                    System.err.println(buf);
                    if (output instanceof LexicalHandler) {
                        char[] bufch = buf.toString().toCharArray();
                        try {
                            ((LexicalHandler) output).comment(bufch, 0, bufch.length);
                        } catch (SAXException e) {
                        }
                    }
                    // throw new NVDLReconstructionException(buf.toString());
                    input = getLastActiveInput();
                }
            }
            if (input == null) return;

            input.notifyActivation(true);
            responsedInput = null;
            do {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            } while (responsedInput == null);
        }
    }

    private synchronized void notifyCompletion(Input input,
                                               boolean finished) {
        this.finished = finished;
        this.responsedInput = input;
        notify();
    }

    private class Attr {
        final String ns;
        final String prefix;
        final String localName;
        final String value;
        Attr(String ns, String qName, String value) {
            this.ns = ns;
            this.value = value;
            int idx = qName.indexOf(':');
            if (idx < 0) {
                this.localName = qName;
                this.prefix = "";
            } else {
                this.localName = qName.substring(idx + 1);
                this.prefix = qName.substring(0, idx);
            }
        }
    }

    private class PrefixEntry {
        final String prefix;
        final String ns;
        final PrefixEntry prev;
        PrefixEntry(PrefixEntry prev, String prefix, String ns) {
            this.prev = prev;
            this.prefix = prefix;
            this.ns = ns;
        }
        PrefixEntry(PrefixEntry prev) {
            this.prev = prev;
            this.prefix = null;
            this.ns = null;
        }
    }

    private class Input extends DefaultHandler {
        // This is an internal variable
        private boolean skipToNextElement;

        // Temporary space for creating the next NVDL element.
        private String nextNS;
        private String localName;
        private void setupNextElement(String ns, String localName) {
            this.nextNS = ns;
            this.localName = localName;
        }

        // accessed.
        public NVDLElement retrieveNextElement() {
            return createNVDLElement(nextNS, localName);
        }

        private PrefixEntry prefixList = null;
        private void storePrefixMapping(String prefix, String ns) {
            prefixMapper.startPrefixMapping(prefix, ns);
            prefixList = new PrefixEntry(prefixList, prefix, ns);
        }
        private void sendStartPrefixMapping() throws SAXException {
            PrefixEntry pe = prefixList;
            prefixList = new PrefixEntry(prefixList);
            for (; pe != null; pe = pe.prev) {
                if (pe.prefix == null) return;
                output.startPrefixMapping(pe.prefix, pe.ns);
            }
        }
        private void sendEndPrefixMapping() throws SAXException {
            for (PrefixEntry pe = prefixList; pe != null; pe = pe.prev) {
                if (pe.prefix == null) {
                    prefixList = pe.prev;
                    return;
                }
                prefixMapper.endPrefixMapping(pe.prefix);
                output.endPrefixMapping(pe.prefix);
            }
            prefixList = null;
        }
        
        private void notifyCompletion(boolean finished) {
            SAXReconstructor.this.notifyCompletion(this, finished);
        }
        
        private boolean active = true;
        private boolean finish;
        // Internal variable used for notifying the activation.
        private boolean activationFlag;
        private synchronized boolean block(boolean finished) throws SAXException {
            active = false; 
            notifyCompletion(finished);
            do {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                if (finish) {
                    throw new SAXException("Finished");
                }
            } while (!active);
            return activationFlag;
        }

        synchronized void notifyActivation(boolean flag) {
            this.active = true;
            this.activationFlag = flag;
            notify();
        }

        synchronized void notifyFinish() {
            this.finish = true;
            notify();
        }


        private ActionList actionList;
        // accessed.
        public ActionList getActionList() {
            return actionList;
        }

        private String exposingASNID;
        // accessed.
        public String getExposingASNID() {
            return exposingASNID;
        }
        private ArrayList<Attr> asnAttributes;
        // accessed.
        public ArrayList<Attr> getASNAttributes() {
            return asnAttributes;
        }

        private ArrayList<Attr> waitASNAttributes(String asnID) throws SAXException {
            waitingASNID = asnID;
            if (!block(false)) return null;
            return resultASNAttributes;
        }

        private String lastID;
        private boolean isInSlotNode;
        private int secLevel;
        private ArrayList<Integer> secLevelStack = new ArrayList<Integer>();

        private void pushSecLevel() {
            secLevelStack.add(secLevel);
        }

        private void popSecLevel() {
            int idx = secLevelStack.size() - 1;
            secLevel = secLevelStack.get(idx);
            secLevelStack.remove(idx);
        }

        private void setASNAttributes(Attributes attrs) {
            String asnID = attrs.getValue(NVDLConst.INSTANCE_REC_NS,
                                         NVDLConst.ASN_ID_ATTR);
            if (asnID == null) return;
            this.exposingASNID = asnID;
            asnAttributes = new ArrayList<Attr>();
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                String ns = attrs.getURI(i);
                // Skip attributes derived from the NVDL instance namespaces.
                if (ns.startsWith(NVDLConst.INSTANCE_NS_COMMON)) continue;
                String qName = attrs.getQName(i);
                String value = attrs.getValue(i);
                Attr a = new Attr(ns, qName, value);
                asnAttributes.add(a); 
            }
        }

        private void emitStartElement(NVDLElement base,
                                      String uri,
                                      String localName,
                                      String qName,
                                      Attributes attrs) throws SAXException {
            String asnID = attrs.getValue(NVDLConst.INSTANCE_REC_NS,
                                          NVDLConst.ASN_ID_ATTR);
            if (asnID != null) {
                ArrayList<Attr> as = waitASNAttributes(asnID);
                NVDLAttributes newAttrs = new NVDLAttributes(base, attrs);
                Iterator<Attr> it = as.iterator();
                while (it.hasNext()) {
                    Attr a = it.next();
                    PrefixReturnVal prv = prefixMapper.uniquePrefix(a.prefix, a.ns);
                    if (prv.requireDecl) {
                        storePrefixMapping(prv.prefix, a.ns);
                    }
                    String qNameA = prv.prefix + ":" + a.localName;
                    newAttrs.addExtAttribute(a.ns, a.localName, qNameA, a.value);
                }
                attrs = newAttrs;
            }
            sendStartPrefixMapping();
            output.startElement(uri, localName, qName, attrs);
        }

        private void skippingElement() throws SAXException {
            if (skipToNextElement) {
                this.actionList = null;
                block(false);
                skipToNextElement = false;
            }
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (!active) return;
            if (uri.equals(NVDLConst.INSTANCE_REC_NS)) {
                if (NVDLConst.SLOT_NODE_START_NAME.equals(localName)) {
                    String id = attrs.getValue("", NVDLConst.SLOT_NODE_ID_ATTR);
                    if (id != null) {
                        lastID = id;
                    }
                    isInSlotNode = true;
                }
            } else if (NVDLConst.INSTANCE_NS.equals(uri)
                       && NVDLConst.VIRTUALELEMENT_NAME.equals(localName)) {
                setASNAttributes(attrs);
                active = false;
                notifyCompletion(false);
            } else {
                skippingElement();
                String id = attrs.getValue(NVDLConst.INSTANCE_REC_NS,
                                           NVDLConst.SECTION_ID_ATTR);
                if (id != null) {
                    try {
                        this.actionList = new ActionList(rules, id);
                    } catch (InvalidIdException e) {
                        throw new SAXException(e);
                    }
                    setupNextElement(uri, localName);
                    if (block(false)) {
                        requestActionList = null;
                        currentElement = retrieveNextElement();
                        emitStartElement(currentElement, uri, localName, qName, attrs);
                        // System.err.println("Output: " + qName);
                        pushSecLevel();
                        secLevel = 0;
                        pda.startElement(currentElement);
                    }
                    return;
                } else {
                    currentElement = createNVDLElement(uri, localName);
                    emitStartElement(currentElement, uri, localName, qName, attrs);
                    // System.err.println("Output: " + qName);
                    secLevel++;
                    return;
                }
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (!active) return;
            if (uri.equals(NVDLConst.INSTANCE_REC_NS)) {
                if (NVDLConst.SLOT_NODE_START_NAME.equals(localName)) {
                    skippingElement();
                    try {
                        requestActionList = new ActionList(rules, lastID);
                    } catch (InvalidIdException e) {
                        throw new SAXException(e);
                    }
                    skipToNextElement = true;
                    skippingElement();
                }
            } else {
                skippingElement();
                output.endElement(uri, localName, qName);
                sendEndPrefixMapping();
                if (secLevel == 0) {
                    // System.err.println("EndElement: " + qName);
                    pda.endElement();
                    currentElement = currentElement.parent;
                    popSecLevel();
                    block(true);
                } else {
                    currentElement = currentElement.parent;
                    secLevel--;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (!active) return;
            if (skipToNextElement) return;
            output.characters(ch, start, length);
        }
        public void ignorableWhitespace(char[] ch,
                                        int start,
                                        int length) throws SAXException {
            if (!active) return;
            if (skipToNextElement) return;
            output.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException {
            if (active) {
                notifyCompletion(true);
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String ns) throws SAXException {
            storePrefixMapping(prefix, ns);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
    }

    public ContentHandler requestInput() {
        Input input = new Input();
        inputs.add(input);
        return input;
    }

    public void setOutput(ContentHandler output) {
        this.output = output;
    }

    public void start() throws NVDLReconstructionException, SAXException {
        output.startDocument();
        waitForInputReady();
        startWorkerManage();
        output.endDocument();
    }

    public SAXReconstructor(NVDLRules rules) {
        this.rules = rules;
        this.pda = new PDA(rules);
    }
}

