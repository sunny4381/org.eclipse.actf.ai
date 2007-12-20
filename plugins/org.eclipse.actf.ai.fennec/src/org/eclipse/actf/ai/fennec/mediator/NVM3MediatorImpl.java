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

package org.eclipse.actf.ai.fennec.mediator;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.actf.ai.fennec.INVM3Entry;
import org.eclipse.actf.ai.fennec.INVM3Mediator;
import org.eclipse.actf.ai.fennec.INVM3Service;
import org.eclipse.actf.ai.fennec.NVM3ServiceFactory;
import org.eclipse.actf.ai.fennec.impl.NVM3DOMReader;
import org.eclipse.actf.ai.fennec.impl.NVM3EntryImpl;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerFactory;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStorePlugin;
import org.eclipse.actf.model.IWebBrowserACTF;
import org.eclipse.actf.model.dom.dombycom.IDocumentEx;




public class NVM3MediatorImpl implements INVM3Mediator {
    private final IWebBrowserACTF webBrowser;

    public ITreeManager newTreeManager(INVM3Entry entry) {
        IDocumentEx doc = (IDocumentEx) webBrowser.getLiveDocument();

        INVM3Service nvm3Service;
        if (entry != null) {
            try {
                nvm3Service = NVM3ServiceFactory.newNVM3Service(entry, doc);
            } catch (Exception e) {
                nvm3Service = NVM3ServiceFactory.newNVM3ServiceWithDefaultMetadata(doc);
            }
        } else {
            nvm3Service = NVM3ServiceFactory.newNVM3ServiceWithDefaultMetadata(doc);
        }
        return TreeManagerFactory.newITreeManager(nvm3Service);
    }

    private IXMLStore getNVM3Store(String url) {
        IXMLStoreService ss = XMLStorePlugin.getDefault().getXMLStoreService();
        IXMLSelector selector = ss.getSelectorWithDocElem(NVM3DOMReader.NVM3_DOCUMENT_ELEMENT_NAME,
                                                          NVM3DOMReader.NVM3_NAMESPACE_URI);
        IXMLStore store = ss.getRootStore();
        store = store.specify(selector);
        if (store == null) return null;
        selector = ss.getSelectorWithIRI(url);
        return store.specify(selector);
    }

    public INVM3Entry getDefaultNVM3Entry() {
        String url = webBrowser.getURL();
        IXMLStore store = getNVM3Store(url);
        if (store == null) return null;
        Iterator<IXMLInfo> it = store.getInfoIterator();
        if (it == null) return null;
        if (!it.hasNext()) return null;
        IXMLInfo info = it.next();
        return new NVM3EntryImpl(info);
    }

    public INVM3Entry[] getNVM3Entries() {
        String url = webBrowser.getURL();
        IXMLStore store = getNVM3Store(url);
        if (store == null) return null;
        Iterator<IXMLInfo> it = store.getInfoIterator();
        if (it == null) return null;
        ArrayList<NVM3EntryImpl> entries = new ArrayList<NVM3EntryImpl>();
        while (it.hasNext()) {
            IXMLInfo info = it.next();
            if (info == null) continue;
            entries.add(new NVM3EntryImpl(info));
        }
        INVM3Entry[] ea = new INVM3Entry[entries.size()];
        ea = entries.toArray(ea);
        return ea;
    }

    public void release() {
        // this.dombycom.release();
    }

    public NVM3MediatorImpl(IWebBrowserACTF webBrowser) {
        this.webBrowser = webBrowser;
    }
}


