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

package org.eclipse.actf.ai.navigator.extension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.actf.ai.navigator.IMediaControl;
import org.eclipse.actf.ai.navigator.NavigatorPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;




public class MediaControlExtension {
    private static final String TAG_CONTROLLER = "controller";

    private static final String ATTR_CLASS = "class";

    private static MediaControlExtension[] cachedExtensions;

    public static MediaControlExtension[] getExtensions() {
        if (cachedExtensions != null)
            return cachedExtensions;

        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(NavigatorPlugin.ID, "MediaControl")
                .getExtensions();
        List<MediaControlExtension> l = new ArrayList<MediaControlExtension>();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                MediaControlExtension ex = parseExtension(configElements[j], l.size());
                if (ex != null)
                    l.add(ex);
            }
        }
        cachedExtensions = l.toArray(new MediaControlExtension[l.size()]);
        return cachedExtensions;
    }

    private static MediaControlExtension parseExtension(IConfigurationElement configElement, int idx) {
        if (!configElement.getName().equals(TAG_CONTROLLER))
            return null;
        try {
            return new MediaControlExtension(configElement);
        } catch (Exception e) {
        }
        return null;
    }

    public static void disposeExtensions() {
        if (cachedExtensions == null)
            return;
        for (int i = 0; i < cachedExtensions.length; i++) {
            cachedExtensions[i].dispose();
        }
        cachedExtensions = null;
    }

    public static void start(IMediaControl.IHandle handle) {
        MediaControlExtension[] exs = getExtensions();
        if (exs == null)
            return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getMediaControl().start(handle);
        }
    }

    public static void doDispose(IMediaControl.IHandle handle) {
        MediaControlExtension[] exs = getExtensions();
        if (exs == null)
            return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getMediaControl().dispose(handle);
        }
    }

    private final IConfigurationElement configElement;

    private IMediaControl controller;

    private MediaControlExtension(IConfigurationElement configElement) {
        this.configElement = configElement;
    }

    private IMediaControl getMediaControl() {
        if (controller != null)
            return controller;
        try {
            controller = (IMediaControl) configElement.createExecutableExtension(ATTR_CLASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return controller;
    }

    private void dispose() {
        if (controller == null)
            return;
        controller = null;
    }

    public static int toggleEnable() {
        MediaControlExtension[] exs = getExtensions();
        int result = 0;
        if (exs == null)
            return IMediaControl.TOGGLE_FAIL;
        for (int i = 0; i < exs.length; i++) {
            result |= exs[i].getMediaControl().toggleEnabled();
        }
        return result;
    }
    
    public static boolean isAvailable(){
        MediaControlExtension[] exs = getExtensions();
        if (exs == null)
            return false;
        
        boolean ret = false;
        for (int i = 0; i < exs.length; i++) {
            ret |= exs[i].getMediaControl().isAvailable();
        }
        return ret;
    }

    public static boolean toggleViewShowing() {
        MediaControlExtension[] exs = getExtensions();
        boolean result = true;
        for (int i = 0; i < exs.length; i++) {
            result = exs[i].getMediaControl().toggleViewShowing() && result;
        }
        return result;
    }

    public static boolean isEnabled() {
        MediaControlExtension[] exs = getExtensions();
        if (exs == null)
            return false;
        
        boolean ret = false;
        for (int i = 0; i < exs.length; i++) {
            ret |= exs[i].getMediaControl().isEnabled();
        }
        return ret;
    }

    public static void speakInfo(boolean flush) {
        MediaControlExtension[] exs = getExtensions();
        if (exs == null)
            return;

        for (int i = 0; i < exs.length; i++) {
            exs[i].getMediaControl().speakInfo(flush);
        }
    }

}
