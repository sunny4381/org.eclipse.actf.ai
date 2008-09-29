/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.navigator;

import org.eclipse.actf.ai.navigator.extension.MetadataCacheCleanerExtension;
import org.eclipse.actf.ai.navigator.views.NavigatorTreeView;
import org.eclipse.actf.ai.tts.AbstractUIPluginForTTS;
import org.eclipse.jface.action.IMenuManager;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class NavigatorPlugin extends AbstractUIPluginForTTS {

    public static final String ID = "org.eclipse.actf.ai.navigator";
    //The shared instance.
    private static NavigatorPlugin plugin;
    public static IMenuManager menuManager;
        
    /**
     * The constructor.
     */
    public NavigatorPlugin() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static NavigatorPlugin getDefault() {
        return plugin;
    }

    private NavigatorTreeView navigatorTreeView;
    
	/**
	 * The view ID of the Navigator Tree View.
	 */
	public static final String NAVIGATOR_TREE_VIEW_ID = "org.eclipse.actf.ai.navigator.views.NavigatorTreeView"; //$NON-NLS-1$

    /**
     * To share the instance of the Navigator Tree View in this plug-in.
     * @param navigatorTreeView An instance of the Navigator Tree View.
     */
    public void setNavigatorTreeView(NavigatorTreeView navigatorTreeView) {
        this.navigatorTreeView = navigatorTreeView;
    }
    
    /**
     * @return The shared instance of the Navigator Tree View.
     */
    public NavigatorTreeView getNavigatorTreeView() {
        return navigatorTreeView;
    }
    
    /**
     * Clear the memory cache of the metadata.
     */
    public void clearMetadataCache(){
        MetadataCacheCleanerExtension.clearCache();
    }
}
