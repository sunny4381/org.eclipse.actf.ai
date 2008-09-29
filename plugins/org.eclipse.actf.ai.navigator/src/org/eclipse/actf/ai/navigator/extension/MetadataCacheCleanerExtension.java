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

package org.eclipse.actf.ai.navigator.extension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.actf.ai.internal.navigator.NavigatorPlugin;
import org.eclipse.actf.ai.navigator.IMetadataCacheCleaner;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;




public class MetadataCacheCleanerExtension {
    private static final String TAG_CLEANER = "cleaner";
    private static final String ATTR_CLASS = "class";

    private static MetadataCacheCleanerExtension[] cachedExtensions;

    public static MetadataCacheCleanerExtension[] getExtensions() {
        if (cachedExtensions != null) return cachedExtensions;

        IExtension[] extensions = Platform.getExtensionRegistry()
            .getExtensionPoint(NavigatorPlugin.ID, "MetadataCacheCleaner")
            .getExtensions();
        List<MetadataCacheCleanerExtension> l = new ArrayList<MetadataCacheCleanerExtension>();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configElements =
                extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                MetadataCacheCleanerExtension ex = parseExtension(configElements[j], l.size());
                if (ex != null) l.add(ex);
            }
        }
        cachedExtensions = l.toArray(new MetadataCacheCleanerExtension[l.size()]);
        return cachedExtensions;
    }

    private static MetadataCacheCleanerExtension parseExtension(IConfigurationElement configElement, int idx) {
        if (!configElement.getName().equals(TAG_CLEANER))
            return null;
        try {
            return new MetadataCacheCleanerExtension(configElement);
        } catch (Exception e) {
        }
        return null;
    }

    public static void disposeExtensions() {
        if (cachedExtensions == null) return;
        for (int i = 0; i < cachedExtensions.length; i++) {
            cachedExtensions[i].dispose();
        }
        cachedExtensions = null;
    }
    
    public static void clearCache(){
        MetadataCacheCleanerExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getMetadataCacheCleaner().clearCache();
        }
    }

    private final IConfigurationElement configElement;
    private IMetadataCacheCleaner cleaner;

    private MetadataCacheCleanerExtension(IConfigurationElement configElement) {
        this.configElement = configElement;
    }

    private IMetadataCacheCleaner getMetadataCacheCleaner() {
        if (cleaner != null) return cleaner;
        try {
            cleaner = (IMetadataCacheCleaner) configElement.createExecutableExtension(ATTR_CLASS);
        } catch (Exception e) {
        }
        return cleaner;
    }

    private void dispose() {
        if (cleaner == null) return;
        cleaner = null;
    }
}
