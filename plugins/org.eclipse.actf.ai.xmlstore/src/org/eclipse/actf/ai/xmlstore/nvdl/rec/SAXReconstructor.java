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

import org.eclipse.actf.ai.xmlstore.nvdl.fm.ActionList;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.PDA;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.ActionList.InvalidIdException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLConst;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLElement;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
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

    private NVDLElement createNVDLElement(String ns, String localName) {
        return new NVDLElement(ns, localName,
                               currentElement,
                               rules.getTriggerManager(),
                               0);
    }

    private Input responsedInput;

    private boolean finished;

    private HashMap<ActionList, Integer> actionListCountMap = new HashMap<ActionList, Integer>();

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

            ActionList al = input.getActionList();
            if (al == null) continue;

            if (requestActionList != null) {
                if (requestActionList.getCount() != al.getCount()) continue;
            }

            Integer countInt = actionListCountMap.get(al);
            int count;
            if (countInt == null) {
                if (al.getCount() != 0) continue;
                count = 1;
            } else {
                count = countInt.intValue();
                if (count != al.getCount()) continue;
                count++;
            }
            
            PDA.MatchResult mr = pda.matchActionList(input.nextElement, al);
            if (mr == PDA.MatchResult.MATCH) {
                int cscore = al.getPrecedence();
                if (score < cscore) {
                    actionListCountMap.put(al, count);
                    selectedInput = input;
                    score = cscore;
                    // TODO
                    break;
                }
            }
        }
        return selectedInput;
    }

    private ArrayList<Input> activeInputList = new ArrayList<Input>();

    private synchronized void startWorkerManage() throws NVDLReconstructionException {
        for (;;) {
            Input input;
            if (finished) {
                int idx = activeInputList.size();
                if (idx == 1) {
                    return;
                } else {
                    idx--;
                    activeInputList.remove(idx);
                    idx--;
                    input = activeInputList.get(idx);
                }
            } else {
                input = selectInput();
                activeInputList.add(input);
            }
            if (input == null) {
                throw new NVDLReconstructionException("No Reconstruction Candidate.");
            }

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

    private class Input extends DefaultHandler {
        private boolean active = true;
        private boolean finish;
        private boolean activationFlag;
        private boolean skipToNextElement;

        private NVDLElement nextElement;
        
        private void notifyCompletion(boolean finished) {
            SAXReconstructor.this.notifyCompletion(this, finished);
        }
        
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

        ActionList getActionList() {
            return actionList;
        }

        private String lastID;
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
                }
                return;
            } else {
                skipToNextElement = false;
                String id = attrs.getValue(NVDLConst.INSTANCE_REC_NS,
                                           NVDLConst.SECTION_ID_ATTR);
                if (id != null) {
                    try {
                        this.actionList = new ActionList(rules, id);
                    } catch (InvalidIdException e) {
                        throw new SAXException(e);
                    }
                    nextElement = createNVDLElement(uri, localName);
                    if (block(false)) {
                        currentElement = createNVDLElement(uri, localName);
                        requestActionList = null;
                        output.startElement(uri, localName, qName, attrs);
                        // System.err.println("Output: " + qName);
                        pushSecLevel();
                        secLevel = 0;
                        pda.startElement(currentElement);
                    }
                } else {
                    output.startElement(uri, localName, qName, attrs);
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
                    try {
                        requestActionList = new ActionList(rules, lastID);
                    } catch (InvalidIdException e) {
                        throw new SAXException(e);
                    }
                    skipToNextElement = true;
                }
            } else {
                if (skipToNextElement) {
                    this.actionList = null;
                    block(false);
                    skipToNextElement = false;
                }
                if (secLevel == 0) {
                    output.endElement(uri, localName, qName);
                    // System.err.println("EndElement: " + qName);
                    pda.endElement();
                    currentElement = currentElement.parent;
                    popSecLevel();
                    block(true);
                } else {
                    output.endElement(uri, localName, qName);
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
            output.startPrefixMapping(prefix, ns);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            output.endPrefixMapping(prefix);
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

