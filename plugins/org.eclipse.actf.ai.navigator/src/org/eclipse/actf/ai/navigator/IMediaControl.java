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
import org.eclipse.actf.model.IWebBrowserACTF;




public interface IMediaControl {

    int TOGGLE_FAIL = 1;
    
    int STATUS_ON = 2;
    
    int STATUS_OFF = 4;
    
    int STATUS_NOT_AVAILABLE = 8;
    
    public interface IHandle {
        IVideoControl getVideoControl();
        ISoundControl getSoundControl();
        IWebBrowserACTF getWebBrowser();
        IVoice getVoice();
    }
    
    void start(IHandle handle);
    
    void dispose(IHandle handle);

    int toggleEnable();
    
    boolean isAvailable();
    
    boolean toggleViewShowing();

    boolean isEnabled();

    void speakInfo(boolean flush);
}
