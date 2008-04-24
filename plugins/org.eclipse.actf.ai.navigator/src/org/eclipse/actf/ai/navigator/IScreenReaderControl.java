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

package org.eclipse.actf.ai.navigator;

import org.eclipse.actf.model.IWebBrowserACTF;



/**
 * IScreenReaderControl interface defines methods to be implemented by the 
 * screen reader which conflicts with the application.
 * This interface provides functions to manage the screen reader behavior.
 */
public interface IScreenReaderControl {
    /**
     * @return Whether the screen reader is available or not.
     */
    boolean isAvailable();

    /**
     * The screen reader should be activated.
     */
    void screenReaderOn();
    
    /**
     * The screen reader should be deactivated, because the voice output from the application 
     * conflicts with the screen reader voice output.
     */
    void screenReaderOff();
    
    /**
     * This is a special method to get control of application from the screen reader.
     */
    void takeBackControl();
}
