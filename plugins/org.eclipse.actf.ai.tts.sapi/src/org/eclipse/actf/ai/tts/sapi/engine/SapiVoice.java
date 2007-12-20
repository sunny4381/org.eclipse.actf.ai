/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.sapi.engine;

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.util.win32.COMUtil;
import org.eclipse.actf.util.win32.MemoryUtil;
import org.eclipse.actf.util.win32.NativeIntAccess;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;


public class SapiVoice implements ITTSEngine, IPropertyChangeListener {

    public static final String ID = "org.eclipse.actf.ai.tts.sapi.engine.SapiVoice";   //$NON-NLS-1$
    public static final String AUDIO_OUTPUT = "org.eclipse.actf.ai.tts.SapiVoice.audioOutput";   //$NON-NLS-1$
	
	public static final int 	SVSFDefault = 0,
							    SVSFlagsAsync = 1,
							    SVSFPurgeBeforeSpeak = 2,
							    SVSFIsFilename = 4,
							    SVSFIsXML = 8,
							    SVSFIsNotXML = 16,
							    SVSFPersistXML = 32;

    private ISpVoice dispSpVoice;
	private Variant varSapiVoice;
	private OleAutomation automation;
	private int idGetVoices;
    private int idGetAudioOutputs;
	private ISpNotifySource spNotifySource = null;
	private static IPreferenceStore preferenceStore = SAPIPlugin.getDefault().getPreferenceStore();
	
	public SapiVoice() {
        int pv = COMUtil.createDispatch(ISpVoice.IID);
		dispSpVoice = new ISpVoice(pv);
		varSapiVoice = new Variant(dispSpVoice);
		automation = varSapiVoice.getAutomation();
		spNotifySource = ISpNotifySource.getNotifySource(dispSpVoice);
		SAPIPlugin.getDefault().addPropertyChangeListener(this);
		
        idGetVoices = getIDsOfNames("GetVoices"); //$NON-NLS-1$
        idGetAudioOutputs = getIDsOfNames("GetAudioOutputs"); //$NON-NLS-1$
		
		setVoiceName();
        setAudioOutputName();
	}

	public void propertyChange(PropertyChangeEvent event) {
		if( ID.equals(event.getProperty()) ) {
			stop();
			setVoiceName();
		}
        else if( AUDIO_OUTPUT.equals(event.getProperty()) ) {
            stop();
            setAudioOutputName();
        } 
	}
	
	public void setEventListener(IVoiceEventListener eventListener) {
		spNotifySource.setEventListener(eventListener);
	}
	
	public void speak(String text, int flags, int index) {
		int firstFlag = SVSFlagsAsync;
		if( 0 != (TTSFLAG_FLUSH & flags) ) {
			firstFlag |= SVSFPurgeBeforeSpeak;
		}
		if (index >= 0) {
            speak("<BOOKMARK mark=\"" + index + "\"/>",firstFlag | SVSFPersistXML); //$NON-NLS-1$ //$NON-NLS-2$
            speak(text,SVSFlagsAsync);
            speak("<BOOKMARK mark=\"-1\"/>",SVSFlagsAsync | SVSFPersistXML); //$NON-NLS-1$
        } else {
            speak(text,firstFlag);
        }
	}
    
    private void speak(String text, int sapiFlags) {
        char[] data = (text + "\0").toCharArray(); //$NON-NLS-1$
        int bstrText = MemoryUtil.SysAllocString(data);
        try {
            dispSpVoice.Speak(bstrText,sapiFlags);
        }
        finally {
            MemoryUtil.SysFreeString(bstrText);
        }
    }
	
	public void stop() {
		speak("",TTSFLAG_FLUSH,-1); //$NON-NLS-1$
	}
	
	public boolean setRate(int rate) {
        return OLE.S_OK == dispSpVoice.put_Rate(rate);
	}
	
	public int getRate() {
        NativeIntAccess nia = new NativeIntAccess();
        try {
            if( OLE.S_OK == dispSpVoice.get_Rate(nia.getAddress()) ) {
                return nia.getInt();
            }
        }
        finally {
            nia.dispose();
        }
        return -1;
	}
	
	public boolean setVoice(Variant varVoice) {
        return OLE.S_OK == dispSpVoice.put_Voice(varVoice.getDispatch().getAddress());
	}
    
    public boolean setAudioOutput(Variant varAudioOutput) {
        return OLE.S_OK == dispSpVoice.put_AudioOutput(null!=varAudioOutput ? varAudioOutput.getDispatch().getAddress() : 0);
    }
	
	public void setVoiceName() {
		String voiceName = preferenceStore.getString(ID);
		if( voiceName.length()>0 ) {
			setVoiceName("name="+voiceName); //$NON-NLS-1$
		}
	}

	public boolean setVoiceName(String voiceName) {
		boolean success = false;
		Variant varVoices = getVoices(voiceName,null);
		if( null != varVoices ) {
			SpeechObjectTokens tokens = SpeechObjectTokens.getTokens(varVoices);
			if( null != tokens && 0<tokens.getCount() ) {
				Variant varVoice = tokens.getItem(0);
				if( null != varVoice ) {
					success = setVoice(varVoice);
				}
			}
			varVoices.dispose();
		}
		return success;
	}
	
    public void setAudioOutputName() {
        String audioOutput = preferenceStore.getString(AUDIO_OUTPUT);
        if( audioOutput.length()>0 ) {
            setAudioOutputName(audioOutput); //$NON-NLS-1$
        }
        else {
            setAudioOutput(null);
        }
    }

    public boolean setAudioOutputName(String audioOutput) {
        boolean success = false;
        Variant varAudioOutputs = getAudioOutputs(null,null);
        if( null != varAudioOutputs ) {
            SpeechObjectTokens tokens = SpeechObjectTokens.getTokens(varAudioOutputs);
            if (null != tokens) {
                for (int i = 0; i < tokens.getCount(); i++) {
                    Variant varAudioOutput = tokens.getItem(i);
                    if (null != varAudioOutput) {
                        SpObjectToken token = SpObjectToken.getToken(varAudioOutput);
                        if (null != token && audioOutput.equals(token.getDescription(0)) ) {
                            success = setAudioOutput(varAudioOutput);
                            break;
                        }
                    }
                }
            }
            varAudioOutputs.dispose();
        }
        return success;
    }
	
    public Variant getVoices(String requiredAttributes, String optionalAttributes) {
        return getTokens(idGetVoices, requiredAttributes, optionalAttributes);
    }
    
    public Variant getAudioOutputs(String requiredAttributes, String optionalAttributes) {
        return getTokens(idGetAudioOutputs, requiredAttributes, optionalAttributes);
    }
    
    private Variant getTokens(int id, String requiredAttributes, String optionalAttributes) {
        if( null == requiredAttributes ) {
            return automation.invoke(id);
        }
        else if( null == optionalAttributes ) {
            return automation.invoke(id,new Variant[]{new Variant(requiredAttributes)}); 
        }
        return automation.invoke(id,new Variant[]{new Variant(requiredAttributes),new Variant(optionalAttributes)}); 
    }
	
	private int getIDsOfNames(String name) {
		int dispid[] = automation.getIDsOfNames(new String[]{name});
		if( null != dispid ) {
			return dispid[0];
		}
		return 0;
	}
	
	public void dispose() {
		varSapiVoice.dispose();
	}
	
	public int getSpeed() {
		int rate = getRate();	// -10 <= rate <= 10
		return (rate+10)*5;		// 0 <= speed <= 100
	}

	public void setSpeed(int speed) {
		int rate = speed/5 -10;
		setRate(rate);
	}
	
	public void setLanguage(String language) {
		String token;
		if( LANG_JAPANESE.equals(language) ) {
			token = "language=411"; //$NON-NLS-1$
		}
		else if( LANG_ENGLISH.equals(language) ) {
			token = "language=409;9"; //$NON-NLS-1$
		}
		else {
			return;
		}
		setVoiceName(token);
	}

	public void setGender(String gender) {
	}

    public boolean isAvailable() {
        return automation != null;
    }
}
