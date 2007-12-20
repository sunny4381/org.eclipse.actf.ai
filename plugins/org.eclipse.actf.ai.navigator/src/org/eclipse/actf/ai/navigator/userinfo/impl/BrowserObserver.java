/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Masatomo KOBAYASHI - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.navigator.userinfo.impl;

import org.eclipse.actf.model.IWebBrowserACTF;



public class BrowserObserver {
    private IWebBrowserACTF webBrowser;
    
    public BrowserObserver(IWebBrowserACTF webBrowser) {
        this.webBrowser = webBrowser;
    }
    
    public String getTargetFilter() { // TODO
        String s = webBrowser.getURL();
        int i = s.indexOf('?');
        if (i >= 0)
            s = s.substring(0, i).concat("*");
        return s;
    }

    public IWebBrowserACTF getWebBrowser() {
        return webBrowser;
    }

    public void setWebBrowser(IWebBrowserACTF webBrowser) {
        this.webBrowser = webBrowser;
    }
    
}
