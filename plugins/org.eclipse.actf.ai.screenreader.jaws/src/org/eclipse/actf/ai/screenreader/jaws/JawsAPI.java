/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *    Yevgen Borodin - [Bug 226468]
 *******************************************************************************/
package org.eclipse.actf.ai.screenreader.jaws;


/**
 * JawsAPI is the wrapper of "jawsapi-bridge.dll" library which is the wrapper of "jfwapi.dll".
 */
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

    /**
     * @return The singleton instance of JawsAPI.
     */
    public static JawsAPI getInstance(){
        if (instance == null) {
        	if (!_initialize()) return null;
            instance = new JawsAPI();
        }
        return instance;
    }
    
    /**
     * Execute the JAWS function named <i>funcName</i>.
     * @param funcName The function name to be executed.
     * @return If the invocation is succeeded then true is returned.
     */
    public boolean JawsRunFunction(String funcName) {
    	return _JawsRunFunction(funcName);
    }

    /**
     * Order to speak <i>stringToSpeak</i> to JAWS.
     * @param stringToSpeak The string to be spoken.
     * @param bInterrupt If this flag is true then JAWS is stopped speaking and speaks the string,
     * Otherwise the string is buffered at the end of the speech.
     * @return If the invocation is succeeded then true is returned.
     */
    public boolean JawsSayString(String stringToSpeak, boolean bInterrupt) {
        if (bInterrupt) _JawsStopSpeech();
        return _JawsSayString(stringToSpeak, bInterrupt);
    }

    /**
     * Order to stop speaking to JAWS.
     * @return If the invocation is succeeded then true is returned.
     */
    public boolean JawsStopSpeech() {
    	// JawsStopSpeech does not work well, so null string will be spoken with interrupt flag.
        // return _JawsStopSpeech();
    	return _JawsSayString("", true);
    }

    /**
     * Execute the JAWS script named <i>funcName</i>.
     * @param scriptName The script name to be executed.
     * @return If the invocation is succeeded then true is returned.
     */
    public boolean JawsRunScript(String scriptName) {
        return _JawsRunScript(scriptName);
    }
    
	/**
	 * @return If JAWS is running then it returns <i>true</i>.
	 */
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
