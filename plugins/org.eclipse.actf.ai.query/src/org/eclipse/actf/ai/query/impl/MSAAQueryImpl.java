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

import org.eclipse.actf.model.dom.dombycom.IMSAANode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class MSAAQueryImpl {
    private static final String MSAA_QUERY_NS = "http://www.ibm.com/xmlns/prod/aiBrowser/fennec/msaa-query";
    private String[] ids;

    private MSAAQueryImpl(String[] ids) {
        this.ids = ids;
    }
    
    public boolean hasTarget() {
        return (ids != null);
    }

    public List query(Node base) {
        if (ids == null) return null;
        if (!(base instanceof IMSAANode)) return null;
        IMSAANode mn = (IMSAANode) base;

        List<IMSAANode> r = new ArrayList<IMSAANode>();
        for (int i = 0; i < ids.length; i++) {
            IMSAANode n = mn.searchByID(ids[i]);
            if (n != null) r.add(n);
        }
        return r;
    }

    static MSAAQueryImpl parse(Element e, MSAAQueryImpl parentQuery) {
        String[] ids = null;
        String idrefs = e.getAttributeNS(MSAA_QUERY_NS, "idrefs");
        if (idrefs.length() > 0) {
            ids = idrefs.split("[ \r\n\t]");
        }
        return new MSAAQueryImpl(ids);
    }
    
    static Attr serializeQuery(Node domNode, Node usrNode) {
        if (!(domNode instanceof IMSAANode))
            return null;
        Document doc = usrNode.getOwnerDocument();
        String target = ((IMSAANode)domNode).getID();
        if (target != null && target.length() > 0) {
            Attr attr = doc.createAttributeNS(MSAA_QUERY_NS, "msq:idrefs");
            attr.setNodeValue(target);
            return attr;
        }
        return null;
    }

}
