/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Masatomo KOBAYASHI - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.navigator.userinfo.impl;

import org.eclipse.actf.ai.navigator.userinfo.IUserInfoConstants;
import org.eclipse.actf.ai.navigator.userinfo.IUserInfoGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;




public class AltTextEditor implements IUserInfoGenerator, IUserInfoConstants {
    
//    @Override
//    public String toString() {
//        return "ALT text";
//    }

    public String toString(Result result) {
        switch (result) {
        case CREATED:
            return "AltTextEditor.Result.CREATED";
        case REMOVED:
            return "AltTextEditor.Result.REMOVED";
        case CHANGED:
            return "AltTextEditor.Result.CHANGED";
        case ERROR:
            return "AltTextEditor.Result.ERROR";
        }
        return "";
    }
    
    private Node getExistingUserInfo(Node node) {
        for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            String ns = c.getNamespaceURI();
            if ((ns == null) || (!ns.equals(DEFAULT_NAMESPACE))) continue;
            String n = c.getLocalName();
            if (n.equals("altText"))
                return c;
        }
        return null;
    }
    
    public Result addUserInfo(Node node, String text) {
        Document doc = node.getOwnerDocument();
        Node info = getExistingUserInfo(node);
        if (text.length() > 0) {
            if (info == null) {
                info = node.appendChild(doc.createElementNS(DEFAULT_NAMESPACE, "altText"));
                info.setTextContent(text);
                return Result.CREATED;
            }
            else {
                info.setTextContent(text);
                return Result.CHANGED;
            }
        }   
        // TODO ? which is better: "" -> remove the alt text added by the user or set an empty alt text
        else if (info != null) {
            node.removeChild(info);
            return Result.REMOVED;
        }
        return Result.NOTHING;
    }
    
//    public Status addUserInfo(Node parent, TargetNodeQuery paths, String text) {
//        Document doc = parent.getOwnerDocument();
//        Node node = paths.getNode(parent);
//        Node info = getExistingUserInfo(node);
//
//        if (text.length() > 0) {
//            if (info == null) {
//                info = node.appendChild(doc.createElementNS(DEFAULT_NAMESPACE, "altText"));
//                info.setTextContent(text);
//                return Status.CREATED;
//            }
//            else {
//                info.setTextContent(text);
//                return Status.CHANGED;
//            }
//        }   
//        
//        // TODO ? which is better: "" -> remove the alt text added by the user or set an empty alt text
//        
//        if (info != null) {
//            node.removeChild(info);
//            if (!node.hasChildNodes())
//                parent.removeChild(node);
//            return Status.REMOVED;
//        }
//
//        return Status.NOTHING;
//    }

//    @Override
//    protected String buildXML(String attr, String base, String text) {
//        StringBuffer ret = new StringBuffer();
//        ret.append("<attach ");
//        ret.append(attr);
//        ret.append("=\"");
//        ret.append(base);
//        ret.append("\"");
//        ret.append("><altText>");
//        ret.append(text);
//        ret.append("</altText></attach>");
//        return ret.toString();
//    }
    
//    @Override
//    public String buildXML(Map<String, String> attr, String text) {
//        StringBuffer ret = new StringBuffer();
//        ret.append("<!--attach loc:path=\".\"--><group");
//        for (Map.Entry<String, String> e : attr.entrySet()) {
//            ret.append("\n  ");
//            ret.append(e.getKey());
//            ret.append("=\"");
//            ret.append(e.getValue());
//            ret.append("\"");
//        }
//        ret.append(">\n  <altText>");
//        ret.append(text);
//        ret.append("</altText>\n</group><!--/attach-->");
//        return ret.toString();
//    }
    
}
