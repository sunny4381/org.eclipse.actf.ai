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
package org.eclipse.actf.ai.navigator.broker;

import java.lang.reflect.Method;

import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.navigator.impl.NavigatorImpl;
import org.eclipse.actf.ai.navigator.impl.WebEventListener;
import org.eclipse.actf.model.IWebBrowserACTF;
import org.eclipse.actf.util.timer.Yield;


public class RequestBroker {
    public static final int SHOW_STATUS = 100;
	
    public static final int EVENT_NEW_URL = 101;

    public static final int EVENT_NEWPAGE_READY = 1000;
	
    public static final int EVENT_LOAD_STARTING = 1001;
	
    public static final int EVENT_WAIT_FOR_PROCESSING = 1200;
	
    public static final int EVENT_INFORMATION_UPDATED = 1201;

    public static final int EVENT_TREE_MODIFIED = 1202;

    public static final int EVENT_NOTIFICATION = 1203;
	
    public static final int EVENT_ALERT_MODAL = 1204;
	
    public static final int EVENT_AUTOMATIC_TRANSITION = 1205;
	
    private final WebEventListener webEventListener;
    private NavigatorImpl navigator;
    private IWebBrowserACTF webBrowser;

    private boolean enabled;

    private Method registerRequestBrokerMethod;
    private Method sendEventMethod;

    public void setNavigator(NavigatorImpl navigator, IWebBrowserACTF webBrowser) {
        this.navigator = navigator;
        this.webBrowser = webBrowser;
    }

    private void registerProxy() {
        if (!enabled) return;
        try {
            Object o = Thread.currentThread();
            registerRequestBrokerMethod.invoke(o, new Object[] { this });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEvent(int id, Object param) {
        if (!enabled) return;
        System.err.println("EventRP:" + id + " Param:" + param);
        try {
            Object o = Thread.currentThread();
            sendEventMethod.invoke(o, new Object[] { new Integer(id), param });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newPageReady() {
        sendEvent(EVENT_NEW_URL, webBrowser.getURL());
        sendEvent(EVENT_NEWPAGE_READY, null);
    }

    public Object invokeNavigator(String signature, Object[] args) throws Exception {
        Mirror m = new Mirror(navigator);
        Method method = m.getMethod(signature);
        return Yield.syncInvoke(method, navigator, args);
    }

    public Object invokeTreeManager(String signature, Object[] args) throws Exception {
        ITreeManager treeManager = navigator.getTreeManager();
        Mirror m = new Mirror(treeManager);
        Method method = m.getMethod(signature);
        return Yield.syncInvoke(method, treeManager, args);
    }

    public Object invokeITreeItem(Object item, String signature, Object[] args) throws Exception {
        Mirror m = new Mirror(item);
        Method method = m.getMethod(signature);
        return Yield.syncInvoke(method, item, args);
    }

    private void initialize() {
        Object o = Thread.currentThread();
        Class c = o.getClass();
        try {
            registerRequestBrokerMethod = c.getMethod("registerRequestBroker", new Class[] { Object.class });
            sendEventMethod = c.getMethod("sendEvent", new Class[] { java.lang.Integer.TYPE, Object.class });
            enabled = true;
        } catch (NoSuchMethodException e) {
            enabled = false;
        }
    }

    public RequestBroker(WebEventListener webEventListener) {
    	this.webEventListener = webEventListener;
        initialize();
        registerProxy();
    }
}
