/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.tts.msp.engine;

import org.eclipse.actf.util.win32.COMUtil;
import org.eclipse.swt.internal.ole.win32.GUID;
import org.eclipse.swt.internal.ole.win32.IDispatch;

/**
 * COM wrapper of ISpVoice interface.
 * 
 * @see "Microsoft Speech API ISpVoice"
 */
public class ISpVoice extends IDispatch {

	public static final GUID IID = COMUtil
			.IIDFromString("{d941651c-44e6-4c17-badf-c36826fc3424}"); //$NON-NLS-1$

	private int address;

	public ISpVoice(int address) {
		super(address);
		this.address = address;
	}

	public int put_Voice(int pVoiceAddress) {
		return COMUtil.VtblCall(9, address, pVoiceAddress);
	}

	public int put_AudioOutput(int pAudioOutputAddress) {
		return COMUtil.VtblCall(11, address, pAudioOutputAddress);
	}

	public int get_Rate(int pRateAddress) {
		return COMUtil.VtblCall(14, address, pRateAddress);
	}

	public int put_Rate(int rate) {
		return COMUtil.VtblCall(15, address, rate);
	}

	public int Speak(int pTextAddress, int flags) {
		return COMUtil.VtblCall(28, address, pTextAddress, flags, 0);
	}

}
