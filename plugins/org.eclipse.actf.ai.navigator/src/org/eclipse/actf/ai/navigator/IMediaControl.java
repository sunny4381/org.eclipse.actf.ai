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

import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.ai.voice.IVoice;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;


/**
 * IMediaControl interface defines the methods to be implemented by the 
 * user interfaces which controls browser, video, audio, and voice output.
 * Implementations of this interface are managed by MediaControl extension.
 */
public interface IMediaControl {

    /**
     * The status code used by {@link #toggleEnabled()}
     * The user interface function isn't able to use. There is no extension.
     * This status code is only used in the extenstion manager.
     */
    int TOGGLE_FAIL = 1;
    
    /**
     * The status code used by {@link #toggleEnabled()}
     * The user interface function is enabled and the function is on.
     */
    int STATUS_ON = 2;
    
    /**
     * The status code used by {@link #toggleEnabled()}
     * The user interface function is enabled and the function is off.
     */
    int STATUS_OFF = 4;
    
    /**
	 * The status code used by {@link #toggleEnabled()}
	 * The user interface function is not enabled.
     */
    int STATUS_NOT_AVAILABLE = 8;
    
    /**
     * An instance of IHandle has video, sound, browser, and voice controller.
     */
    public interface IHandle {
        IVideoControl getVideoControl();
        ISoundControl getSoundControl();
        IWebBrowserACTF getWebBrowser();
        IVoice getVoice();
    }
    
    /**
     * The implementation of this extension is started by this methods with IHandle.
     * This method will be called when a focus of a browser is activated.
     * The <i>handle</i> contains the browser instance.
     * @param handle An instance of IHandle.
     */
    void start(IHandle handle);
    
    /**
     * This method will be called when a focus of a browser is deactivated.
     * @param handle An instance of IHandle.
     */
    void dispose(IHandle handle);

    /**
     * Toggle the enabled of the user interface.
     * If the user interface can be used then the status will be STATUS_ON or STATUS_OFF.
     * If the user interface can not be used then the status will be STATUS_NOT_AVAILABLE.
     * @return The status of after the toggle.
     */
    int toggleEnabled();
    
    /**
     * @return The availability of the user interface.
     */
    boolean isAvailable();
    
    /**
     * After calling method the view is shown then it returns true.
     * @return Whether the view is shown or not.
     */
    boolean toggleViewShowing();

    /**
     * @return Whether the user interface is enabled or not.
     */
    boolean isEnabled();

    /**
     * The user interface should speak the status information of itself using
     * the voice engine.
     * @see #IHandle.getVoice()
     * @param flush This is used for the argument of speak method of the voice engine.
     */
    void speakInfo(boolean flush);
}
