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
import java.util.Iterator;
import java.util.List;

import org.eclipse.actf.ai.query.IQuery;
import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.eclipse.actf.model.dom.dombycom.IMSAANode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class QueryImpl implements IQuery {
    private final XMLQueryImpl xmlQuery;

    private final FlashQueryImpl flashQuery;

    private final MSAAQueryImpl msaaQuery;

    private QueryImpl(XMLQueryImpl xmlQuery,
                      FlashQueryImpl flashQuery,
                      MSAAQueryImpl msaaQuery) {
        this.xmlQuery = xmlQuery;
        this.flashQuery = flashQuery;
        this.msaaQuery = msaaQuery;
    }

    public static QueryImpl parse(Element e, IQuery parentQuery) {
        QueryImpl pq = null;
        if (parentQuery == null) {
            pq = null;
        } else {
            pq = (QueryImpl) parentQuery;
        }

        XMLQueryImpl xmlParentQuery;
        if (pq == null) {
            xmlParentQuery = null;
        } else {
            xmlParentQuery = pq.xmlQuery;
        }
        XMLQueryImpl xmlQuery = XMLQueryImpl.parse(e, xmlParentQuery);

        FlashQueryImpl flashParentQuery;
        if (pq == null) {
            flashParentQuery = null;
        } else {
            flashParentQuery = pq.flashQuery;
        }
        FlashQueryImpl flashQuery = FlashQueryImpl.parse(e, flashParentQuery);

        MSAAQueryImpl msaaParentQuery;
        if (pq == null) {
            msaaParentQuery = null;
        } else {
            msaaParentQuery = pq.msaaQuery;
        }
        MSAAQueryImpl msaaQuery = MSAAQueryImpl.parse(e, msaaParentQuery);

        return new QueryImpl(xmlQuery, flashQuery, msaaQuery);
    }

    public static QueryImpl parseXPath(String xpath) {
        XMLQueryImpl xmlQuery = XMLQueryImpl.parseXPath(xpath);
        return new QueryImpl(xmlQuery, null, null);
    }

    private static class NodeListImpl implements NodeList {
        private List<Node> nodeList;

        public Node item(int index) {
            if ((index < 0) || (index >= nodeList.size()))
                return null;
            return (Node) nodeList.get(index);
        }

        public int getLength() {
            return nodeList.size();
        }

        NodeListImpl(List<Node> nodeList) {
            if (nodeList == null) {
                this.nodeList = new ArrayList<Node>(0);
            } else {
                this.nodeList = nodeList;
            }
        }
    }

    public boolean hasTarget() {
        return (((xmlQuery != null) && xmlQuery.hasTarget()) || (flashQuery != null) && flashQuery.hasTarget());
    }

    public NodeList query(Node base) {
        List<Node> result = null;
        if ((xmlQuery != null) && (xmlQuery.hasTarget())) {
            result = xmlQuery.query(base);
        }
        if ((flashQuery != null) && (flashQuery.hasTarget())) {
            if (result == null) {
                result = flashQuery.query(base);
            } else {
                List<Node> r = new ArrayList<Node>();
                for (Iterator<Node> iter = result.iterator(); iter.hasNext();) {
                    Node node = iter.next();
                    if (!(node instanceof IFlashNode))
                        continue;
                    List<Node> list = flashQuery.query((IFlashNode)node);
                    if (list != null)
                        r.addAll(list);
                }
                result = r;
            }
        }
        if ((msaaQuery != null) && (msaaQuery.hasTarget())) {
            if (result == null) {
                result = msaaQuery.query(base);
            } else {
                List<Node> r = new ArrayList<Node>();
                for (Iterator<Node> iter = result.iterator(); iter.hasNext();) {
                    Node node = iter.next();
                    if (!(node instanceof IFlashNode))
                        continue;
                    List<Node> list = msaaQuery.query(((IFlashNode)node).getMSAA());
                    if (list != null)
                        r.addAll(list);
                }
                result = r;
            }
        }
        return new NodeListImpl(result);
    }

    // --------------------------------------------------------------------------------
    //  Query Serialization.
    // --------------------------------------------------------------------------------
    
    public static final String Fennec_NAMESPACE_URI = "http://www.ibm.com/xmlns/prod/aiBrowser/fennec";
    public static final String Fennec_NODE_ELEMENT_NAME = "node";
    
    public static Node serializeQuery(Node domTarget, Node usrParent) {
        List<Attr> attrs = calcAttrs(domTarget, usrParent);
        Node node = getNode(usrParent, attrs);
        if (node != null) {
            return node;
        } else {
            return createNode(usrParent, attrs);
        }
    }
    
    private static List<Attr> calcAttrs(Node domTarget, Node usrParent) {
        List<Attr> attrs = new ArrayList<Attr>(2);
        Node xmlNode;
        Attr flashAttr;

        if (domTarget instanceof IFlashNode) {
            xmlNode = ((IFlashNode) domTarget).getBaseNode();
            flashAttr = FlashQueryImpl.serializeQuery(domTarget, usrParent);
        } else if (domTarget instanceof IMSAANode) {
            xmlNode = ((IMSAANode) domTarget).getBaseNode();
            flashAttr = MSAAQueryImpl.serializeQuery(domTarget, usrParent);
        } else {
            xmlNode = domTarget;
            flashAttr = null;
        }
        Attr xmlAttr = XMLQueryImpl.serializeQuery(xmlNode, usrParent);
        if (xmlAttr != null) attrs.add(xmlAttr);
        if (flashAttr != null) attrs.add(flashAttr);

        return attrs;
    }

    private static boolean match(Node node, List<Attr> attrs) {
        NamedNodeMap map = node.getAttributes();
        if (map == null)
            return false;
        for (Attr a : attrs) {
             Node n = map.getNamedItemNS(a.getNamespaceURI(), a.getLocalName());
             if (n == null)
                 return false;
             String s = a.getNodeValue();
             String t = n.getNodeValue();
             if (!s.equals(t))
                 return false;
        }
        return true;
    }
    
    private static Node getNode(Node parent, List<Attr> attrs) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (match(n, attrs))
                return n;
        }
        return null;
    }
    
    private static Node createNode(Node parent, List<Attr> attrs) {
        Document doc = parent.getOwnerDocument();
        Node node = parent.insertBefore(doc.createElementNS(Fennec_NAMESPACE_URI, Fennec_NODE_ELEMENT_NAME), parent.getFirstChild());
        NamedNodeMap map = node.getAttributes();
        for (Attr a : attrs) {
            map.setNamedItemNS(a);
        }
        return node;
    }
    
}
