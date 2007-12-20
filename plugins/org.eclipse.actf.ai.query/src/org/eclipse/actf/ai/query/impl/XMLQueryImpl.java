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

package org.eclipse.actf.ai.query.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.actf.model.dom.dombycom.IDocumentEx;
import org.eclipse.actf.util.xpath.XPathService;
import org.eclipse.actf.util.xpath.XPathServiceFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class XMLQueryImpl {
    private static final String XML_QUERY_NS = "http://www.ibm.com/xmlns/prod/aiBrowser/fennec/xml-query";
    private static final XPathService xpathService = XPathServiceFactory.newService();
    
    private final String[] ids;

    private Object compiledXPath;

    private XMLQueryImpl(String[] ids, Object compiledXPath) {
        this.ids = ids;
        this.compiledXPath = compiledXPath;
    }

    public boolean hasTarget() {
        return (ids != null) || (compiledXPath != null);
    }

    public List query(Node base) {
        List<Node> result = null;

        if (ids != null) {
            if (result == null) {
                result = new ArrayList<Node>();
            }
            Document doc = base.getOwnerDocument();
            for (int i = 0; i < ids.length; i++) {
                if (doc instanceof IDocumentEx) {
                    List r = ((IDocumentEx) doc).getElementsByIdInAllFrames(ids[i]);
                    result.addAll(r);
                } else {
                    Node n = doc.getElementById(ids[i]);
                    if (n != null) result.add(n);
                }
            }

        }
        if (compiledXPath != null) {
            NodeList nl = xpathService.evalForNodeList(compiledXPath, base);
            if (nl != null) {
                int len = nl.getLength();
                if (len > 0) {
                    if (result == null) {
                        result = new ArrayList<Node>(len);
                    }
                    for (int i = 0; i < len; i++) {
                        result.add(nl.item(i));
                    }
                }
            }
        }
        return result;
    }

    static XMLQueryImpl parse(Element e, XMLQueryImpl parentQuery) {
        String[] ids = null;
        
        String idrefs = e.getAttributeNS(XML_QUERY_NS, "idrefs");
        if (idrefs.length() > 0) {
            ids = idrefs.split("[ \r\n\t]");
        }
        String path = e.getAttributeNS(XML_QUERY_NS, "path");

        Object compiled = null;
        if (path.length() > 0) {
            compiled = xpathService.compile(path);
        }
        return new XMLQueryImpl(ids, compiled);
    }

    // --------------------------------------------------------------------------------
    //    XPath Query Service
    // --------------------------------------------------------------------------------
    static XMLQueryImpl parseXPath(String xpath) {
        Object compiled = xpathService.compile(xpath);
        return new XMLQueryImpl(null, compiled);
    }

    // --------------------------------------------------------------------------------
    //    Query Serialization
    // --------------------------------------------------------------------------------
    
    static Attr serializeQuery(Node domNode, Node usrNode) {
        Document doc = usrNode.getOwnerDocument();
        String id = getID(domNode);
        if (id != null && id.length() > 0) {
            Attr attr = doc.createAttributeNS(XML_QUERY_NS, "loc:idrefs");
            attr.setNodeValue(id);
            return attr;
        }
        String path = getXPath(domNode);
        if (path != null && path.length() > 0) {
            Attr attr = doc.createAttributeNS(XML_QUERY_NS, "loc:path");
            attr.setNodeValue(path);
            return attr;
        }
        return null;
    }

    private static String getID(Node node) {
        if (!(node instanceof Element))
            return null;
        Element e = (Element) node;
        return e.getAttribute("id");
    }

    private static String checkID(Node n) {
        if (!(n instanceof Element)) return null;
        Element e = (Element) n;
        String id = e.getAttribute("id");
        if (id == null) return null;
        Document doc = e.getOwnerDocument();
        Node e2 = doc.getElementById(id);
        if (!e.isSameNode(e2)) return null;
        // ':' is not allowed according to XML spec.
        if (id.indexOf(':') >= 0) return null;
        return id;
    }
    
    private static String getXPath(Node node) {
//      return XPathCreator.childPathSequence(n);
        StringBuffer ret = new StringBuffer();
        for (Node n = node; n != null; n = n.getParentNode()) {
//          /* /HTML[1]/ -> ./ */
//          if (node.getParentNode() instanceof Document) {
//              ret.insert(0, '.');
//              break;
//          }
            if (n instanceof Document) {
                break;
            }

            String id = checkID(n);
            // if (id != null) System.err.println("ID " + id + " is used");
            if (id != null) {
                ret.insert(0, "\")");
                ret.insert(0, id);
                ret.insert(0, "id(\"");
                break;
            }

            String name = n.getNodeName();
            int k = 1;
            if (name.startsWith("#")) {
//                ITreeItem a = item.getParent();
//                Node p = node.getParentNode();
//                if (a != null) {
//                    for (ITreeItem c : a.getChildItems()) {
//                        if (c == item)
//                            break;
//                        Object o = c.getBaseNode();
//                        if (!(o instanceof Node))
//                            continue;
//                        Node n = (Node) o;
//                        if (n.getParentNode() == p && n.getNodeName().equals(name))
//                            ++k;
//                    }
//                }
                /* currently text() is not supported */
//              return calcPath(a); // query the parent node in the nav tree
                continue;           // query the parent node in the DOM tree
            }
            else {
                Node p = n.getParentNode();
                if (p != null) {
                    for (Node c = p.getFirstChild(); c != null; c = c.getNextSibling()) {
                        if (c == n)
                            break;
                        if (c.getNodeName().equals(name))
                            ++k;
                    }
                }
            }
            ret.insert(0, ']');
            ret.insert(0, k);
            ret.insert(0, '[');
            ret.insert(0, getNodeNameForXPath(n.getNodeName()));
            ret.insert(0, '/');
        }
        return ret.toString();
    }

    private static String getNodeNameForXPath(String s) {
        if (s.startsWith("#"))
            return s.substring(1) + "()";
        else
            return s;
    }
    
}
