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

import javax.sound.sampled.AudioFormat;

import org.eclipse.actf.ai.audio.io.AudioIOException;

/**
 * This interface provides for reading audio data stream. It will read the audio
 * data from file, URL, audio device, and so on.
 */
public interface IAudioReader {

	/**
	 * @return the name of the reader.
	 */
	String getName();

	/**
	 * @return the audio format of the audio data.
	 */
	AudioFormat getAudioFormat();

	/**
	 * It returns true when the reader is opened, but an error occurs then
	 * it returns false.
	 * 
	 * @return Whether the audio reader can read the data or not.
	 */
	boolean canRead();

	/**
	 * Open the audio stream.
	 * 
	 * @throws AudioIOException
	 */
	void open() throws AudioIOException;

	/**
	 * Close the audio stream.
	 */
	void close();

	/**
	 * This copies the audio stream from the source to the buffer. The reading
	 * cursor will be increased. When the cursor is reached to the end, the
	 * reader will be closed.
	 * 
	 * @param data
	 *            the buffer to copy the audio data.
	 * @param offset
	 *            the offset of the buffer to copy.
	 * @param length
	 *            the length of the audio data to copy.
	 * @return the number of the actual read.
	 * @throws AudioIOException
	 * @see #isClosed()
	 */
	int read(byte[] data, int offset, int length) throws AudioIOException;

	/**
	 * @return whether the reader is closed or not.
	 */
	boolean isClosed();
}
