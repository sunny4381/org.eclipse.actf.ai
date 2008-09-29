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

import org.eclipse.actf.ai.internal.navigator.NavigatorPlugin;
import org.eclipse.actf.ai.navigator.IScreenReaderControl;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;




public class ScreenReaderExtension {
    private static final String TAG_CONTROLLER = "controller";
    private static final String ATTR_CLASS = "class";

    private static ScreenReaderExtension[] cachedExtensions;

    public static ScreenReaderExtension[] getExtensions() {
        if (cachedExtensions != null) return cachedExtensions;

        IExtension[] extensions = Platform.getExtensionRegistry()
            .getExtensionPoint(NavigatorPlugin.ID, "ScreenReaderController")
            .getExtensions();
        List<ScreenReaderExtension> l = new ArrayList<ScreenReaderExtension>();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configElements =
                extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                ScreenReaderExtension ex = parseExtension(configElements[j], l.size());
                if (ex != null) l.add(ex);
            }
        }
        cachedExtensions = l.toArray(new ScreenReaderExtension[l.size()]);
        return cachedExtensions;
    }

    private static ScreenReaderExtension parseExtension(IConfigurationElement configElement, int idx) {
        if (!configElement.getName().equals(TAG_CONTROLLER))
            return null;
        try {
            return new ScreenReaderExtension(configElement);
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

    public static void screenReaderOn() {
        ScreenReaderExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getScreenReaderControl().screenReaderOn();
        }
    }

    public static void screenReaderOff() {
        ScreenReaderExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getScreenReaderControl().screenReaderOff();
        }
    }

    public static boolean isAvailable() {
        ScreenReaderExtension[] exs = getExtensions();
        if (exs == null) return false;
        boolean result = false;
        for (int i = 0; i < exs.length; i++) {
            result |= exs[i].getScreenReaderControl().isAvailable();
        }
        return result;
    }
    
    public static void takeBackControl(IWebBrowserACTF browser) {
        ScreenReaderExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getScreenReaderControl().takeBackControl();
        }
    }

    private final IConfigurationElement configElement;
    private IScreenReaderControl controller;

    private ScreenReaderExtension(IConfigurationElement configElement) {
        this.configElement = configElement;
    }

    private IScreenReaderControl getScreenReaderControl() {
        if (controller != null) return controller;
        try {
            controller = (IScreenReaderControl) configElement.createExecutableExtension(ATTR_CLASS);
        } catch (Exception e) {
        }
        return controller;
    }

    private void dispose() {
        if (controller == null) return;
        controller = null;
    }
}
