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
 * The <code>NVDLNoResultAction</code> is a base abstract class
 * for NVDL `NoResultAction.'
 */
public abstract class NVDLNoResultAction extends NVDLAction {
    public NVDLModel visitModel(NVDLModelVisitor v)
        throws NVDLModelException {
        return v.visitNVDLNoResultAction(this);
    }

    NVDLNoResultAction(String name, String useModeName, NVDLRule belongingRule) {
        super(name, useModeName, belongingRule);
    }
}
