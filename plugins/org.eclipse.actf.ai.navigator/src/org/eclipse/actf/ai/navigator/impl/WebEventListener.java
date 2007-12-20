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
package org.eclipse.actf.ai.navigator.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

import org.eclipse.actf.ai.fennec.INVM3Mediator;
import org.eclipse.actf.ai.fennec.NVM3Plugin;
import org.eclipse.actf.ai.fennec.treemanager.ILocation;
import org.eclipse.actf.ai.navigator.NavigatorPlugin;
import org.eclipse.actf.ai.navigator.broker.RequestBroker;
import org.eclipse.actf.ai.navigator.extension.ManipulatorExtension;
import org.eclipse.actf.ai.navigator.extension.MediaControlExtension;
import org.eclipse.actf.ai.navigator.extension.ScreenReaderExtension;
import org.eclipse.actf.model.IModelServiceHolder;
import org.eclipse.actf.model.IWebBrowserACTF;
import org.eclipse.actf.model.events.IWebBrowserACTFEventListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


public class WebEventListener implements IWebBrowserACTFEventListener {

    private static final String ABOUT_BLANK_URL = "about:blank";

    private NVM3Plugin getNVM3Plugin() {
        return NVM3Plugin.getDefault();
    }

    // --------------------------------------------------------------------------------
    //  Browser State: It keeps the state of the browser by receiving Web Browser
    //  Events.  In overall, we should encapsulate the stateful information around the browser
    //  into this class.
    // --------------------------------------------------------------------------------
    public class BrowserState {
        public static final int STATE_NONE = -1;

        public static final int STATE_UNINIT = 0;

        public static final int STATE_PROGRESS = 1;

        public static final int STATE_NAVIGATECOMPLETED = 3;

        public static final int STATE_STARTED = 10;

        boolean shouldRecord;
        boolean initFlag;
        ILocation savedLocation;
        ILocation savedLocationForMyRefresh;

        NavigatorImpl navigator;

        public NavigatorImpl getNavigator() {
            return navigator;
        }

        int state;


        public int getState() {
            return state;
        }

        void resetState() {
            this.state = STATE_UNINIT;
        }

        void setNoState() {
            this.state = STATE_NONE;
        }

        void forwardState(int newst) {
            if (this.state < newst) this.state = newst;
        }

        BrowserState(IWebBrowserACTF webBrowser) {
            this.navigator = new NavigatorImplEx(WebEventListener.this, webBrowser, 30, 1000);
            NavigatorPlugin.getDefault().setNavigatorUI(this.navigator);
            this.state = STATE_NONE;
        }
    }

    private HashMap<IWebBrowserACTF, BrowserState> browserStateMap;

    private RequestBroker requestBroker;
    
    private final TripJournal tripJournal;

    private final BrowserControlImpl browserControl;

    public WebEventListener() {
        this.browserStateMap = new HashMap<IWebBrowserACTF, BrowserState>();
        this.tripJournal = new TripJournal();
        this.browserControl = new BrowserControlImpl(this, this.tripJournal);
        IWebBrowserACTF.WebBrowserNavigationEventListnerHolder.LISTENER = this.browserControl;
        ManipulatorExtension.setBrowserControl(this.browserControl);

        requestBroker = new RequestBroker(this);
    }
    
    public synchronized BrowserState getBrowserState(IWebBrowserACTF webBrowser) {
        BrowserState bs = browserStateMap.get(webBrowser);
        if (bs == null) {
            bs = new BrowserState(webBrowser);
            browserStateMap.put(webBrowser, bs);
        }
        return bs;
    }

    public void forceRestart(IWebBrowserACTF webBrowser) {
        startNavigation(webBrowser, false);
    }

    public NavigatorImpl getFocused() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorPart editor = page.getActiveEditor();

        if (editor == null) return null;
        IModelServiceHolder modelServiceHolder = (IModelServiceHolder) editor;
        IWebBrowserACTF wb = (IWebBrowserACTF) modelServiceHolder.getModelService();
        BrowserState bs = browserStateMap.get(wb);
        if (bs == null) return null;

        return bs.getNavigator();
    }

    // --------------------------------------------------------------------------------
    //  Event Handlers
    // --------------------------------------------------------------------------------
    
    private static final boolean EVENT_DEBUG = false;
    
    private void startNavigation(IWebBrowserACTF webBrowser) {
        startNavigation(webBrowser, false);
    }

    private void startNavigation(IWebBrowserACTF webBrowser, boolean isRefresh) {
        tripJournal.tripEnd();
        BrowserState bs = getBrowserState(webBrowser);
        if (bs.state >= BrowserState.STATE_STARTED)
            return;
        
        if (EVENT_DEBUG) System.err.println(webBrowser.getURL() + ", isRefresh=" + isRefresh + ", " + webBrowser);
        
        if (bs.state == BrowserState.STATE_STARTED) {
            MediaControlExtension.doDispose(bs.navigator.getMediaControlHandle());
        }
        MediaControlExtension.start(bs.navigator.getMediaControlHandle());

        INVM3Mediator mediator = getNVM3Plugin().newNVM3Mediator(webBrowser);
        bs.navigator.setNVM3Mediator(mediator);
        bs.navigator.startNavigation(webBrowser, !isRefresh);
        requestBroker.setNavigator(bs.navigator, webBrowser);

        bs.forwardState(BrowserState.STATE_STARTED);
        requestBroker.newPageReady();
    }

    private static final String FILE_SCHEME_SUFFIX = "file:///";
    private String unifyURLFileScheme(String url) {
        if (url.startsWith(FILE_SCHEME_SUFFIX)) {
            url = url.substring(FILE_SCHEME_SUFFIX.length());
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return url.replace('/', '\\');
        }
        return url;
    }

    private boolean urlEquals(String url1, String url2) {
        if ((url1 == null) || (url2 == null)) return false;
        return unifyURLFileScheme(url1).equals(unifyURLFileScheme(url2));
    }

    public void navigateComplete(IWebBrowserACTF webBrowser, String url) { 
        if (EVENT_DEBUG) System.out.println("navigateComplete " + url + ", "
                                            + webBrowser.getURL() + " | " + webBrowser);
        
        if (!urlEquals(url, webBrowser.getURL())) return;

        BrowserState bs = getBrowserState(webBrowser);
        if ((bs.shouldRecord)
            && (bs.state < BrowserState.STATE_NAVIGATECOMPLETED)) {
            tripJournal.recordJournal(bs.navigator,
                                      bs.savedLocation,
                                      webBrowser.getURL(),
                                      bs.initFlag);
        }
        bs.forwardState(BrowserState.STATE_NAVIGATECOMPLETED);
        bs.navigator.navigateComplete();
    }

    public void titleChange(IWebBrowserACTF webBrowser, String title) {
        if (EVENT_DEBUG) System.out.println("titleChange " + title + ", "+ webBrowser);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        IEditorPart editor = page.getActiveEditor();
        if (editor instanceof IModelServiceHolder) {
            if (webBrowser != ((IModelServiceHolder) editor).getModelService()) {
                return;
            }
        }

        setWindowTitle(title);
    }

    public void dispose() {
    }

    public void progressChange(IWebBrowserACTF webBrowser, int progress, int progressMax) {
        if (EVENT_DEBUG) System.out.println("progressChange progress=" + progress + ", progressMax=" + progressMax + ", " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        bs.forwardState(BrowserState.STATE_PROGRESS);
        bs.navigator.progressChange(progress, progressMax);
    }

    public void myDocumentComplete(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("myDocumentComplete " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        
        if (!(ABOUT_BLANK_URL.equals(webBrowser.getURL()))) {
            bs.navigator.speakTab(false);
            startNavigation(webBrowser);
        } else {
            bs.setNoState();
        }
    }

    public void focusChange(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("focusChange " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        ManipulatorExtension.setNavigator(bs.navigator);
        requestBroker.setNavigator(bs.navigator, webBrowser);
        MediaControlExtension.start(bs.navigator.getMediaControlHandle());
        setWindowTitle();
    }

    private void setWindowTitle(String title) {
        String productName = Platform.getProduct().getName();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(title + " - " + productName);
    }

    private void setWindowTitle() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        String title = page.getActiveEditor().getTitle();
        setWindowTitle(title);
    }

    public void beforeNavigate(IWebBrowserACTF webBrowser,
                               String url,
                               String targetFrameName,
                               boolean isInNavigation) {
        if (EVENT_DEBUG) System.out.println("beforeNavigate " + url + ", " + targetFrameName + ", isInNavigation=" + isInNavigation);
        BrowserState bs = getBrowserState(webBrowser);
        //if (isInNavigation) {
        if (!url.startsWith("javascript")) {
            if (!(ABOUT_BLANK_URL.equals(url))) {
                if (bs.state == BrowserState.STATE_NONE) {
                    bs.initFlag = true;
                } else {
                    bs.initFlag = false;
                }
                bs.shouldRecord = true;
                bs.savedLocation = bs.navigator.getLocation();
            } else {
                bs.shouldRecord = false;
            }
            bs.resetState();
            bs.navigator.beforeNavigation(webBrowser.getURL());
        } else {
            bs.shouldRecord = false;
        }
        ScreenReaderExtension.takeBackControl(webBrowser);
    }

    public void browserDisposed(IWebBrowserACTF webBrowser, String title) {
        if (EVENT_DEBUG) System.out.println("browserDisposed " + title);
        BrowserState bs = getBrowserState(webBrowser);
        bs.navigator.speakCloseTab(title);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        if ((page == null) || (page.getActiveEditor() == null)) {
            setWindowTitle("No Tab");
        }
    }

    public void myRefresh(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("myRefresh " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        bs.resetState();
        bs.savedLocationForMyRefresh = bs.navigator.getLocation();
        bs.navigator.beforeNavigation(webBrowser.getURL());
    }

    public void myRefreshComplete(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("Refresh Complete " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        bs.navigator.restoreLocation(bs.savedLocationForMyRefresh);
        startNavigation(webBrowser, true);
    }

    public void navigateStop(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("navigateStop " + webBrowser);
    }

    public void focusGainedOfAddressText(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("focusGainedOfAddressText " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        bs.navigator.enterBrowserAddress();
    }

    public void focusLostOfAddressText(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("focusLostOfAddressText " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        bs.navigator.exitFormMode();
    }

    public void newWindow(IWebBrowserACTF webBrowser) {
        if (EVENT_DEBUG) System.out.println("newWindow " + webBrowser);
        BrowserState bs = getBrowserState(webBrowser);
        bs.navigator.speakOpenTab();
    }

}
