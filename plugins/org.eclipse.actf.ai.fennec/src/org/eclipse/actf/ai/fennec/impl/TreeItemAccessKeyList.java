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

import org.eclipse.actf.ai.fennec.treemanager.IAccessKeyList;
import org.eclipse.actf.model.dom.dombycom.AnalyzedResult;
import org.eclipse.actf.model.dom.dombycom.INodeEx;




public class TreeItemAccessKeyList implements IAccessKeyList {
    INodeEx[] accessKeyNodes;

    public TreeItemAccessKeyList(AnalyzedResult analyzedResult) {
        accessKeyNodes = analyzedResult.getAccessKeyNodes();
    }

    public char getAccessKeyAt(int index) {
        if (accessKeyNodes.length <= index)
            return 0;
        return accessKeyNodes[index].getAccessKey();
    }

    public String getUIStringAt(int index) {
        // TODO
        return "";
    }

    public int size() {
        return accessKeyNodes.length;
    }

    public static IAccessKeyList newAccessKeyList(AnalyzedResult analyzedResult) {
        return new TreeItemAccessKeyList(analyzedResult);
    }

}
