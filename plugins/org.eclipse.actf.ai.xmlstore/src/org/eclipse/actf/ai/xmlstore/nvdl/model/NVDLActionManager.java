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

package org.eclipse.actf.ai.xmlstore.nvdl.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>NVDLActionManager</code> manages actions for a NVDL rule.
 */
public class NVDLActionManager {
    private boolean isCancelAction;
    private NVDLResultAction resultAction = null;;
    private List<NVDLNoResultAction> noResultActions;
    
    public boolean isCancelAction() {
    	return isCancelAction;
    }

    public void setCancelAction() {
    	this.isCancelAction = true;
    }

    public void setResultAction(NVDLResultAction resultAction) {
        this.resultAction = resultAction;
    }

    public NVDLResultAction getResultAction() {
        return resultAction;
    }

    public void addNoResultAction(NVDLNoResultAction noResultAction) {
        noResultActions.add(noResultAction);
    }

    public List getNoResultActions() {
        return noResultActions;
    }

    public NVDLActionManager() {
        noResultActions = new ArrayList<NVDLNoResultAction>(0);
    }
}
