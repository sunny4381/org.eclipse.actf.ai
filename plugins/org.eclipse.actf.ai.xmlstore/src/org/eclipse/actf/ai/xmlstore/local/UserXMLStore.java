/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore.local;

import java.io.File;

import org.eclipse.actf.ai.xmlstore.IXMLEditableInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;

public class UserXMLStore extends XMLStoreLocal {
    public static final String TEMP_DIR_NAME = "user";

    IXMLSelector selector;

    private File dir;

    public UserXMLStore(File dir, String[] extensions) {
        super(dir, extensions);
        this.dir = dir;
        dir.mkdirs();
    }

    @Override
    protected void register(File file) {
        try {
            UserXML xf = new UserXML(file);
            list.add(xf);
        } catch (XMLStoreException e) {
        }
    }
    
    public IXMLEditableInfo newXML(String namespaceURI, String qualifiedName, String targetUriPattern) throws XMLStoreException {
        UserXML ux = new UserXML(namespaceURI, qualifiedName, targetUriPattern, dir);
        list.add(ux);
        return ux;
    }
    
    @Override
    public IXMLStore specify(IXMLSelector selector) {
        return new UserSubStore(this, selector);
    }
    
    @Override
    protected void registerDir(File dir) {
        File[] fileList = dir.listFiles();
        if (fileList == null) return;
        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            if (f.isDirectory()) {
                registerDir(f);
            } else if (isNotTemporal(f)){
                register(f);
            }
        }
    }
}
