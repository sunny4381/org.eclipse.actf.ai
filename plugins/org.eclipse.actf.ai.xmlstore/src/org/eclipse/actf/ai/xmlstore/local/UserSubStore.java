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

import java.util.ArrayList;

import org.eclipse.actf.ai.xmlstore.IXMLSelector;
import org.eclipse.actf.ai.xmlstore.IXMLStore;


public class UserSubStore extends SubStore {

    UserSubStore(XMLFileListStore baseStore, IXMLSelector selector) {
        super(baseStore, selector);
    }

    @Override
    public IXMLStore specify(IXMLSelector selector) {
        return new UserSubStore(this, selector);
    }
    
    @Override
    protected void initSubStore() {
        ArrayList<XMLFile> l = baseStore.list;
        int len = l.size();
        this.list = new ArrayList<XMLFile>();
        for (int i = 0; i < len; i++) {
            XMLFile xf = l.get(i);
            if (xf instanceof UserXML) {
                UserXML ux = (UserXML) xf;
                if (ux.match(selector)) {
                    this.list.add(xf);
                }
            }
        }
    }

}
