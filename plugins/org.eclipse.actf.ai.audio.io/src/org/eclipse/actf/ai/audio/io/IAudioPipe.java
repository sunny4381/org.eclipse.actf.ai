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
 * This interface provides for processing audio stream between a IAudioReader
 * and a IAudioWriter. The simplest implementation provides the function to
 * transfer a audio stream from the IAudioReader to the IAudioWriter. You can
 * creates IAudioPipe that have function to repeat or convert the audio stream.
 * 
 * The pipe should create a thread to manager the time line of the audio stream.
 */
public interface IAudioPipe {
	/**
	 * @param listener
	 *            the listener to be added.
	 */
	void addAudioPipeListener(AudioPipeListener listener);

	/**
	 * The pipe thread will be processed every <i>interval</i>. The interval
	 * might be sufficiently-small than the buffer size of the pipe.
	 * 
	 * @param milliSeconds
	 *            the interval time in milliseconds.
	 * @see #setBufferSize(int)
	 */
	void setInterval(int milliSeconds);

	/**
	 * @param priority
	 *            the priority of the pipe thread.
	 * @see Thread#setPriority(int)
	 */
	void setPriority(int priority);

	/**
	 * The pipe will read buffer-size data from the audio stream of the
	 * IAudioReader at one interval. If you want to stop the audio play back
	 * immediately when you call {@link #stop()} then you have to set the buffer
	 * size as small as possible. But this will be coin side of generating noise
	 * because of the data missing for the play back device.
	 * 
	 * @param milliSeconds
	 *            the length of the buffer size in milliSeconds.
	 */
	void setBufferSize(int milliSeconds);

	/**
	 * @return whether the pipe is running or not.
	 */
	boolean isActive();

	/**
	 * The pipe opens the IAudioReader and IAudioWriter. Firstly, the pipe gets
	 * the format of the IAudioReader then the IAudioWriter is opened with the
	 * format or converted format.
	 */
	void prepare();

	/**
	 * @param delay
	 *            the delay time to start the pipe in millisecond.
	 * @see #start()
	 */
	void start(int delay);

	/**
	 * The pipe starts to process the audio stream. This creates a thread.
	 */
	void start();

	/**
	 * The pipe will be stopped. The play back device will be stopped within the
	 * buffer size. The thread will be terminated.
	 * 
	 * @see #setBufferSize(int)
	 */
	void stop();

	/**
	 * This function blocks until the pipe is finished. The pipe will be
	 * finished when the IAudioReader is finished or {@link #stop()} is called.
	 */
	void join();
}
