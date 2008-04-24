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


/**
 * IBrowserControl defines the methods to be implemented on the top of the browser.
 * All functions are not bound together with one tab.
 * Although the forward and the backward action is bound to one tab in general, 
 * the forward and the backward operates the action over the tabs.
 */
public interface IBrowserControl {
    /**
     * Forward to the next page.
     */
    void forward();
    
    /**
     * Backward to the previous page.
     */
    void backward();
    
    /**
     * Open the file dialog to import metadata files into the application storage.
     */
    void importMetadata();
    
    /**
     * Open the file dialog to export all metadata files in the application storage.
     */
    void exportAllMetadata();

    /**
     * Open a new tab in the application.
     */
    void openTab();
}
