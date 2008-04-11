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
package org.eclipse.actf.ai.screenreader.jaws;

import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.model.IWebBrowserACTF;

public class JawsAPI {
    private static JawsAPI instance;
    static{
        try{
            System.loadLibrary("jawsapi-bridge");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
	
    private JawsAPI() {
    }

    public static JawsAPI getInstance(){
        if (instance == null) {
        	if (!_initialize()) return null;
            instance = new JawsAPI();
        }
        return instance;
    }
    
    public boolean JawsRunFunction(String funcName) {
    	return _JawsRunFunction(funcName);
    }

    public boolean JawsSayString(String stringToSpeak, boolean bInterrupt) {
        if (bInterrupt) _JawsStopSpeech();
        return _JawsSayString(stringToSpeak, bInterrupt);
    }

    public boolean JawsStopSpeech() {
    	// JawsStopSpeech does not work well, so null string will be spoken.
        // return _JawsStopSpeech();
    	
    	return _JawsSayString("", true);
    }

    public boolean JawsRunScript(String scriptName) {
        return _JawsRunScript(scriptName);
    }
    
	public boolean isAvailable() {
	    return _isAvailable();
	}

	private static native boolean _initialize();
	private static native boolean _isAvailable();
	private static native boolean _JawsRunFunction(String funcName);
    private static native boolean _JawsSayString(String stringToSpeak, boolean bInterrupt);
    private static native boolean _JawsStopSpeech();
    private static native boolean _JawsRunScript(String scriptName);
}
