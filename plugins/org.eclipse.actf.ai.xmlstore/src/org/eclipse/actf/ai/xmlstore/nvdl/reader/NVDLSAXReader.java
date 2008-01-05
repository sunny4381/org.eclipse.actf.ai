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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.eclipse.actf.ai.xmlstore.nvdl.NVDLException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.Location;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAllowAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLConst;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMessage;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLMode;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModel;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLModelException;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRejectAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLResultAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRule;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLValidateAction;
import org.eclipse.actf.ai.xmlstore.nvdl.util.DefaultErrorHandler;
import org.eclipse.actf.ai.xmlstore.nvdl.util.Log;
import org.eclipse.actf.ai.xmlstore.nvdl.util.PrefixMapper;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * The <code>NVDLSAXReader</code> is a reader for NVDL scripts using SAX.
 */
public class NVDLSAXReader {
    private NVDLRules rules;

    private ErrorHandler eh;

    private Locator locator;

    private int errorCounter;

    private Location newLocation() {
        return new Location(locator.getLineNumber(),
                            locator.getColumnNumber(),
                            locator.getSystemId());
    }

    private void setLocation(NVDLModel m) {
        m.setLocation(newLocation());
    }

    private XMLReader reader;

    private void error(NVDLReaderException e) throws SAXException {
        if (eh != null)
            eh.error(new SAXParseException(e.getMessage(), locator, e));
    }

    private void readUnknownError(String mes, Object[] args) throws SAXException {
        errorCounter++;
        error(new NVDLReaderException(mes, args));
    }

    private void readInvalidError(String mes) throws SAXException {
        readInvalidError(mes, new Object[0]);
    }

    private void readInvalidError(String mes, Object[] args) throws SAXException {
        errorCounter++;
        error(new NVDLReaderException(mes, args));
    }

    private boolean isForeign(String iri) {
        return !NVDLConst.NVDL_NS.equals(iri);
    }

    private boolean isLocal(String iri) {
        return (iri.length() == 0);
    }

    private static final String NSNAME = "xmlns";
    private boolean isNSDecl(String qName) {
        if (!qName.startsWith(NSNAME)) return false;
        if (qName.length() == NSNAME.length()) return true;
        if (qName.charAt(NSNAME.length()) == ':') return true;
        return false;
    }

    private void setHandler(BaseHandler h) {
        reader.setContentHandler(h);
    }

    /*
      Whitespace
     */
    private StringBuffer removeLeadingAndTrailingSpaces(StringBuffer sb) {
        int len = sb.length();
        int s, e;
        ex1: for (s = 0; s < len; s++) {
            char c = sb.charAt(s);
            switch (c) {
            case ' ': case '\r': case '\n': case '\t':
                break;
            default:
                break ex1;
            }
        }
        if (s == (len - 1)) {
            sb.delete(0, s);
            return sb;
        }
        ex2: for (e = len - 1; s >= 0; s--) {
            char c = sb.charAt(e);
            switch (c) {
            case ' ': case '\r': case '\n': case '\t':
                break;
            default:
                break ex2;
            }
        }

        if (s > 0) {
            sb.delete(0, s);
        }
        if (e < (len - 1)) {
            sb.delete(e, len - 1);
        }
        return sb;
    }

    private String[] splitList(String s) {
        return s.split("[ \r\n\t]");
    }

    private boolean checkWhiteSpace(char[] ch, int start, int length) {
        for (int i = 0; i < length; i++) {
            switch (ch[start + i]) {
            case ' ': case '\r': case '\n':	case '\t':
                break;
            default:
                return false;
            }
        }
        return true;
    }

    /*
      xsd:boolean
     */

    private boolean parseXSDBoolean(String s, boolean defaultValue)
        throws SAXException {
        if (s == null) return defaultValue;
        StringBuffer sb = removeLeadingAndTrailingSpaces(new StringBuffer(s));
        String s2 = sb.toString();
        if (s2.equals("0")) return false;
        if (s2.equals("false")) return false;
        if (s2.equals("1")) return true;
        if (s2.equals("true")) return true;
        readInvalidError("NVDLReader.XSDBooleanError", new Object[] {s});
        return false;
    }

    /*
      Attribute
     */
    static class AtRet {
        String name;
        String value;
        AtRet(String name) {
            this.name = name;
            this.value = null;
        }
    }

    private void getAttrs(Attributes attr, AtRet[] ars) throws SAXException {
        int len = attr.getLength();
        for (int i = 0; i < len; i++) {
            String iri = attr.getURI(i);
            if (!isLocal(iri)) continue;
            String localName = attr.getLocalName(i);
            String qName = attr.getQName(i);
            if (isNSDecl(qName)) continue;
            for (int j = 0; ; j++) {
                if (j >= ars.length) {
                    readUnknownError("NVDLReader.UnallowedAttributeError",
                                     new Object[] {localName});
                }
                if (ars[j].name.equals(localName)) {
                    ars[j].value = attr.getValue(i);
                    break;
                }
            }
        }
    }

    private void checkAttrs(Attributes attr) throws SAXException {
        int len = attr.getLength();
        for (int i = 0; i < len; i++) {
            String iri = attr.getURI(i);
            String localName = attr.getLocalName(i);
            if (isLocal(iri)) {
                readUnknownError("NVDLReader.UnallowedAttributeError",
                                 new Object[] {localName});
            }
        }
    }

    /*
      id
     */

    private String getXMLID(Attributes attrs) throws SAXException {
        return attrs.getValue("id");
    }

    /*
      Rule
    */
    private void addRule(NVDLMode mode, NVDLRule rule)
        throws SAXException {
        NVDLRule conflictedRule = mode.addRule(rule);
        if (conflictedRule != null) {
            readInvalidError("NVDLReader.RuleConflictsError",
                             new Object[] {rule.toString(), conflictedRule.toString()});
        }
    }

    /*
      Mode Helper
    */

    private void checkNestedMode(NVDLAction action) throws SAXException {
        if (action.getUseModeName() != null) {
            readInvalidError("NVDLReader.NestedModeAndUseModeError");
        }
        if (action.getUseMode() != null) {
            readInvalidError("NVDLReader.DuplicatedNestedModeError");
        }
    }

    private String uniqueModeName() {
        StringBuffer name = new StringBuffer("mode-");
        name.append(locator.getLineNumber());
        name.append("-");
        name.append(locator.getColumnNumber());
        name.append("-");
        String fname;
        for (int i = 1; ; i++) {
            fname = name.toString() + i;
            if (rules.getMode(fname) == null) break;
        }
        return fname;
    }

    private String uniqueActionID(Attributes attrs) throws SAXException {
        String id = getXMLID(attrs);
        if ((id == null) || (id.length() == 0)) {
            for (int i = 1; ; i++) {
                id = "A" + i;
                if (rules.getAction(id) == null) break;
            }
        }
        return id;
    }

    /*
       PrefixMapper
     */
    private PrefixMapper prefixMapper = new PrefixMapper();

    /* 
       Handlers
    */

    private class BaseHandler extends DefaultHandler {
        private final BaseHandler ph;

        public void startPrefixMapping(String prefix,
                                       String uri)
            throws SAXException {
            prefixMapper.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            prefixMapper.endPrefixMapping(prefix);
        }

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            readUnknownError("NVDLReader.UnallowedCharactersError",
                             new Object[] {new String(ch, start, length)});
        }

        public void setDocumentLocator(Locator locator) {
            NVDLSAXReader.this.locator = locator;
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            readUnknownError("NVDLReader.UnallowedElementError",
                             new Object[] {uri, qName});
        }
        public void endElement(String uri,
                               String localName,
                               String qName) throws SAXException {
            if (ph != null) setHandler(ph);
        }

        BaseHandler(BaseHandler prev) {
            this.ph = prev;
        }
    }

    private class ForeignSkipHandler extends BaseHandler {
        private int level;
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            return;
        }
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            level++;
        }
        public void endElement(String uri,
                               String localName,
                               String qName) throws SAXException {
            level--;
            if (level <= 0)
                super.endElement(uri, localName, qName);
        }
        ForeignSkipHandler(BaseHandler bh) {
            super(bh);
            level = 1;
        }
    }

    private class WSSkipHandler extends BaseHandler {
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (!checkWhiteSpace(ch, start, length)) {
                readUnknownError("NVDLReader.UnallowedCharactersError",
                                 new Object[] {new String(ch, start, length)});
            }
        }
        WSSkipHandler(BaseHandler bh) {
            super(bh);
        }
    }

    private class InitialHandler extends WSSkipHandler {
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)
                || !localName.equals("rules")) {
                readUnknownError("NVDLReader.DocumentElementIsRulesError",
                                 new Object[] {uri, qName});
                return;
            }
            AtRet[] ars = new AtRet[2];
            ars[0] = new AtRet("schemaType");
            ars[1] = new AtRet("startMode");
            getAttrs(attrs, ars);
            rules = new NVDLRules(ars[0].value, ars[1].value);
            setLocation(rules);
            setHandler(new RulesHandler(this));
        }
        InitialHandler() {
            super(null);
        }
    }

    private abstract class RuleHandler extends WSSkipHandler {
        private boolean targetElement = false;
        private boolean targetAttribute = false;
        private void parseMatchTarget(String mt) throws SAXException {
            if (mt.equals("elements")) {
                targetElement = true;
            } else if (mt.equals("attributes")) {
                targetAttribute = true;
            } else {
                readInvalidError("NVDLReader.InvalidMatchTargetError", new Object[] {mt});
            }
        }
        private NVDLRule createRuleModel(boolean any,
                                         String ns,
                                         char wildCard,
                                         String target)
            throws SAXException {
            targetElement = targetAttribute = false;
            if (target == null) {
                // Simplification 6.4.9
                targetElement = true;
            } else {
                String[] targets = splitList(target);
                if (targets.length == 0) {
                    // Simplification 6.4.9
                    targetElement = true;
                } else if (targets.length == 1) {
                    parseMatchTarget(targets[0]);
                } else if (targets.length == 2) {
                    parseMatchTarget(targets[0]);
                    parseMatchTarget(targets[1]);
                } else {
                    readInvalidError("NVDLReader.InvalidMatchTargetError", new Object[] {target});
                }
            }
            NVDLRule rule = new NVDLRule(any, ns, wildCard,
                                         targetElement, targetAttribute);
            setLocation(rule);
            return rule;
        }

        protected NVDLRule parseRule(String localName,
                                     Attributes attrs) throws SAXException {
            if (localName.equals("namespace")) {
                AtRet[] ars = new AtRet[3];
                ars[0] = new AtRet("ns");
                ars[1] = new AtRet("wildCard");
                ars[2] = new AtRet("match");
                getAttrs(attrs, ars);
                // Simplification 6.4.9
                char wildCardChar = '*';
                if (ars[1].value == null) {
                } else if (ars[1].value.length() == 0) {
                    wildCardChar = '\u0000';
                } else if (ars[1].value.length() == 1) {
                    wildCardChar = ars[1].value.charAt(0);
                } else {
                    readInvalidError("NVDLReader.WildCardOneCharError",
                                     new Object[] {ars[1].value});
                }
                NVDLRule rule = createRuleModel(false, ars[0].value,
                                                wildCardChar, ars[2].value);
                parseRuleActions(this, rule);
                return rule;
            } else if (localName.equals("anyNamespace")) {
                AtRet[] ars = new AtRet[1];
                ars[0] = new AtRet("match");
                getAttrs(attrs, ars);
                NVDLRule rule = createRuleModel(true, null, ' ', ars[0].value);
                parseRuleActions(this, rule);
                return rule;
            }
            return null;
        }

        RuleHandler(BaseHandler bh) {
            super(bh);
        }
    }

    private void parseRuleActions(BaseHandler bh, NVDLRule rule) {
        setHandler(new ActionsHandler(bh, rule));
    }
    private class ActionsHandler extends WSSkipHandler {
        private final NVDLRule baseRule;
        private boolean cancelNestedActions = false;
        private boolean noResultAction = false;
        private boolean resultAction = false;
        private void checkResultAction(String localName) throws SAXException {
            if (resultAction) {
                readInvalidError("NVDLReader.OneResultActionError",
                                 new Object[] {localName});
            }
        }

        private void parseValidate(Attributes attrs) throws SAXException {
            AtRet[] ars = new AtRet[5];
            ars[0] = new AtRet("useMode");
            ars[1] = new AtRet("message");
            ars[2] = new AtRet("schemaType");
            ars[3] = new AtRet("schema");
            ars[4] = new AtRet("id");
            getAttrs(attrs, ars);
            String id = uniqueActionID(attrs);
            NVDLValidateAction action = new NVDLValidateAction(id, ars[0].value, baseRule);
            rules.putAction(action);
            setLocation(action);
            baseRule.getActionManager().addNoResultAction(action);
            if (ars[2].value != null) action.setSchemaType(ars[2].value);
            if (ars[3].value != null) action.setSchemaIRI(ars[3].value);

            if (ars[1].value != null) {
                action.getMessage().addMessage(null, ars[2].value);
            }

            action.setSchemaLoader(newSchemaLoader(action.getSchemaType()));

            setHandler(new ValidateHandler(this, action));
        }

        private AtRet[] parseBaseActionAttributes(Attributes attrs)
            throws SAXException {
            AtRet[] ars = new AtRet[3];
            ars[0] = new AtRet("useMode");
            ars[1] = new AtRet("message");
            ars[2] = new AtRet("id");
            getAttrs(attrs, ars);
            return ars;
        }

        private void parseActionBase(NVDLAction action,
                                     String message) {
            rules.putAction(action);
            setLocation(action);
            if (message != null) {
                action.getMessage().addMessage(null, message);
            }
            setHandler(new ActionBaseHandler(this, action));
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)) {
                setHandler(new ForeignSkipHandler(this));
                return;
            }
            if (cancelNestedActions) {
                readInvalidError("NVDLReader.CancelNestedActionsError",
                                 new Object[] {localName});
            }
            if (localName.equals("cancelNestedActions")) {
                cancelNestedActions = true;
                checkAttrs(attrs);
                baseRule.getActionManager().setCancelAction();
                setHandler(new WSSkipHandler(this));
            } else if (localName.equals("validate")) {
                noResultAction = true;
                parseValidate(attrs);
            } else if (localName.equals("allow")) {
                noResultAction = true;
                AtRet[] atr = parseBaseActionAttributes(attrs);
                String id = uniqueActionID(attrs);
                NVDLAllowAction a = new NVDLAllowAction(id, atr[0].value, baseRule);
                baseRule.getActionManager().addNoResultAction(a);
                parseActionBase(a, atr[1].value);
            } else if (localName.equals("reject")) {
                noResultAction = true;
                AtRet[] atr = parseBaseActionAttributes(attrs);
                String id = uniqueActionID(attrs);
                NVDLRejectAction a = new NVDLRejectAction(id, atr[0].value, baseRule);
                baseRule.getActionManager().addNoResultAction(a);
                parseActionBase(a, atr[1].value);
            } else if (localName.equals("attach")) {
                checkResultAction(localName);
                resultAction = true;
                AtRet[] atr = parseBaseActionAttributes(attrs);
                String id = uniqueActionID(attrs);
                NVDLResultAction a = new NVDLResultAction(id, atr[0].value,
                                                          NVDLResultAction.TYPE_ATTACH,
                                                          baseRule);
                baseRule.getActionManager().setResultAction(a);
                parseActionBase(a, atr[1].value);
            } else if (localName.equals("attachPlaceHolder")) {
                checkResultAction(localName);
                resultAction = true;
                AtRet[] atr = parseBaseActionAttributes(attrs);
                String id = uniqueActionID(attrs);
                NVDLResultAction a = new NVDLResultAction(id, atr[0].value,
                                                          NVDLResultAction.TYPE_ATTACHPLACEHOLDER,
                                                          baseRule);
                baseRule.getActionManager().setResultAction(a);
                parseActionBase(a, atr[1].value);
            } else if (localName.equals("unwrap")) {
                checkResultAction(localName);
                resultAction = true;
                AtRet[] atr = parseBaseActionAttributes(attrs);
                String id = uniqueActionID(attrs);
                NVDLResultAction a = new NVDLResultAction(id, atr[0].value,
                                                          NVDLResultAction.TYPE_UNWRAP,
                                                          baseRule);
                baseRule.getActionManager().setResultAction(a);
                parseActionBase(a, atr[1].value);
            } else {
                super.startElement(uri, localName, qName, attrs);
            }
        }
        public void endElement(String namespaceURI,
                               String localName, String qName) throws SAXException {
            super.endElement(namespaceURI, localName, qName);
            if (!cancelNestedActions && !noResultAction && !resultAction) {
                readInvalidError("NVDLReader.NoActionError");
            }
        }
        ActionsHandler(BaseHandler bh, NVDLRule baseRule) {
            super(bh);
            this.baseRule = baseRule;
        }
    }

    private SchemaLoader newSchemaLoader(String schemaType) {
        return new SchemaLoader(locator.getSystemId(), newLocation());
    }

    private class ValidateHandler extends ActionBaseHandler {
        private final NVDLValidateAction validateAction;
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)) {
                super.startElement(uri, localName, qName, attrs);
            } else if (localName.equals("option")) {
                if (actionBaseState > MESSAGE_OPTION_APPEARED) {
                    readInvalidError("NVDLReader.OptionBeforeSchemaError");
                }
                actionBaseState = MESSAGE_OPTION_APPEARED;
                AtRet[] ars = new AtRet[3];
                ars[0] = new AtRet("name");
                ars[1] = new AtRet("arg");
                ars[2] = new AtRet("mustSupport");
                getAttrs(attrs, ars);
                if (ars[0].value == null) {
                    readInvalidError("NVDLReader.OptionHasNameError");
                }
                // Simplification 6.4.5
                boolean mustSupport = parseXSDBoolean(ars[2].value, false);
                validateAction.addOption(ars[0].value, ars[1].value, mustSupport);
                setHandler(new WSSkipHandler(this));
            } else if (localName.equals("schema")) {
                if (validateAction.getSchemaIRI() != null) {
                    readInvalidError("NVDLReader.SchemaAttributeSpecifiedError");
                }
                if (actionBaseState > SCHEMA_APPEARED) {
                    readInvalidError("NVDLReader.SchemaBeforeModeOrContextError");
                }
                actionBaseState = SCHEMA_APPEARED;
                parseSchema(this, validateAction, attrs);
            } else {
                super.startElement(uri, localName, qName, attrs);
            }
        }

        public void endElement(String namespaceURI,
                               String localName, String qName) throws SAXException {
            super.endElement(namespaceURI, localName, qName);
            // schema element or attribute check.
            if (!validateAction.isSchamaSpecified()) {
                readInvalidError("NVDLReader.ValidateHasSchemaError");
            }
        }

        ValidateHandler(BaseHandler bh, NVDLValidateAction action) {
            super(bh, action);
            this.validateAction = action;
        }
    }

    private class ActionBaseHandler extends WSSkipHandler {
        private final NVDLAction action;

        protected static final int INITIAL = 0;
        protected static final int MESSAGE_OPTION_APPEARED = 1;
        protected static final int SCHEMA_APPEARED = 2;
        protected static final int MODEUSAGE1_APPEARED = 3;
        protected static final int CONTEXT_APPEARED = 4;
        protected int actionBaseState = INITIAL;

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)) {
                setHandler(new ForeignSkipHandler(this));
                return;
            }
            if (localName.equals("message")) {
                if (actionBaseState > MESSAGE_OPTION_APPEARED) {
                    readInvalidError("NVDLReader.MessageBeforeSchemaError");
                }
                actionBaseState = MESSAGE_OPTION_APPEARED;
                parseMessage(this, action.getMessage(), attrs);
            } else if (localName.equals("mode")) {
                checkNestedMode(action);
                if (actionBaseState > MODEUSAGE1_APPEARED) {
                    readInvalidError("NVDLReader.ModeBeforeContextError");
                }
                actionBaseState = MODEUSAGE1_APPEARED;
                action.setUseMode(parseNestedMode(this, attrs));
            } else if (localName.equals("context")) {
                actionBaseState = CONTEXT_APPEARED;
                parseContext(this, action, attrs);
            } else {
                super.startElement(uri, localName, qName, attrs);
            }
        }

        ActionBaseHandler(BaseHandler bh, NVDLAction action) {
            super(bh);
            this.action = action;
        }
    }

    private void parseMessage(BaseHandler bh,
                              NVDLMessage nm, Attributes attrs) {
        String lang = attrs.getValue("xml:lang");
        setHandler(new MessageHandler(bh, nm, lang));
    }
    private class MessageHandler extends BaseHandler {
        private final NVDLMessage nm;
        private final String lang;
        private StringBuffer buf = new StringBuffer();

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            buf.append(ch, start, length);
        }

        public void endElement(String namespaceURI,
                               String localName, String qName) throws SAXException {
            super.endElement(namespaceURI, localName, qName);
            nm.addMessage(lang, buf.toString());
        }

        MessageHandler(BaseHandler bh, NVDLMessage nm, String lang) {
            super(bh);
            this.nm = nm;
            this.lang = lang;
        }
    }

    private void parseSchema(BaseHandler bh,
                             NVDLValidateAction action,
                             Attributes attrs) throws SAXException {
        checkAttrs(attrs);
        setHandler(new SchemaHandler(bh, action));
    }
    private class SchemaHandler extends WSSkipHandler {
        private NVDLValidateAction action;
        private int level;
        private SchemaFactory schemaFactory;
        private SchemaReaderProxy proxyForSchemaAttribute;
        private SchemaReaderProxy proxyForSchemaElement;

        public void setDocumentLocator(Locator locator) {
            if (proxyForSchemaAttribute != null) {
                proxyForSchemaAttribute.setDocumentLocator(locator);
            }
            if (proxyForSchemaElement != null) {
                proxyForSchemaElement.setDocumentLocator(locator);
            }
            super.setDocumentLocator(locator);
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            if (level == 0) {
                Log.debug("Try to create schemaFactory for " + uri);
                try {
                    schemaFactory = SchemaFactory.newInstance(uri);
                    schemaFactory.setErrorHandler(eh);
                } catch (IllegalArgumentException e){
                    readInvalidError("NVDLReader.SchemaImplementationNotFoundError",
                                     new Object[] {uri});
                    return;
                }
                if (proxyForSchemaAttribute != null) {
                    proxyForSchemaAttribute.begin(schemaFactory,
                                                  new InputSource(locator.getSystemId()),
                                                  uri, true);
                }
                if (proxyForSchemaElement != null) {
                    proxyForSchemaElement.begin(schemaFactory,
                                                new InputSource(locator.getSystemId()),
                                                uri, false);
                }
            }
                if (proxyForSchemaAttribute != null) {
                    proxyForSchemaAttribute.startElement(uri, localName, qName, attributes);
                }
                if (proxyForSchemaElement != null) {
                    proxyForSchemaElement.startElement(uri, localName, qName, attributes);
                }
            level++;
        }

        public void characters(char[] ch,
                               int start,
                               int length)
            throws SAXException {
            if (proxyForSchemaAttribute != null) {
                proxyForSchemaAttribute.characters(ch, start, length);
            }
            if (proxyForSchemaElement != null) {
                proxyForSchemaElement.characters(ch, start, length);
            }
        }

        public void endElement(String uri,
                               String localName,
                               String qName) throws SAXException {
            if (level == 0) {
                if (proxyForSchemaAttribute != null) {
                    proxyForSchemaAttribute.end();
                    if (proxyForSchemaAttribute.getSchema() == null) {
                        readInvalidError("NVDLReader.SchemaReadError");
                    }
                    action.setSchema(proxyForSchemaAttribute.getSchema(), true);
                }
                if (proxyForSchemaElement != null) {
                    proxyForSchemaElement.end();
                    if (proxyForSchemaElement.getSchema() == null) {
                        readInvalidError("NVDLReader.SchemaReadError");
                    }
                    action.setSchema(proxyForSchemaElement.getSchema(), false);
                }
                super.endElement(uri, localName, qName);
            } else {
                if (proxyForSchemaAttribute != null) {
                    proxyForSchemaAttribute.endElement(uri, localName, qName);
                }
                if (proxyForSchemaElement != null) {
                    proxyForSchemaElement.endElement(uri, localName, qName);
                }
                level--;
            }
        }

        public void startPrefixMapping(String prefix,
                                       String uri)
            throws SAXException {
            super.startPrefixMapping(prefix, uri);
            if (proxyForSchemaAttribute != null) {
                proxyForSchemaAttribute.startPrefixMapping(prefix, uri);
            }
            if (proxyForSchemaElement != null) {
                proxyForSchemaElement.startPrefixMapping(prefix, uri);
            }
        }
        public void endPrefixMapping(String prefix) throws SAXException {
            super.endPrefixMapping(prefix);
            if (proxyForSchemaAttribute != null) {
                proxyForSchemaAttribute.endPrefixMapping(prefix);
            }
            if (proxyForSchemaElement != null) {
                proxyForSchemaElement.endPrefixMapping(prefix);
            }
        }

        public void processingInstruction(String target,
                                          String data)
            throws SAXException {
            if (proxyForSchemaAttribute != null) {
                proxyForSchemaAttribute.processingInstruction(target, data);
            }
            if (proxyForSchemaElement != null) {
                proxyForSchemaElement.processingInstruction(target, data);
            }
        }

    	SchemaHandler(BaseHandler bh,
                      NVDLValidateAction action) {
            super(bh);
            this.action = action;
            level = 0;

            if (action.getBelongingRule().isTargetAttribute()) {
                this.proxyForSchemaAttribute = SchemaReaderProxy.newProxy(locator, prefixMapper);
                this.proxyForSchemaAttribute.setErrorHandler(eh);
            }
            if (action.getBelongingRule().isTargetElement()) {
                this.proxyForSchemaElement = SchemaReaderProxy.newProxy(locator, prefixMapper);
                this.proxyForSchemaElement.setErrorHandler(eh);
            }
    	}
    }

    private void parseContext(BaseHandler bh,
                              NVDLAction action, Attributes attrs)
        throws SAXException{
        AtRet[] ars = new AtRet[2];
        ars[0] = new AtRet("path");
        ars[1] = new AtRet("useMode");
        getAttrs(attrs, ars);
        if (ars[0].value == null) {
            readInvalidError("NVDLReader.ContextHasPathError");
        }
        setHandler(new ContextHandler(bh, action, ars[0].value, ars[1].value));
    }
    private class ContextHandler extends WSSkipHandler {
        private final NVDLAction action;
        private NVDLMode mode;
        private final String useModeName;
        private final String path;
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)) {
                setHandler(new ForeignSkipHandler(this));
                return;
            }
            if (localName.equals("mode")) {
                if (useModeName != null) {
                    readInvalidError("NVDLReader.ContextHasModeButUseModeIsSpecifiedError");
                }
                mode = parseNestedMode(this, attrs);
            } else {
                super.startElement(uri, localName, qName, attrs);
            }
        }

        public void endElement(String namespaceURI,
                               String localName, String qName) throws SAXException {
            super.endElement(namespaceURI, localName, qName);
            try {
                if (mode == null) {
                    if (useModeName == null) {
                        readInvalidError("NVDLReader.ContextHasUseModeOrModeError");
                    }
                    action.addContext(path, useModeName);
                } else {
                    action.addContext(path, mode);
                }
            } catch (NVDLException e) {
                readInvalidError(e.getMessage());
            }
        }

        ContextHandler(BaseHandler bh, NVDLAction action,
                       String path, String useModeName) {
            super(bh);
            this.action = action;
            this.path = path;
            this.useModeName = useModeName;
        }
    }


    private class RulesHandler extends RuleHandler {
        private static final int INITIAL = 0;
        private static final int TRIGGER_APPEARED = 1;
        private static final int RULE_APPEARED = 2;
        private static final int MODE_APPEARED = 3;
        private int rulesState = INITIAL;

        private void parseTrigger(Attributes attrs) throws SAXException {
            AtRet[] ars = new AtRet[2];
            ars[0] = new AtRet("ns");
            ars[1] = new AtRet("nameList");
            getAttrs(attrs, ars);
            if (ars[0].value == null) {
                readInvalidError("NVDLReader.TriggerHasNSError");
            }
            if (ars[1].value == null) {
                readInvalidError("NVDLReader.TriggerHasNameListError");
            }
            rules.getTriggerManager().addTrigger(ars[0].value,
                                                 splitList(ars[1].value));
            // empty element.
            setHandler(new WSSkipHandler(this));
        }

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)) {
                setHandler(new ForeignSkipHandler(this));
                return;
            }
            if (localName.equals("trigger")) {
                if (rulesState > TRIGGER_APPEARED) {
                    readInvalidError("NVDLReader.TriggerIsFirstError");
                }
                rulesState = TRIGGER_APPEARED;
                parseTrigger(attrs);
            } else if (localName.equals("mode")) {
                if (rulesState == RULE_APPEARED) {
                    readInvalidError("NVDLReader.ModeIsAfterRuleError");
                }
                rulesState = MODE_APPEARED;
               	parseMode(this, attrs);
            } else {
                // Simplification 6.4.7
                NVDLRule rule = parseRule(localName, attrs);
                if (rule == null) {
                    super.startElement(uri, localName, qName, attrs);
                    return;
                }
                if (rulesState == MODE_APPEARED) {
                    readInvalidError("NVDLReader.RuleIsAfterModeError",
                                     new Object[] {localName});
                }
                rulesState = RULE_APPEARED;
                if (rules.getStartModeName() != null) {
                    readInvalidError("NVDLReader.StartModeError");
                }
                NVDLMode mode = rules.getStartMode();
                if (mode == null) {
                    mode = new NVDLMode("nested start mode ");
                    mode.setLocation(rule.getLocation());
                    rules.setStartMode(mode);
                }
                addRule(mode, rule);
            }
        }

        RulesHandler(BaseHandler bh) {
            super(bh);
        }
    }

    // must have name;
    private NVDLMode parseMode(BaseHandler ph, Attributes attrs)
        throws SAXException {
        AtRet[] ars = new AtRet[1];
        ars[0] = new AtRet("name");
        getAttrs(attrs, ars);
        if (ars[0].value == null) {
            readInvalidError("NVDLReader.ModeInRulesHasName");
        }
        NVDLMode mode = new NVDLMode(ars[0].value);
        setLocation(mode);
        if (rules.getMode(mode.name) != null) {
            readInvalidError("NVDLReader.ModeNameConflictError",
                             new Object[] {mode.name});
        }
        setHandler(new ModeHandler(ph, mode));
        rules.putMode(mode);

        return mode;
    }
    // optionally has name
    private NVDLMode parseIncludedMode(BaseHandler ph, Attributes attrs,
                                       NVDLMode parentMode)
        throws SAXException {
        AtRet[] ars = new AtRet[1];
        ars[0] = new AtRet("name");
        getAttrs(attrs, ars);
        NVDLMode mode = new NVDLMode(ars[0].value);
        setLocation(mode);
        setHandler(new ModeHandler(ph, mode));
        parentMode.addIncludedMode(mode);
        return mode;
    }
    // has no name.
    private NVDLMode parseNestedMode(BaseHandler ph, Attributes attrs)
        throws SAXException {
        checkAttrs(attrs);
        NVDLMode mode = new NVDLMode(uniqueModeName());
        setLocation(mode);
        setHandler(new ModeHandler(ph, mode));
        return mode;
    }
    private class ModeHandler extends RuleHandler {
        private final NVDLMode mode;
        private static final int INITIAL = 0;
        private static final int INCLUDEDMODE_APPEARED = 1;
        private static final int RULE_APPEARED = 2;
        private int modeState = INITIAL;

        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attrs) throws SAXException {
            if (isForeign(uri)) {
                setHandler(new ForeignSkipHandler(this));
                return;
            }
            if (localName.equals("mode")) {
                if (modeState > INCLUDEDMODE_APPEARED) {
                    readInvalidError("NVDLReader.IncludedModeAfterRuleError");
                }
                parseIncludedMode(this, attrs, mode);
                modeState = INCLUDEDMODE_APPEARED;
            } else {
                NVDLRule rule = parseRule(localName, attrs);
                if (rule == null) {
                    super.startElement(uri, localName, qName, attrs);
                } else {
                    addRule(mode, rule);
                    modeState = RULE_APPEARED;
                }
            }
        }

        ModeHandler(BaseHandler bh, NVDLMode mode) {
            super(bh);
            this.mode = mode;
        }
    }

    public NVDLRules parse(InputSource is) throws NVDLReaderException, IOException {
        this.errorCounter = 0;
        try {
            reader.parse(is);
            if (errorCounter > 0) {
                readInvalidError("NVDLReader.NVDLHasError",
                                 new Object[] {new Integer(errorCounter)});
                return null;
            }
            NVDLSimplifier simplifier = new NVDLSimplifier(rules, eh);
            try {
                simplifier.traverse(rules);
            } catch (NVDLModelException e) {
                throw new NVDLReaderException(e);
            }
            if (simplifier.getErrorCount() > 0) {
                readInvalidError("NVDLReader.NVDLHasError",
                                 new Object[] {new Integer(simplifier.getErrorCount())});
                return null;
            }
        } catch (SAXException e) {
            Exception ei = e.getException();
            if (ei instanceof NVDLReaderException) {
                throw (NVDLReaderException) ei;
            }
            throw new NVDLReaderException(e);
        }
        return rules;
    }

    public static SAXParser newSAXParser() throws SAXException {
        SAXParserFactory pf = SAXParserFactory.newInstance();
        try {
			pf.setFeature("http://xml.org/sax/features/namespaces", true);
	        pf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
	        pf.setXIncludeAware(true);
	        return pf.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
    }

    public NVDLSAXReader(SAXParser parser) throws SAXException {
        this(parser, null);
    }

    public NVDLSAXReader(ErrorHandler eh) throws SAXException {
        this(newSAXParser(), eh);
    }
	
    public NVDLSAXReader(SAXParser parser, ErrorHandler eh) throws SAXException {
        this.reader = parser.getXMLReader();
        if (eh != null) {
            this.eh = eh;
        } else {
            this.eh = DefaultErrorHandler.getErrorHandler();
        }
       setHandler(new InitialHandler());
    }

}
