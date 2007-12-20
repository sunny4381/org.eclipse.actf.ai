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

package org.eclipse.actf.ai.query;

import org.eclipse.actf.ai.query.impl.QueryImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class QueryService {
    public static IQuery parse(Element e, IQuery parentQuery) {
        return QueryImpl.parse(e, parentQuery);
    }

    public static IQuery createFromXPath(String xpath) {
        return QueryImpl.parseXPath(xpath);
    }
    
    /*
     * domTarget : 
     * usrParent : <attach> node in user metadata DOM
     * 
     * return    : <node> node in the specified <attach> node
     */
    public static Node serializeQuery(Node domTarget, Node usrParent) {
        return QueryImpl.serializeQuery(domTarget, usrParent);
    }
}
