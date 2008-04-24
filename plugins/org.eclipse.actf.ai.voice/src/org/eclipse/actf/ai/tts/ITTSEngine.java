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
package org.eclipse.actf.ai.tts;

import org.eclipse.actf.ai.voice.IVoice;
import org.eclipse.actf.ai.voice.IVoiceEventListener;

/**
 * ITTSEngine interface defines low level text synthesis interface to be
 * implemented by text-to-speech engine
 * 
 */
public interface ITTSEngine {

	/* speak flag constants */
	/**
	 * Append speak request at end of speak requests
	 */
	public static final int TTSFLAG_DEFAULT = 0;

	/**
	 * Flash all pending speak request prior to request speak
	 */
	public static final int TTSFLAG_FLUSH = 1;

	/* language constants */
	public static final String LANG_ENGLISH = "en"; //$NON-NLS-1$

	public static final String LANG_JAPANESE = "ja"; //$NON-NLS-1$

	/* Gender constants */
	public static final String GENDER_MALE = "male"; //$NON-NLS-1$

	public static final String GENDER_FEMALE = "female"; //$NON-NLS-1$

	/**
	 * Speak the contents of a text string
	 * 
	 * @param text
	 *            text string to be spoken
	 * @param flags
	 *            text speak flags. see TTSFLAG_*
	 * @param index
	 *            positive int value to be reported via IVoiceEventListener when
	 *            start speaking text. TTS engine should also report -1 when
	 *            completed. index event should not reported when negative index
	 *            is specified
	 */
	public void speak(String text, int flags, int index);

	/**
	 * Flash all pending speak request
	 */
	public void stop();

	/**
	 * Set event listener in order to receive index event
	 * 
	 * @param eventListener
	 */
	public void setEventListener(IVoiceEventListener eventListener);

	/**
	 * Dispose TTS engine
	 */
	public void dispose();

	/**
	 * Get current speaking speed
	 * 
	 * @see IVoice#getSpeed()
	 */
	public int getSpeed();

	/**
	 * Set speaking speed
	 * 
	 * @see IVoice#setSpeed(int)
	 */
	public void setSpeed(int speed);

	/**
	 * Set voice language
	 * 
	 * @param language
	 */
	public void setLanguage(String language);

	/**
	 * Set voice gender
	 * 
	 * @param gender
	 */
	public void setGender(String gender);

	/**
	 * Returns current availability of TTS engine
	 * 
	 * @return true when TTS engine is available false when TTS engine is not
	 *         available
	 */
	public boolean isAvailable();
}
