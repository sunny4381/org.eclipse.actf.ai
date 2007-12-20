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


/**
 * The <code>NVDLRejectAction</code> is for NVDL `reject' action.
 */
public class NVDLRejectAction extends NVDLNoResultAction {
    public String toString() {
        return "Reject";
    }

    public NVDLRejectAction(String name, String useModeName, NVDLRule belongingRule) {
        super(name, useModeName, belongingRule);
    }
}
