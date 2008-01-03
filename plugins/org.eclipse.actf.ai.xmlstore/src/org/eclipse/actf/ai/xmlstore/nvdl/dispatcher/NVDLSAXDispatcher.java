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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.validation.Schema;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.Interpretation;
import org.eclipse.actf.ai.xmlstore.nvdl.fm.PDA;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAllowAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLConst;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLElement;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRejectAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLValidateAction;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * The <code>NVDLSAXDispatcher</code> is a dispatcher for SAX interface.
 */
public class NVDLSAXDispatcher {
    private NVDLRules rules;
    private PDA pda;

    private final boolean emitSectionID;

    private PrefixMapper prefixMapper = new PrefixMapper();

    // NVDLElement -> List of ActiveHandlers
    private Map<NVDLElement, List<ActiveHandler>> activeHandlersMap = new HashMap<NVDLElement, List<ActiveHandler>>();
    private Set<ActiveHandler> activeHandlers = new HashSet<ActiveHandler>();

    private int errorCounter = 0;

    public void reset() {
        pda.reset();
        prefixMapper.reset();
        activeHandlersMap.clear();
        activeHandlers.clear();
        errorCounter = 0;
        currentElement = null;
        nextSectionIdx = 0;

        idMap.clear();
        asnIDCounter = 0;
    }

    private XMLReader reader;

    public int getErrorCount() {
        return errorCounter;
    }

    private void incrementErrorCounter() {
        errorCounter++;
    }

    private void sendVirtualElement(ContentHandler ch,
                                    String instancePrefix,
                                    String qName,
                                    Attributes attrs,
                                    boolean requireDecl)
        throws SAXException {
        Object effectiveMapping = prefixMapper.startEffectivePrefixMappings(ch);
        if (requireDecl) {
            ch.startPrefixMapping(instancePrefix, NVDLConst.INSTANCE_NS);
        }
        ch.startElement(NVDLConst.INSTANCE_NS,
                        NVDLConst.VIRTUALELEMENT_NAME,
                        qName, attrs);
        ch.endElement(NVDLConst.INSTANCE_NS,
                      NVDLConst.VIRTUALELEMENT_NAME,
                      qName);
        if (requireDecl) {
            ch.endPrefixMapping(instancePrefix);
        }
        prefixMapper.endEffectivePrefixMappings(effectiveMapping, ch);
    }

    private NVDLAttributes addASNIDAttribute(Interpretation ip,
                                             ContentHandler h,
                                             NVDLAttributes attr,
                                             String asnID)
    	throws SAXException {
        if (h == null) return attr;
        PrefixMapper.PrefixReturnVal prv = prefixMapper.uniquePrefix(NVDLConst.INSTANCE_REC_PREFIX_BASE,
                                                                     NVDLConst.INSTANCE_REC_NS);
        String qName = prv.prefix + ":" + NVDLConst.ASN_ID_ATTR;
            
        if (prv.requireDecl) {
            h.startPrefixMapping(prv.prefix, NVDLConst.INSTANCE_REC_NS);
            prefixMapper.startPrefixMapping(prv.prefix, NVDLConst.INSTANCE_REC_NS);
            ip.setPrefix(prv.prefix);
        }
        attr.addExtAttribute(NVDLConst.INSTANCE_REC_NS,
                             NVDLConst.ASN_ID_ATTR,
                             qName, asnID);
        return attr;
        
    }

    private void dispatchAttributeSection(List<Interpretation> ips,
                                          NVDLAttributes attrs,
                                          String asnID)
        throws SAXException {
        Iterator<Interpretation> it = ips.iterator();
        while (it.hasNext()) {
            Interpretation ip = it.next();
            if (!ip.isDispatch()) continue;

            InterpretationSlot slot = setupInterpretationSlot(ip, currentElement, true);
            ContentHandler h = slot.contentHandler;
            ContentHandler dh = slot.contentHandlerForDebug;

            PrefixMapper.PrefixReturnVal prv;
            prv = prefixMapper.uniquePrefix(NVDLConst.INSTANCE_PREFIX_BASE,
                                            NVDLConst.INSTANCE_NS);
            String qName = prv.prefix + ":" + NVDLConst.VIRTUALELEMENT_NAME;
            sendVirtualElement(h, prv.prefix, qName, attrs, prv.requireDecl);
            if (dh != null) {
                if (emitSectionID) {
                    attrs = addASNIDAttribute(ip, dh, attrs, asnID);
                }
                sendVirtualElement(dh, prv.prefix, qName, attrs, prv.requireDecl);
            }
        }
    }

    private NVDLAttributes dispatchAttribute(Interpretation ip,
                                             ContentHandler h,
                                             Attributes attrs)
    	throws SAXException {
        Map<String, NVDLAttributes> attrsMap = new HashMap<String, NVDLAttributes>();
        int len = attrs.getLength();
        NVDLAttributes restAttributes = new NVDLAttributes(currentElement, attrs);
        for (int i = 0; i < len; i++) {
            String ns = attrs.getURI(i);
            NVDLAttributes pa = attrsMap.get(ns);
            if (pa == null) {
                pa = new NVDLAttributes(currentElement, attrs, ns);
                attrsMap.put(ns, pa);
            }
            pa.addAttribute(i);
        }

        Iterator<NVDLAttributes> it = attrsMap.values().iterator();
        boolean isASNIDRequired = false;
        String asnID = generateASNID();
        while (it.hasNext()) {
            NVDLAttributes pa = it.next();
            List<Interpretation> ipAttrs = pda.getAttributeInterpretation(ip, pa);
            if (pda.isAttributeAttached()) {
                restAttributes.addAttributes(pa);
            } else {
                isASNIDRequired = emitSectionID;
            }
            dispatchAttributeSection(ipAttrs, pa, asnID);
        }
        if (isASNIDRequired) {
            restAttributes = addASNIDAttribute(ip, h, restAttributes, asnID);
        }
        
        return restAttributes;
    }

    private class InterpretationSlot implements ErrorHandler {
        ContentHandler contentHandler;
        ContentHandler contentHandlerForDebug;

        String definedPrefix;
        final Interpretation ip;

        /********************************************************************************
                      Error Handler Proxy
         ********************************************************************************/
        private final ErrorHandler errorHandler;

        private SAXParseException encapsulateException(SAXParseException e) {
            NVDLDispatcherException de = new NVDLDispatcherException(e.getException(),
                                                                     ip.getAction());
            return new SAXParseException(e.getMessage(),
                                         e.getPublicId(),
                                         e.getSystemId(),
                                         e.getLineNumber(),
                                         e.getColumnNumber(), de);
        }
        
        public void warning(SAXParseException e)
            throws SAXException {
            if (errorHandler != null) 
                errorHandler.warning(encapsulateException(e));
        }

        public void error(SAXParseException e)
            throws SAXException {
            incrementErrorCounter();
            if (errorHandler != null) 
                errorHandler.error(encapsulateException(e));
        }

        public void fatalError(SAXParseException e)
            throws SAXException {
            incrementErrorCounter();
            if (errorHandler != null) 
                errorHandler.fatalError(encapsulateException(e));
        }

        InterpretationSlot(Interpretation ip,
                           ContentHandler contentHandler,
                           ContentHandler contentHandlerForDebug) {
            this.ip = ip;
            this.contentHandler = contentHandler;
            this.contentHandlerForDebug = contentHandlerForDebug;
            errorHandler = validatorHandler.getErrorHandler();
            if (contentHandler instanceof ValidatorHandler) {
                ((ValidatorHandler) contentHandler).setErrorHandler(this);
            }
        }
    }

    private static class IDMapVal {
        int sectionID;
        int slotNodeID;
    }

    private int asnIDCounter;
    private String generateASNID() {
        return Integer.toString(asnIDCounter++);
    }

    private HashMap<String, IDMapVal> idMap = new HashMap<String, IDMapVal>();

    private String generateID(Interpretation ip, boolean sectionID, boolean increment) {
        String id = ip.getID();
        String idForNum;
        if (sectionID) {
            idForNum = ip.getPrevID();
        } else {
            idForNum = id;
        }
        // System.err.println("!"+id+" / "+idForNum);
        IDMapVal idVal = idMap.get(idForNum);
        int idNum;
        if (idVal == null) {
            idNum = 0;
            idMap.put(idForNum, new IDMapVal());
        } else {
            idNum = idVal.slotNodeID;
            if (!sectionID && increment) {
                idVal.slotNodeID++;
            }
        }
        if (idNum == 0) {
            return id;
        } else {
            return id + " " + idNum;
        }
    }

    private InterpretationSlot setupInterpretationSlot(Interpretation ip,
                                                       NVDLElement e,
                                                       boolean forAttribute)
        throws SAXException {
        InterpretationSlot slot = (InterpretationSlot) ip.getSlot();
        if (slot != null) return slot;

        NVDLAction a = ip.getAction();
        ContentHandler contentHandler = makeContentHandler(a, forAttribute);
        ContentHandler contentHandlerForDebug = setupContentHandlerForDebug(generateID(ip, true, false), a);
        if (contentHandler != null) {
            startActiveHandler(contentHandler, e);
        }
        if (contentHandlerForDebug != null) {
            startActiveHandler(contentHandlerForDebug, e);
        }
        slot = new InterpretationSlot(ip, contentHandler, contentHandlerForDebug);
        ip.setSlot(slot);

        return slot;
    }

    private InterpretationSlot getInterpretationSlot(Interpretation ip) {
        return (InterpretationSlot) ip.getSlot();
    }

    // --------------------------------------------------------------------------------
    // TODO: Debug SAX Handler
    // --------------------------------------------------------------------------------
    public interface DebugHandlerFactory {
        ContentHandler createContentHandler(String id, NVDLAction action) throws SAXException;
        void nextActionHandler(NVDLAction action, Locator locator);
    }
    DebugHandlerFactory debugHandlerFactory;
    public void setDebugHandlerFactory(DebugHandlerFactory f) {
        debugHandlerFactory = f;
    }

    private ContentHandler setupContentHandlerForDebug(String id, NVDLAction a) throws SAXException {
        if (debugHandlerFactory == null) return null;
        debugHandlerFactory.nextActionHandler(a, validatorHandler.locator);
        ContentHandler h = debugHandlerFactory.createContentHandler(id, a);
        if (h == null) return null;
        if (validatorHandler.locator != null) {
            h.setDocumentLocator(validatorHandler.locator);
        }
        return h;
    }
    // --------------------------------------------------------------------------------

    private int nextSectionIdx;
    private NVDLElement currentElement;

    private NVDLElement createNVDLElement(String ns, String localName, NVDLElement parent) {
        NVDLElement e = new NVDLElement(ns, localName, parent,
                                        rules.getTriggerManager(),
                                        nextSectionIdx);
        if (e.isSectionHead()) nextSectionIdx++;
        return e;
    }

    static class ActiveHandler {
        ContentHandler handler;
        NVDLElement rootElement;
        Object effectiveMapping;
    }

    private ActiveHandler startActiveHandler(ContentHandler h,
                                             NVDLElement e) 
        throws SAXException {
        ActiveHandler ah = new ActiveHandler();
        ah.handler = h;
        ah.rootElement = e;
        h.startDocument();
        ah.effectiveMapping = prefixMapper.startEffectivePrefixMappings(h);
        activeHandlers.add(ah);
        List<ActiveHandler> ahs = activeHandlersMap.get(e);
        if (ahs == null) {
            ahs = new ArrayList<ActiveHandler>();
            activeHandlersMap.put(e, ahs);
        }
        ahs.add(ah);
        return ah;
    }

    private void endActiveHandlers(NVDLElement e) throws SAXException {
        List<ActiveHandler> ahs = activeHandlersMap.get(e);
        if (ahs == null) return;
        Iterator<ActiveHandler> it = ahs.iterator();
        while (it.hasNext()) {
            ActiveHandler ah = it.next();
            prefixMapper.endEffectivePrefixMappings(ah.effectiveMapping, ah.handler);
            ah.handler.endDocument();
            activeHandlers.remove(ah);
        }
        activeHandlersMap.put(e, null);
    }


    private ContentHandler makeContentHandlerForValidateAction(NVDLValidateAction validateAction,
                                                               boolean forAttribute)
        throws SAXException {
        Schema schema;
        ValidatorHandler h;
        try {
            schema = validateAction.getSchema(forAttribute);
        } catch (NVDLException e) {
            if (e.getException() instanceof SAXException) {
                throw (SAXException) e.getException();
            }
            throw new SAXException(e);
        }
        h = schema.newValidatorHandler();
        // I think it's a bug of isorelax JARV bridge.
        h.setContentHandler(null);
        return h;
    }

    private ContentHandler makeContentHandler(NVDLAction a,
                                              boolean forAttribute) throws SAXException {
        ContentHandler contentHandler;
        if (a instanceof NVDLValidateAction) {
            NVDLValidateAction va = (NVDLValidateAction) a;
            contentHandler = makeContentHandlerForValidateAction(va, forAttribute);
        } else if (a instanceof NVDLAllowAction) {
            contentHandler = new AllowValidatorHandler();
        } else if (a instanceof NVDLRejectAction) {
            contentHandler = new RejectValidatorHandler();
        } else {
            return null;
        }
        // TODO: redesign.
        if (validatorHandler.locator != null) {
            contentHandler.setDocumentLocator(validatorHandler.locator);
        }
        return contentHandler;
    }

    private class NVDLValidatorHandler extends ValidatorHandler {
        private ContentHandler contentHandler = null;
        public void setContentHandler(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }
        public ContentHandler getContentHandler() {
            return contentHandler;
        }

        private ErrorHandler errorHandler = null;
        public void setErrorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
        }
        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }

        private LSResourceResolver resourceResolver = null;
        public void setResourceResolver(LSResourceResolver resourceResolver) {
            this.resourceResolver = resourceResolver;
        }
        public LSResourceResolver getResourceResolver() {
            return resourceResolver;
        }

        public TypeInfoProvider getTypeInfoProvider() {
            // TODO: we should appropriately forward type info requests.
            return null;
        }

        Locator locator;
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
            Iterator<ActiveHandler> it = activeHandlers.iterator();
            while (it.hasNext()) {
                ActiveHandler ah = it.next();
                ah.handler.setDocumentLocator(locator);
            }
        }

        public void startDocument() throws SAXException {
            // Do nothing.
        }

        public void endDocument() throws SAXException {
            Iterator<ActiveHandler> it = activeHandlers.iterator();
            while (it.hasNext()) {
                ActiveHandler ah = it.next();
                Log.error("All handlers must be deactivated." + ah);
                ah.handler.endDocument();
            }
            activeHandlers.clear();
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
            prefixMapper.startPrefixMapping(prefix, uri);
            Iterator<ActiveHandler> it = activeHandlers.iterator();
            while (it.hasNext()) {
                ActiveHandler ah = it.next();
                ah.handler.startPrefixMapping(prefix, uri);
            }
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            prefixMapper.endPrefixMapping(prefix);
            Iterator<ActiveHandler> it = activeHandlers.iterator();
            while (it.hasNext()) {
                ActiveHandler ah = it.next();
                ah.handler.endPrefixMapping(prefix);
            }
        }

        private void dispatchPlaceHolder(ContentHandler h,
                                         String uri,
                                         String localName) throws SAXException {
            
            Attributes ats = new NVDLPlaceHolderAttributes(uri, localName);
            prefixMapper.sendEmptyElement(h,
                                          NVDLConst.INSTANCE_NS,
                                          NVDLConst.PLACEHOLDER_NAME,
                                          NVDLConst.INSTANCE_PREFIX_BASE,
                                          ats);
        }

        private void dispatchElementSlotNodeStart(Interpretation ip,
                                                  ContentHandler h) throws SAXException {
            Attributes ats = new NVDLSlotNodeAttributes(NVDLConst.SLOT_NODE_ID_ATTR,
                                                        generateID(ip, false, false));
            prefixMapper.sendEmptyElement(h,
                                          NVDLConst.INSTANCE_REC_NS,
                                          NVDLConst.SLOT_NODE_START_NAME,
                                          NVDLConst.INSTANCE_REC_PREFIX_BASE,
                                          ats);
        }

        private void dispatchElementSlotNodeEnd(Interpretation ip,
                                                ContentHandler h) throws SAXException {
            Attributes ats = new NVDLSlotNodeAttributes(NVDLConst.SLOT_NODE_ID_ATTR,
                                                        generateID(ip, false, true));
            prefixMapper.sendEmptyElement(h,
                                          NVDLConst.INSTANCE_REC_NS,
                                          NVDLConst.SLOT_NODE_END_NAME,
                                          NVDLConst.INSTANCE_REC_PREFIX_BASE,
                                          ats);
        }

        private void addSectionIDAttribute(Interpretation ip,
                                           ContentHandler h,
                                           NVDLAttributes attr) throws SAXException {
            if (h == null) return;
            PrefixMapper.PrefixReturnVal prv = prefixMapper.uniquePrefix(NVDLConst.INSTANCE_REC_PREFIX_BASE,
                                                                         NVDLConst.INSTANCE_REC_NS);
            String qName = prv.prefix + ":" + NVDLConst.SECTION_ID_ATTR;
            
            if (prv.requireDecl) {
                h.startPrefixMapping(prv.prefix, NVDLConst.INSTANCE_REC_NS);
                prefixMapper.startPrefixMapping(prv.prefix, NVDLConst.INSTANCE_REC_NS);
                ip.setPrefix(prv.prefix);
            }
            attr.addExtAttribute(NVDLConst.INSTANCE_REC_NS,
                                 NVDLConst.SECTION_ID_ATTR,
                                 qName,
                                 generateID(ip, true, true));
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            currentElement = createNVDLElement(uri, localName, currentElement);
            if (currentElement.isSectionHead()) {
                List prevIps = pda.getCurrentInterpretations();
                Iterator prevIt = prevIps.iterator();
                while (prevIt.hasNext()) {
                    Interpretation ip = (Interpretation) prevIt.next();
                    // Interpretation ipHead = ip.getSectionHeadInterpretation();
                    Interpretation ipHead = ip.getEffectiveInterpretation();
                    if (ipHead == null) continue;
                    if (!ipHead.isDispatch()) continue;
                    InterpretationSlot slot = setupInterpretationSlot(ipHead, currentElement, false);
                    ContentHandler dh = slot.contentHandlerForDebug;
                    if (dh == null) continue;
                    dispatchElementSlotNodeStart(ip, dh);
                }
            }
            pda.startElement(currentElement);
            List ips = pda.getCurrentInterpretations();
            Iterator it = ips.iterator();

            while (it.hasNext()) {
                Interpretation ip = (Interpretation) it.next();
                Interpretation ipDispatch = ip.getEffectiveInterpretation();
                if (ipDispatch == null) continue;
                InterpretationSlot slot = setupInterpretationSlot(ipDispatch, currentElement, false);
                
                ContentHandler h = slot.contentHandler;
                ContentHandler dh = slot.contentHandlerForDebug;
                if (ip.isAttachPlaceHolder()) {
                    if (currentElement.isSectionHead()) {
                        dispatchPlaceHolder(h, uri, localName);
                        if (dh != null) {
                            dispatchPlaceHolder(dh, uri, localName);
                        }
                    }
                } else {
                    NVDLAttributes rest = dispatchAttribute(ip, dh, attrs);
                    h.startElement(uri, localName, qName, rest);
                    if (dh != null) {
                        if (emitSectionID && currentElement.isSectionHead()) {
                            addSectionIDAttribute(ip, dh, rest);
                        }
                        dh.startElement(uri, localName, qName, rest);
                    }
                }
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            List ips = pda.getCurrentInterpretations();
            Iterator it = ips.iterator();
            if (currentElement == null) {
                Log.error("Something wrong happens.  Some tags are not balanced.");
                return;
            }

            while (it.hasNext()) {
                Interpretation ip = (Interpretation) it.next();
                if (ip.isAttachPlaceHolder()) continue;
                Interpretation ipDispatch = ip.getEffectiveInterpretation();
                if (ipDispatch == null) continue;
                InterpretationSlot slot = setupInterpretationSlot(ipDispatch, currentElement, false);

                ContentHandler h = slot.contentHandler;
                ContentHandler dh = slot.contentHandlerForDebug;
                h.endElement(uri, localName, qName);
                if (dh != null) {
                    dh.endElement(uri, localName, qName);
                    if (ip.getPrefix() != null) {
                        prefixMapper.endPrefixMapping(ip.getPrefix());
                        dh.endPrefixMapping(ip.getPrefix());
                        ip.setPrefix(null);
                    }
                }
            }
            endActiveHandlers(currentElement);
            pda.endElement();

            if (currentElement.isSectionHead()) {
                List prevIps = pda.getCurrentInterpretations();
                Iterator prevIt = prevIps.iterator();
                while (prevIt.hasNext()) {
                    Interpretation ip = (Interpretation) prevIt.next();
                    Interpretation ipHead = ip.getEffectiveInterpretation();
                    if (ipHead == null) continue;
                    if (!ipHead.isDispatch()) continue;
                    InterpretationSlot slot = setupInterpretationSlot(ipHead, currentElement, false);
                    ContentHandler dh = slot.contentHandlerForDebug;
                    if (dh == null) continue;
                    dispatchElementSlotNodeEnd(ip, dh);
                }
            }

            currentElement = currentElement.parent;
        }

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            List ips = pda.getCurrentInterpretations();
            Iterator it = ips.iterator();

            while (it.hasNext()) {
                Interpretation ip = (Interpretation) it.next();
                if (ip.isAttachPlaceHolder()) continue;
                Interpretation ipDispatch = ip.getEffectiveInterpretation();
                if (ipDispatch == null) continue;
                InterpretationSlot slot = getInterpretationSlot(ipDispatch);

                ContentHandler h = slot.contentHandler;
                ContentHandler dh = slot.contentHandlerForDebug;
                h.characters(ch, start, length);
                if (dh != null) {
                    dh.characters(ch, start, length);
                }
            }
        }

        public void ignorableWhitespace(char[] ch,
                                        int start,
                                        int length) throws SAXException {
            List ips = pda.getCurrentInterpretations();
            Iterator it = ips.iterator();

            while (it.hasNext()) {
                Interpretation ip = (Interpretation) it.next();
                if (ip.isAttachPlaceHolder()) continue;
                Interpretation ipDispatch = ip.getEffectiveInterpretation();
                if (ipDispatch == null) continue;
                InterpretationSlot slot = getInterpretationSlot(ipDispatch);

                ContentHandler h = slot.contentHandler;
                ContentHandler dh = slot.contentHandlerForDebug;
                h.ignorableWhitespace(ch, start, length);
                if (dh != null) {
                    dh.ignorableWhitespace(ch, start, length);
                }
            }
        }

        public void processingInstruction(String target,
                                          String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
            // Currently do not dispatch.
        }
        
    }

    private final NVDLValidatorHandler validatorHandler = new NVDLValidatorHandler();

    public ValidatorHandler getValidatorHandler() {
        return validatorHandler;
    }

    public boolean validate(InputSource is, ErrorHandler eh)
    	throws SAXException, IOException {
        ValidatorHandler h = getValidatorHandler();
        h.setErrorHandler(eh);
        reader.setErrorHandler(eh);
        reader.parse(is);
        if (getErrorCount() > 0) return false;
        return true;
    }

    public NVDLSAXDispatcher(NVDLRules rules, boolean emitSectionID) throws SAXException {
        this.rules = rules;
        this.pda = new PDA(rules);
        this.emitSectionID = emitSectionID;
        reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        ValidatorHandler h = getValidatorHandler();
        reader.setContentHandler(h);
    }
}
