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

package org.eclipse.actf.ai.xmlstore.spi;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;

public class XMLStoreAggregator implements IXMLStore {
    private final ArrayList<IXMLStore> stores;

    public IXMLStore specify(IXMLSelector selector) {
        int len = stores.size();
        ArrayList<IXMLStore> newStores = new ArrayList<IXMLStore>(len);
        for (int i = 0; i < len; i++) {
            IXMLStore st = stores.get(i);
            IXMLStore st2 = st.specify(selector);
            if (st2 != null) {
                newStores.add(st2);
            }
        }
        if (newStores.size() > 0) {
            return new XMLStoreAggregator(newStores);
        } else {
            return null;
        }
    }

    public void refleshAll() {
        int len = stores.size();
        for (int i = 0; i < len; i++) {
            IXMLStore st = stores.get(i);
            st.refleshAll();
        }
    }

    public void addStore(IXMLStore store) {
        stores.add(store);
    }
    
    private XMLStoreAggregator(ArrayList<IXMLStore> stores) {
        this.stores = stores;
    }
    
    XMLStoreAggregator() {
        this.stores = new ArrayList<IXMLStore>();
    }

    private class AggregatorIterator implements Iterator<IXMLInfo> {
        private int idx;
        private Iterator<IXMLInfo> currentIterator;
        private IXMLInfo next;

        private void setNext(){
            if (currentIterator != null) {
                if (currentIterator.hasNext()){
                    this.next = currentIterator.next();
                    return;
                }
            }
            
            int size = stores.size();
            if (idx < size) {
                IXMLStore store = stores.get(idx);
                currentIterator = store.getInfoIterator();
                idx++;
                setNext();
                return;
            }
            this.next = null;
            return;
        }
        

        AggregatorIterator() {
            this.idx = 0;
            setNext();
        }

        public boolean hasNext() {
            return (next != null);
        }

        public IXMLInfo next() {
            IXMLInfo ret = next;
            setNext();
            return ret;
        }

        public void remove() {
            // Do nothing;
        }
    }

    public Iterator<IXMLInfo> getInfoIterator() {
        return new AggregatorIterator();
    }

}
