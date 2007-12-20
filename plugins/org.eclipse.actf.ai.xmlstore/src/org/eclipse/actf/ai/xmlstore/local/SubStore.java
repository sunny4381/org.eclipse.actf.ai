/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation.
 *    Daisuke SATO - Created it as the distinct class.
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore.local;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;

class SubStore extends XMLFileListStore {
    protected final XMLFileListStore baseStore;
    protected final IXMLSelector selector;

    static class InfoIterator implements Iterator<IXMLInfo> {
        private int i;
        private final ArrayList<XMLFile> list;
        public boolean hasNext() {
            if (i == list.size()) return false;
            return true;
        }

        public IXMLInfo next() {
            return list.get(i++); 
        }

        public void remove() {
        }
        
        InfoIterator(ArrayList<XMLFile> list) {
            this.i = 0;
            this.list = list;
        }
    }
    
    public Iterator<IXMLInfo> getInfoIterator() {
        return new InfoIterator(list);
    }

    public IXMLStore specify(IXMLSelector selector) {
        return new SubStore(this, selector);
    }
    
    protected void initSubStore() {
        ArrayList<XMLFile> l = baseStore.list; 
        int len = l.size();
        this.list = new ArrayList<XMLFile>();
        for (int i = 0; i < len; i++) {
            XMLFile xf = l.get(i);
            if (xf.getSelectorInfo().match(selector)) {
                this.list.add(xf);
            }
        }
    }

    SubStore(XMLFileListStore baseStore, IXMLSelector selector) {
        this.baseStore = baseStore;
        this.selector = selector;
        initSubStore();
    }

    public void refleshAll() {
        baseStore.refleshAll();
        initSubStore();
    }
}
