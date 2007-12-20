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

package org.eclipse.actf.ai.xmlstore.spi;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLSelectorInfo implements IXMLSelector {
    public static final String SELECTOR_NS = "http://www.ibm.com/xmlns/prod/AcTF/aiBrowser/selector/1.0";

    private static final int DEFAULT_PRIORITY = 0;
    
    // --------------------------------------------------------------------------------
    //            Parser
    // --------------------------------------------------------------------------------

    private interface FinishHandler {
        void set(String s);
    }

    private static abstract class BaseHandler extends DefaultHandler {
        private IXMLInfo info;
        protected void setHandler(BaseHandler h) {
            info.setContentHandler(h);
        }
        protected BaseHandler(IXMLInfo info) {
            this.info = info;
        }
        protected BaseHandler(BaseHandler base) {
            this.info = base.info;
        }
    }

    private static class TextContentHandler extends BaseHandler {
        private StringBuffer buf = new StringBuffer();
        private int level;
        private final BaseHandler prevHandler;
        private final FinishHandler finishHandler;

        @Override
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            level++;
        }

        @Override
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            buf.append(ch, start, length);
        }

        @Override
        public void endElement(String namespaceURI,
                               String localName, String qName) throws SAXException {
            level--;
            if (level < 0) {
                finishHandler.set(buf.toString());
                setHandler(prevHandler);
            }
        }
        TextContentHandler(BaseHandler handler, FinishHandler fh) {
            super(handler);
            level = 0;
            finishHandler = fh;
            prevHandler = handler;
        }
    }

    private static class SelectorHandler extends BaseHandler {
        private XMLSelectorInfo selectorInfo;
        public XMLSelectorInfo getXMLSelectorInfo() {
            return selectorInfo;
        }

        private boolean rootFlag;
        
        private boolean metaFlag;

        private String quote(String pat, char wildCardChar) {
            StringBuffer r = new StringBuffer();
            for (int i = 0; i < pat.length(); i++) {
                char c = pat.charAt(i);
                if (c == wildCardChar) {
                    r.append(".*");
                } else if (("\\?*+.[]{}()$^".indexOf(c) >= 0)) {
                    r.append('\\');
                    r.append(c);
                } else {
                    r.append(c);
                }
            }
            return r.toString();
        }

        private void parseTargetSite(Attributes attributes) {
            String targetURI = attributes.getValue("", "uri");
            if (targetURI != null) {
                selectorInfo.targetURIs.add(targetURI);
                selectorInfo.targetURIPatterns.add(Pattern.compile(quote(targetURI, '*')));
            }
        }

        @Override
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            if (!rootFlag) {
                selectorInfo.documentElementNS = uri;
                selectorInfo.documentElementName = localName;
                rootFlag = true;
                return;
            }
            if (!(SELECTOR_NS.equals(uri))) return;
            if (metaFlag) {
                if ("targetSite".equals(localName)) {
                    parseTargetSite(attributes);
                    
                } else if ("documentation".equals(localName)) {
                    setHandler(new TextContentHandler(this, new FinishHandler() {
                        public void set(String s) {
                            selectorInfo.documentation = s;
                        }
                    }));
                } else if ("userEntry".equals(localName)) {
                    selectorInfo.isUserEntry = true;
                } else if ("pageTitle".equals(localName)) {
                    setHandler(new TextContentHandler(this, new FinishHandler() {
                        public void set(String s) {
                            selectorInfo.pageTitle = s;
                        }
                    }));
                } else if ("authorName".equals(localName)) {
                    setHandler(new TextContentHandler(this, new FinishHandler() {
                        public void set(String s) {
                            selectorInfo.authorName = s;
                        }
                    }));                   
                }
            } else if ("meta".equals(localName)) {
                metaFlag = true;
                return;
            }
        }

        public static class FinishedException extends SAXException {
            private static final long serialVersionUID = -7213987482937237478L;

            FinishedException() {
                super("Finished");
            }
        }

        @Override
        public void endElement(String uri,
                               String localName,
                               String qName) throws SAXException {
            if (!(SELECTOR_NS.equals(uri))) return;
            if (metaFlag) {
                if ("meta".equals(localName)) {
                    throw new FinishedException();
                }
            }
        }

        SelectorHandler(IXMLInfo info) {
            super(info);
            this.selectorInfo = new XMLSelectorInfo();
        }
    }

    static public XMLSelectorInfo parse(IXMLInfo info) throws XMLStoreException {
        SelectorHandler sh = new SelectorHandler(info);
        try {
            info.setContentHandler(sh);
            info.startSAX();
        } catch (SelectorHandler.FinishedException e) {
        } catch (Exception e) {
            throw new XMLStoreException("XMLSelectorInfo parse failed.");
        }
        return sh.getXMLSelectorInfo();
    }
    
    // --------------------------------------------------------------------------------
    //            Meta Information.
    // --------------------------------------------------------------------------------

    private String documentElementName;
    private String documentElementNS;

    public String getDocumentElementName() {
        return documentElementName;
    }

    public String getDocumentElementNS() {
        return documentElementNS;
    }

    private String documentation;

    public String getDocumentation() {
        return documentation;
    }
    
    private boolean isUserEntry = false;

    public boolean isUserEntry() {
        return isUserEntry;
    }
    
    private String pageTitle;

    public String getPageTitle() {
        return pageTitle;
    }
    
    private String authorName;
    
    public String getAuthorName() {
        return authorName;
    }


    public String getURI() {
        return "";
    }

    private int priority = DEFAULT_PRIORITY;

    public int getPriority() {
        return priority;
    }

    private ArrayList<Pattern> targetURIPatterns;
    private ArrayList<String> targetURIs;

    private boolean matchDocumentElement(IXMLSelector selector) {
        if (selector.getDocumentElementNS() != null) {
            if (!documentElementNS.equals(selector.getDocumentElementNS())) return false;
        }
        if (selector.getDocumentElementName() != null) {
            if (!documentElementName.equals(selector.getDocumentElementName())) return false;
        }
        return true;
    }

    private boolean matchURI(IXMLSelector selector) {
        String uri = selector.getURI();
        if (uri == null) return true;

        int size = targetURIPatterns.size();
        if (size == 0) return false;
        for (int i = 0; i < size; i++) {
            Pattern pat = targetURIPatterns.get(i);
            Matcher m = pat.matcher(uri);
            if (m.matches()) return true;
        }
        return false;
    }

    public boolean match(IXMLSelector selector) {
        if (!matchDocumentElement(selector)) return false;
        if (!matchURI(selector)) return false;
        return true;
    }

    private XMLSelectorInfo() {
        targetURIPatterns = new ArrayList<Pattern>();
        targetURIs = new ArrayList<String>();
    }
}
