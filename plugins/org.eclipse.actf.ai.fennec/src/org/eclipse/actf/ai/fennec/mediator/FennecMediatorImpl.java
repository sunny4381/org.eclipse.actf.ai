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

import org.eclipse.actf.ai.fennec.IFennecEntry;
import org.eclipse.actf.ai.fennec.IFennecMediator;
import org.eclipse.actf.ai.fennec.IFennecService;
import org.eclipse.actf.ai.fennec.FennecServiceFactory;
import org.eclipse.actf.ai.fennec.impl.FennecDOMReader;
import org.eclipse.actf.ai.fennec.impl.FennecEntryImpl;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerFactory;
import org.eclipse.actf.ai.xmlstore.IXMLInfo;
import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;
import org.eclipse.actf.ai.xmlstore.IXMLStoreService;
import org.eclipse.actf.ai.xmlstore.XMLStorePlugin;
import org.eclipse.actf.model.IWebBrowserACTF;
import org.eclipse.actf.model.dom.dombycom.IDocumentEx;




public class FennecMediatorImpl implements IFennecMediator {
    private final IWebBrowserACTF webBrowser;

    public ITreeManager newTreeManager(IFennecEntry entry) {
        IDocumentEx doc = (IDocumentEx) webBrowser.getLiveDocument();

        IFennecService fennecService;
        if (entry != null) {
            try {
                fennecService = FennecServiceFactory.newFennecService(entry, doc);
            } catch (Exception e) {
                fennecService = FennecServiceFactory.newFennecServiceWithDefaultMetadata(doc);
            }
        } else {
            fennecService = FennecServiceFactory.newFennecServiceWithDefaultMetadata(doc);
        }
        return TreeManagerFactory.newITreeManager(fennecService);
    }

    private IXMLStore getFennecStore(String url) {
        IXMLStoreService ss = XMLStorePlugin.getDefault().getXMLStoreService();
        IXMLSelector selector = ss.getSelectorWithDocElem(FennecDOMReader.Fennec_DOCUMENT_ELEMENT_NAME,
                                                          FennecDOMReader.Fennec_NAMESPACE_URI);
        IXMLStore store = ss.getRootStore();
        store = store.specify(selector);
        if (store == null) return null;
        selector = ss.getSelectorWithURI(url);
        return store.specify(selector);
    }

    public IFennecEntry getDefaultFennecEntry() {
        String url = webBrowser.getURL();
        IXMLStore store = getFennecStore(url);
        if (store == null) return null;
        Iterator<IXMLInfo> it = store.getInfoIterator();
        if (it == null) return null;
        if (!it.hasNext()) return null;
        IXMLInfo info = it.next();
        return new FennecEntryImpl(info);
    }

    public IFennecEntry[] getFennecEntries() {
        String url = webBrowser.getURL();
        IXMLStore store = getFennecStore(url);
        if (store == null) return null;
        Iterator<IXMLInfo> it = store.getInfoIterator();
        if (it == null) return null;
        ArrayList<FennecEntryImpl> entries = new ArrayList<FennecEntryImpl>();
        while (it.hasNext()) {
            IXMLInfo info = it.next();
            if (info == null) continue;
            entries.add(new FennecEntryImpl(info));
        }
        IFennecEntry[] ea = new IFennecEntry[entries.size()];
        ea = entries.toArray(ea);
        return ea;
    }

    public void release() {
        // this.dombycom.release();
    }

    public FennecMediatorImpl(IWebBrowserACTF webBrowser) {
        this.webBrowser = webBrowser;
    }
}


