/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    kentarou - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts;


public interface ISAPIEngine extends ITTSEngine {

	public static final int SVSFDefault = 0, SVSFlagsAsync = 1,
			SVSFPurgeBeforeSpeak = 2, SVSFIsFilename = 4, SVSFIsXML = 8,
			SVSFIsNotXML = 16, SVSFPersistXML = 32;

	/**
	 * @param rate
	 *            The rate property to be set.
	 * @return The invocation is succeeded then it returns true.
	 */
	public boolean setRate(int rate);

	/**
	 * @return The rate property of the voice engine.
	 */
	public int getRate();
	
	public void speak(String text, int sapiFlags);
	

}
