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

package org.eclipse.actf.ai.navigator;


public interface IManipulator {
    public static class Mode {
        public final String name;
        public final int code;
        private Mode(String name, int code) {
            this.name = name;
            this.code = code;
        }
    }
    int TREE_NAVIGATION_MODE_CODE = 0;
    Mode TREE_NAVIGATION_MODE = new Mode(Messages.getString("IManipulator.TreeNavigaion"), TREE_NAVIGATION_MODE_CODE); //$NON-NLS-1$
    int FORM_INPUT_MODE_CODE = 1;
    Mode FORM_INPUT_MODE = new Mode(Messages.getString("IManipulator.FormInput"), FORM_INPUT_MODE_CODE); //$NON-NLS-1$
    int KEYHOOK_DISABLED_MODE_CODE = 2;
    Mode KEYHOOK_DISABLED_MODE = new Mode(Messages.getString("IManipulator.Input"), KEYHOOK_DISABLED_MODE_CODE); //$NON-NLS-1$

    void setBrowserControl(IBrowserControl browserControl);
    void setNavigator(INavigatorUI navigatorUI);
    void setMode(Mode mode);
    void dispose();
}
