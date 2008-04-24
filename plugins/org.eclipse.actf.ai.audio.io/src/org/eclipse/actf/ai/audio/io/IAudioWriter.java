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
 * This interface provides for writing audio data stream. It will write the
 * audio data to file, audio device, another pipe, and so on.
 */
public interface IAudioWriter {

	/**
	 * @return the name of the writer.
	 */
	String getName();

	/**
	 * This copies the audio stream from the buffer to the destination.
	 * 
	 * @param data
	 *            the buffer to copy the audio data.
	 * @param offset
	 *            the offset of the buffer to copy.
	 * @param length
	 *            the length of the audio data to copy.
	 * @return the number of the actual write.
	 * @throws AudioIOException
	 */
	int write(byte[] data, int offset, int length) throws AudioIOException;

	/**
	 * @return whether the writer can write to the destination or not.
	 */
	boolean canWrite();

	/**
	 * The writer open the destination in specified format.
	 * 
	 * @param format
	 *            the format to be opened.
	 * @throws AudioIOException
	 */
	void open(AudioFormat format) throws AudioIOException;

	/**
	 * The writer close the destination.
	 */
	void close();

	/**
	 * @return whether the destination is closed or not.
	 */
	boolean isClosed();
}
