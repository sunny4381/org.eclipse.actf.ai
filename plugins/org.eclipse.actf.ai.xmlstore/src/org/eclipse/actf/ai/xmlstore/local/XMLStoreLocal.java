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

package org.eclipse.actf.ai.xmlstore.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.ai.xmlstore.local.SubStore.InfoIterator;

public class XMLStoreLocal extends XMLFileListStore {
    public static final String SYSTEM_DIR_NAME = "system";
    
    private final File directory;

	private final String[] extensions;

    protected void register(File file) {
        try {
            XMLFile xf = new XMLFile(file);
            list.add(xf);
        } catch (XMLStoreException e) {
        }
    }

    protected void registerDir(File dir) {
        if (!dir.isDirectory()) return;
        
        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            File f = fileList[i];
            if (f.isDirectory()) {
                registerDir(f);
            } else if (isNotTemporal(f) && matchExtension(f)){
                register(f);
            }
        }
    }
    
    protected boolean matchExtension(File f) {
    	String name = f.getName();
    	boolean result = false;
        for (int i = 0; i < extensions.length; i++) {
        	result = result || name.endsWith(extensions[i]);
        }
        return result;
    }

    protected boolean isNotTemporal(File f) {
        String name = f.getName();
        if (name.startsWith("#") && name.endsWith("#"))
            return false;
        if (name.endsWith("~"))
            return false;
        return true;
    }

    private void init() {
        this.list = new ArrayList<XMLFile>();
        
        registerDir(directory);
    }

    public Iterator<IXMLInfo> getInfoIterator() {
        return new InfoIterator(list);
    }

    public IXMLStore specify(IXMLSelector selector) {
        return new SubStore(this, selector);
    }

    public void refleshAll() {
        init();
    }

    public XMLStoreLocal(File directory, String[] extensions) {
        this.directory = directory;
        this.extensions = extensions;
        init();
    }

    public static XMLFile newXMLFile(File file) throws XMLStoreException {
        return new XMLFile(file);
    }
}
