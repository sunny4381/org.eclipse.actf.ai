/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.actf.ai.xmlstore.IXMLEditableInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.ai.xmlstore.spi.XMLSelectorInfo;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class UserXML extends XMLFile implements IXMLEditableInfo {

    private String namespaceURI;

    private String qualifiedName;

    private String documentation = "User Annotation";

    private String targetUriPattern;

    private Pattern uriPat;
    
    private String pageTitle;
    
    private String authorName;

    private final boolean onlyInMemory;

    private int priority;
    
    private boolean removed = false;

    private static String getFileName(String targetURI) {
        long time = System.currentTimeMillis();
        String uri = targetURI.replaceAll("[\\/:*?\"<>|]", "-");
        if (uri.length() > 200) 
            uri = uri.substring(0, 200);
        
        return time + "-" + uri + ".fnc";
    }

    UserXML(File file) throws XMLStoreException {
        super(file);
        onlyInMemory = false;
        targetUriPattern = getSelectorInfo().getURI();
        documentation = getSelectorInfo().getDocumentation();
        priority = getSelectorInfo().getPriority();
        pageTitle = getSelectorInfo().getPageTitle();
        authorName = getSelectorInfo().getAuthorName();
    }

    UserXML(String namespaceURI, String qualifiedName, String targetUriPattern, File dir) throws XMLStoreException {
        //      super(new File(dir, getFileName()));
        super(new File(dir, getFileName(targetUriPattern)), false);
        this.namespaceURI = namespaceURI;
        this.qualifiedName = qualifiedName;
        this.onlyInMemory = true;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();
            doc = impl.createDocument(namespaceURI, qualifiedName, null);
            setTargetURIPattern(targetUriPattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getTargetUriPattern() {
        return targetUriPattern;
    }

    @Override
    public String getDocumentation() {
        return documentation;
    }
    
    @Override
    public boolean isUserEntry() {
        return true;
    }
    
    public String getPageTitle() {
        return pageTitle;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Node getRootNode() throws XMLStoreException {
        if (onlyInMemory)
            return doc.getDocumentElement();
        else
            return super.getRootNode();
    }

    @Override
    public void setContentHandler(ContentHandler h) {
        if (!onlyInMemory)
            super.setContentHandler(h);
    }

    @Override
    public void startSAX() throws XMLStoreException, SAXException {
        if (onlyInMemory)
            throw new SAXException("not supported");
        else
            super.startSAX();
    }
    
    @Override
    public void reset() throws XMLStoreException {
        if(!onlyInMemory)
            super.reset();
    }

    public void save() {
        save(doc, this.file);
    }
    
    public void save(File file) {
        save(doc, file);
    }

    private void save(Document doc, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            save(doc, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void save(Document doc, OutputStream stream) {
        try {
            //System.out.println("store into " + file.getAbsolutePath());
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
        resetMeta();
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
        resetMeta();
    }

    private void setTargetURIPattern(String targetUriPattern) {
        this.targetUriPattern = targetUriPattern;
        uriPat = Pattern.compile(quote(targetUriPattern, '*'));
        resetMeta();
    }
    
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        resetMeta();
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
        resetMeta();
    }

    private void resetMeta() {
        Element meta = getElement("meta", doc.getDocumentElement());
        getElement("userEntry", meta);
        Element target = getElement("targetSite", meta);
        Element documentation = getElement("documentation", meta);
        Element pageTitle = getElement("pageTitle", meta);
        Element authorName = getElement("authorName", meta);

        target.setAttribute("uri", getTargetUriPattern());
        documentation.setTextContent(getDocumentation());
        pageTitle.setTextContent(getPageTitle());
        authorName.setTextContent(getAuthorName());
    }

    private Element getElement(String name, Node parent) {
        NodeList list = doc.getElementsByTagNameNS(XMLSelectorInfo.SELECTOR_NS, name);
        if (list.getLength() == 0) {
            Element temp = doc.createElementNS(XMLSelectorInfo.SELECTOR_NS, name);
            parent.insertBefore(temp, parent.getFirstChild());
            return temp;
        }
        return (Element) list.item(0);
    }

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

    public boolean match(IXMLSelector selector) {
        if (removed)
            return false;
        if (!onlyInMemory)
            return super.getSelectorInfo().match(selector);
        if (!matchDocumentElement(selector))
            return false;
        if (!matchURI(selector))
            return false;
        return true;
    }

    private boolean matchDocumentElement(IXMLSelector selector) {
        if (selector.getDocumentElementName() != null) {
            if (!namespaceURI.equals(selector.getDocumentElementNS()))
                return false;
            if (!qualifiedName.equals(selector.getDocumentElementName()))
                return false;
        }
        return true;
    }

    private boolean matchURI(IXMLSelector selector) {
        String uri = selector.getURI();
        if (uri == null)
            return true;

        Matcher m = uriPat.matcher(uri);
        if (m.matches())
            return true;
        return false;
    }

    public void remove() {
        removed = true;
        if (!file.exists())
            return;
        file.delete();
    }

    public void save(ZipOutputStream zos) {
        try {
            zos.putNextEntry(new ZipEntry(file.getName()));
            getRootNode();
            save(doc, zos);
            zos.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStoreException e) {
            e.printStackTrace();
        }
    }
}
