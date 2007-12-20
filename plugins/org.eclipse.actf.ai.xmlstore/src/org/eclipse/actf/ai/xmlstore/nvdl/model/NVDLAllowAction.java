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
 * The <code>NVDLAllowAction</code> is for NVDL `allow' action.
 */
public class NVDLAllowAction extends NVDLNoResultAction {
    public String toString() {
        return "Allow";
    }

    public NVDLAllowAction(String name, String useModeName, NVDLRule belongingRule) {
        super(name, useModeName, belongingRule);
    }
}
