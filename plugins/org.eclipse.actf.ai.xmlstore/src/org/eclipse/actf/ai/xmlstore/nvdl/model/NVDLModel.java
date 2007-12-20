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
 * The <code>NVDLModel</code> is the base abstract class for any NVDL model.
 */
public abstract class NVDLModel {
    public abstract NVDLModel visitModel(NVDLModelVisitor v) throws NVDLModelException;

    private Location loc = null;
    public void setLocation(Location loc) {
        this.loc = loc;
    }
    public Location getLocation() {
        return loc;
    }
    public void copyLocation(NVDLModel src) {
        this.loc = src.loc;
    }
}
