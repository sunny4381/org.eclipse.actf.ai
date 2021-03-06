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

package org.eclipse.actf.ai.audio.io;

/**
 * The listener interface for receiving pipe events.
 */
public interface AudioPipeListener {
	/**
	 * Invoke when the audio stream is finished.
	 * 
	 * @param pipe
	 *            the IAudioPipe instance which raises this event.
	 */
	void finished(IAudioPipe pipe);

	/**
	 * Invoke when the play back is stopped.
	 * 
	 * @param pipe
	 *            the IAudioPipe instance which raises this event.
	 */
	void stopped(IAudioPipe pipe);
}
