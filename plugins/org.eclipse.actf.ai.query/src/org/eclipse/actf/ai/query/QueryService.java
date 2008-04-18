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



/**
 * This class has factory methods for creating query objects. 
 */
public class QueryService {
    /**
     * @param e The element to be parsed.
     * @param parentQuery The parent query of the query will be parsed.
     * @return An instance of query for "Fennec".
     */
    public static IQuery parse(Element e, IQuery parentQuery) {
        return QueryImpl.parse(e, parentQuery);
    }

    /**
     * @param xpath The XPath to be used in the query.
     * @return An instance of XPath query.
     */
    public static IQuery createFromXPath(String xpath) {
        return QueryImpl.parseXPath(xpath);
    }
    
    /**
     * @param domTarget The target DOM node to be queried by the created query. 
     * @param usrParent The parent node of the created query.
     * @return An instance of query which returns the domTarget.
     */
    public static Node serializeQuery(Node domTarget, Node usrParent) {
        return QueryImpl.serializeQuery(domTarget, usrParent);
    }
}
