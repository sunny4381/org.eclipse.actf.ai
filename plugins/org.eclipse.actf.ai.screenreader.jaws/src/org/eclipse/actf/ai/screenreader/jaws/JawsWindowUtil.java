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

public class JawsWindowUtil {
	
	private static JawsAPI jaws = JawsAPI.getInstance();

	private static IVoiceEventListener listener;
	
	private static JawsWindowUtil instance;

	public static JawsWindowUtil getInstance() {
		if (instance == null) {
            int handle = JawsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().handle;
            if (!_initializeWindow(handle)) return null;
            instance = new JawsWindowUtil();
		}
		return instance;
	}
	
	private JawsWindowUtil() {
		
	}

	public void setEventListener(IVoiceEventListener eventListener) {
	    listener = eventListener;
	}

	public boolean JawsShowTextToWindow(String stringToSpeak, boolean bInterrupt, int index) {
	    // if (bInterrupt) JawsStopSpeech();
	    boolean ret = _setJawsWindowText(stringToSpeak);
	    if (listener != null) {
	        listener.indexReceived(index);
	    }
	    return ret;
	}

	public boolean TakeBackControl(IWebBrowserACTF browser){
	    //return _TakeBackControl(browser.getIWebBrowser2());
	    
	    int handle = JawsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().handle;
	    return _TakeBackControl(handle);
	}

	public static boolean callBack(int param) {
	    System.err.println("Callbacked!!!" + param);
	    if (listener == null) return true;
	    if (param == 0) {
	        listener.indexReceived(-1);
	    } else if (param == 1) {
	        jaws.JawsStopSpeech();
	        resetJawsWindowText();
            jaws.JawsRunScript(Jaws.SAYALLOFF);
	        listener.indexReceived(-2);
	    }
	    return true;
	}

	public static void resetJawsWindowText() {
        _resetJawsWindowText();
	}
	

	private static native boolean _TakeBackControl(long browser);
	private static native boolean _setJawsWindowText(String text);
	private static native boolean _resetJawsWindowText();
	private static native boolean _initializeWindow(int handle);
}
