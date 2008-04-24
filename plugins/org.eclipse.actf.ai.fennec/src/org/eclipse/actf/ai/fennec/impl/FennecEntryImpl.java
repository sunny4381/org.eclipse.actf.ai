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

import java.io.File;

import org.eclipse.actf.ai.fennec.IFennecEntry;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.XMLStorePlugin;




public class FennecEntryImpl implements IFennecEntry {
    private IXMLInfo info;

    public String getDocumentation() {
        return info.getDocumentation();
    }

    public boolean isUserEntry() {
        return info.isUserEntry();
    }
        
    public IXMLInfo getIXMLInfo() {
        return info;
    }

    public FennecEntryImpl(IXMLInfo info) {
        this.info = info;
    }

    public boolean export(File dest) {
        return XMLStorePlugin.getDefault().getXMLStoreService().exportMetadata(info, dest);
    }
}
