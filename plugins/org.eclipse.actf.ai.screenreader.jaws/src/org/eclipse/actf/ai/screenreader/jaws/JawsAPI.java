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
    private static final String SAYALLOFF = "AiBrowserSayAllOff";
    
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
            int handle = JawsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().handle;
            if (!_initialize(handle)) return null;
            instance = new JawsAPI();
        }
        return instance;
    }

    public boolean isAvailable() {
        return _isAvailable();
    }

    public boolean JawsSayString(String stringToSpeak, boolean bInterrupt) {
        if (bInterrupt) _JawsStopSpeech();
        return _JawsSayString(stringToSpeak, bInterrupt);
    }

    public boolean JawsShowTextToWindow(String stringToSpeak, boolean bInterrupt, int index) {
        // if (bInterrupt) JawsStopSpeech();
        boolean ret = _setJawsWindowText(stringToSpeak);
        if (listener != null) {
            listener.indexReceived(index);
        }
        return ret;
    }

    public boolean JawsStopSpeech() {
        _resetJawsWindowText();
        JawsRunScript(SAYALLOFF);
        return _JawsStopSpeech();
    }

    public boolean JawsRunScript(String scriptName) {
        return _JawsRunScript(scriptName);
    }
    
    public boolean TakeBackControl(IWebBrowserACTF browser){
        //return _TakeBackControl(browser.getIWebBrowser2());
        
        int handle = JawsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().handle;
        return _TakeBackControl(handle);
    }

    private static IVoiceEventListener listener;

    public void setEventListener(IVoiceEventListener eventListener) {
        listener = eventListener;
    }

    public static boolean callBack(int param) {
        System.err.println("Callbacked!!!" + param);
        if (listener == null) return true;
        if (param == 0) {
            listener.indexReceived(-1);
        } else if (param == 1) {
            getInstance().JawsStopSpeech();
            listener.indexReceived(-2);
        }
        return true;
    }

    private static native boolean _initialize(int handle);
    private static native boolean _isAvailable();
    private static native boolean _JawsSayString(String stringToSpeak, boolean bInterrupt);
    private static native boolean _JawsStopSpeech();
    private static native boolean _JawsRunScript(String scriptName);
    private static native boolean _TakeBackControl(long browser);
    private static native boolean _setJawsWindowText(String text);
    private static native boolean _resetJawsWindowText();
}
