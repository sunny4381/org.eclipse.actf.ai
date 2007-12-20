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

package org.eclipse.actf.ai.xmlstore;

import java.io.File;

import org.eclipse.actf.ai.xmlstore.local.UserXMLStore;
import org.eclipse.actf.ai.xmlstore.local.XMLStoreLocal;
import org.eclipse.actf.ai.xmlstore.spi.XMLStoreServiceImpl;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class XMLStorePlugin extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.actf.ai.xmlstore";

    // The shared instance
    private static XMLStorePlugin plugin;
	
    /**
     * The constructor
     */
    public XMLStorePlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        initialize();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static XMLStorePlugin getDefault() {
        return plugin;
    }

    
    public File getLocalDir(String sub) {
        return new File(getLocalDir(), sub);
    }

    private File getLocalDir() {
        Bundle bundle = getBundle();
        IPath stateLocationPath = Platform.getStateLocation(bundle);
        return stateLocationPath.toFile();
    }
    
    private static final String[] XML_EXT = new String[]{".xml", ".fnc"};
    
    private void initialize() {
        XMLStoreServiceImpl.getInstance().setUserStore(new UserXMLStore(getLocalDir(UserXMLStore.TEMP_DIR_NAME), XML_EXT));
        XMLStoreServiceImpl.getInstance().addStore(new XMLStoreLocal(getLocalDir(XMLStoreLocal.SYSTEM_DIR_NAME), XML_EXT));
    }
    
    public void addSystemStore(File location) {
        XMLStoreServiceImpl.getInstance().addStore(new XMLStoreLocal(location, XML_EXT));
    }

    public IXMLStoreService getXMLStoreService(){
        return XMLStoreServiceImpl.getInstance();
    }
}
