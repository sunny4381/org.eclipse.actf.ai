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
 * The <code>NVDLResultAction</code> encapsulates all kinds of
 * NVDL `ResultAction.'
 */
public class NVDLResultAction extends NVDLAction {
    public static final int TYPE_ATTACH = 1;
    public static final int TYPE_ATTACHPLACEHOLDER = 2;
    public static final int TYPE_UNWRAP = 3;

    public final int type;

    public NVDLModel visitModel(NVDLModelVisitor v)
        throws NVDLModelException {
        return v.visitNVDLResultAction(this);
    }
    
    public String toString() {
        switch (type) {
        case TYPE_ATTACH:
            return "Attach:";
        case TYPE_ATTACHPLACEHOLDER:
            return "AttachPlaceHolder:";
        case TYPE_UNWRAP:
            return "Unwrap:";
        }
        return "";
    }

    public NVDLResultAction(String name, String useModeName, int type,
                            NVDLRule belongingRule) {
        super(name, useModeName, belongingRule);
        this.type = type;
        assert (type >= TYPE_ATTACH) && (type <= TYPE_UNWRAP);
    }
}
