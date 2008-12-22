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

import org.eclipse.actf.ai.internal.navigator.Messages;


/**
 * IManipulator interface defines the methods to be implemented by the "manipulator"
 * of the application. For example keyboard manipulator, joy stick manipulator, and so on.
 * IManipulator can obtain instances of IBrowserControl and INavigatorUI.
 */
public interface IManipulator {
    /**
     * This Mode class represents modes of the application.
     */
    public static class Mode {
        public final String name;
        public final int code;
        private Mode(String name, int code) {
            this.name = name;
            this.code = code;
        }
    }
    
    /**
     * The code of the tree navigation mode.
     */
    int TREE_NAVIGATION_MODE_CODE = 0;
    
    /**
     * The instance of the Mode of the tree navigation mode.
     * This is the default mode of the application.
     */
    Mode TREE_NAVIGATION_MODE = new Mode(Messages.IManipulator_TreeNavigaion, TREE_NAVIGATION_MODE_CODE); 
    
    /**
     * The code of the form input mode.
     */
    int FORM_INPUT_MODE_CODE = 1;
    
    /**
     * The instance of the Mode of the form input mode.
     * This mode is used during the users input information into HTML forms.
     */
    Mode FORM_INPUT_MODE = new Mode(Messages.IManipulator_FormInput, FORM_INPUT_MODE_CODE); 
    
    /**
     * The code of the key hook disabled mode.
     */
    int KEYHOOK_DISABLED_MODE_CODE = 2;
    
    /**
     * The instance of the Mode of the key hook disabled mode.
     * This mode is used during a dialog is shown.
     */
    Mode KEYHOOK_DISABLED_MODE = new Mode(Messages.IManipulator_Input, KEYHOOK_DISABLED_MODE_CODE); 

    /**
     * @param browserControl The instance of the IBrowserControl to be controlled by the manipulator.
     */
    void setBrowserControl(IBrowserControl browserControl);
    /**
     * @param navigatorUI The instance of the INavigatorUI to be controlled by the manipulator.
     */
    void setNavigator(INavigatorUI navigatorUI);
    
    /**
     * @param mode The mode to be set
     */
    void setMode(Mode mode);
    
    /**
     *  It will be called when the manipulator is disposed.
     */
    void dispose();
}
