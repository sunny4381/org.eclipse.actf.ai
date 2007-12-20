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

package org.eclipse.actf.ai.fennec.impl;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class NodeUtil {
    private static StringBuffer extractNodeString(StringBuffer buf, Node n) {
        if (n instanceof INodeEx) {
            INodeEx n2 = (INodeEx) n;
            
            // TODO: should be rewritten!!
            // changed by daisuke
            String n2String = n2.extractString();
            buf.append(n2String);
            if (n2String != null && n2String.length() > 0) // if node has text
                buf.append(" "); // insert default separator
        }
        NodeList nl = n.getChildNodes();
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            buf = extractNodeString(buf, nl.item(i));
        }
        return buf;
    }

    public static String extractString(NodeList nl) {
        StringBuffer buf = new StringBuffer();
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            Node n = nl.item(i);
            buf = extractNodeString(buf, n);
        }
        return buf.toString();
    }

    public static String extractString(Node n) {
        StringBuffer buf = new StringBuffer();
        buf = extractNodeString(buf, n);
        return buf.toString();
    }

    public static String extractString(ITreeItem item) {
        Node baseNode = (Node) item.getBaseNode();
        if (baseNode != null) {
            return extractString(baseNode);
        }
        return "";
    }
}
