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
import org.eclipse.actf.ai.navigator.IBrowserControl;
import org.eclipse.actf.ai.navigator.IManipulator;
import org.eclipse.actf.ai.navigator.INavigatorUI;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;




public class ManipulatorExtension {
    private static final String TAG_MANIPULATOR = "manipulator";
    private static final String ATTR_CLASS = "class";

    private static ManipulatorExtension[] cachedExtensions;

    public static ManipulatorExtension[] getExtensions() {
        if (cachedExtensions != null) return cachedExtensions;

        IExtension[] extensions = Platform.getExtensionRegistry()
            .getExtensionPoint(NavigatorPlugin.ID, "Navigation")
            .getExtensions();
        List<ManipulatorExtension> l = new ArrayList<ManipulatorExtension>();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] configElements =
                extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                ManipulatorExtension ex = parseExtension(configElements[j], l.size());
                if (ex != null) l.add(ex);
            }
        }
        cachedExtensions = l.toArray(new ManipulatorExtension[l.size()]);
        return cachedExtensions;
    }

    private static ManipulatorExtension parseExtension(IConfigurationElement configElement, int idx) {
        if (!configElement.getName().equals(TAG_MANIPULATOR))
            return null;
        try {
            return new ManipulatorExtension(configElement);
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

    public static void setBrowserControl(IBrowserControl browserControl) {
        ManipulatorExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getManipulator().setBrowserControl(browserControl);
        }
    }

    public static void setNavigator(INavigatorUI navigatorUI) {
        ManipulatorExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getManipulator().setNavigator(navigatorUI);
        }
    }

    public static void setMode(IManipulator.Mode mode) {
        ManipulatorExtension[] exs = getExtensions();
        if (exs == null) return;
        for (int i = 0; i < exs.length; i++) {
            exs[i].getManipulator().setMode(mode);
        }
    }

    private final IConfigurationElement configElement;
    private IManipulator manipulator;

    private ManipulatorExtension(IConfigurationElement configElement) {
        this.configElement = configElement;
    }

    private IManipulator getManipulator() {
        if (manipulator != null) return manipulator;
        try {
            manipulator = (IManipulator) configElement.createExecutableExtension(ATTR_CLASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return manipulator;
    }

    private void dispose() {
        if (manipulator == null) return;
        manipulator.dispose();
        manipulator = null;
    }
}
