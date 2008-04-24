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

import java.net.URL;

import javax.sound.sampled.Mixer;

import org.eclipse.actf.ai.audio.io.impl.AudioFileReader;
import org.eclipse.actf.ai.audio.io.impl.AudioPipe;
import org.eclipse.actf.ai.audio.io.impl.LoopedAudioPipe;
import org.eclipse.actf.ai.audio.io.impl.PlaybackDevice;
import org.eclipse.actf.ai.audio.io.impl.RecordDevice;

/**
 * This audio IO utilities enable to play audio stream in form of pipe-like
 * interface. The following is a sample code to play audio file for a second.
 * Firstly, a reader and a writer are created then these are combined with a
 * pipe. Secondly, the pipe is initialized to play then the pipe runs for a
 * second.
 * 
 * <pre>
 * IAudioReader reader = AudioFactory.createAudioReader(&quot;*****.wav&quot;);
 * IAudioWriter writer = AudioFactory.createDefaultWriter();
 * IAudioPipe pipe = AudioFactory.createLoopedAudioPipe(reader, writer);
 * // buffer size for reading (in milli seconds)
 * pipe.setBufferSize(100);
 * 
 * // interval time to try writing (in milli seconds)
 * pipe.setInterval(1);
 * 
 * // Create new thread and start playing. 
 * // The pipe automatically opens reader and writer.
 * pipe.prepare();
 * 
 * pipe.start();
 * 
 * try {
 * 	Thread.sleep(1000);
 * } catch (InterruptedException e) {
 * 	e.printStackTrace();
 * }
 * 
 * pipe.stop();
 * </pre>
 */
public class AudioFactory {
	/**
	 * This method creates an IAudioReader which provides an audio stream from
	 * the file. In the default Java environment this can read files in format
	 * of WAVE and AIFF. You can extends the supported formats by Java sound
	 * SPI.
	 * 
	 * @param file
	 *            the audio file to be read.
	 * @return new instance of IAudioReaer.
	 */
	public static IAudioReader createAudioReader(String file) {
		return new AudioFileReader(file);
	}

	/**
	 * This method creates an IAudioReader which provides an audio stream from
	 * the URL.
	 * 
	 * @param url
	 *            the audio file to be read.
	 * @return new instance of IAudioReader.
	 * @see #createAudioReader(String)
	 */
	public static IAudioReader createAudioReader(URL url) {
		return new AudioFileReader(url);
	}

	/**
	 * This method creates an IAudioWriter which manages the default system play
	 * back device.
	 * 
	 * @return new instance of IAudioWriter
	 */
	public static IAudioWriter createDefaultWriter() {
		return new PlaybackDevice();
	}

	/**
	 * This methods creates an IAudioPipe which manages the audio stream between
	 * <i>reader</i> and <i>writer</i>.
	 * 
	 * @param reader
	 *            the IAudioReader provides an audio stream.
	 * @param writer
	 *            the IAudioWriter process the audio stream.
	 * @return new instance of IAudioPipe.
	 */
	public static IAudioPipe createAudioPipe(IAudioReader reader,
			IAudioWriter writer) {
		return new AudioPipe(reader, writer);
	}

	/**
	 * This method creates an IAudioPipe which manages the audio stream between
	 * <i>reader</i> and <i>writer</i>. The pipe provides a repeating audio
	 * stream, so it has no ending.
	 * 
	 * @param reader
	 *            the IAudioReader provides an audio stream.
	 * @param writer
	 *            the IAudioWriter process the audio stream.
	 * @return new instance of IAudioPipe.
	 */
	public static IAudioPipe createLoopedAudioPipe(IAudioReader reader,
			IAudioWriter writer) {
		return new LoopedAudioPipe(reader, writer);
	}

	/**
	 * This method creates an IAudioWriter which manages the play back device
	 * specified by the <i>mixer</i>.
	 * 
	 * @param mixer
	 *            the Mixer to be used for audio play back.
	 * @return new instance of IAudioWriter
	 * @see Mixer
	 */
	public static IAudioWriter createAudioWriter(Mixer mixer) {
		return new PlaybackDevice(mixer);
	}

	/**
	 * This method creates an IAudioReader which provides an audio stream from
	 * the specified recording device.
	 * 
	 * @param mixer
	 *            the Mixer to be used for audio recording.
	 * @param recordingSmapleRate
	 *            the recording sampling rate. It might be 8000, 16000, or so
	 *            on.
	 * @param recordingChannels
	 *            the number of recording channel. It might be 1 or 2.
	 * @return new instance of IAudioReader.
	 */
	public static IAudioReader createAudioReader(Mixer mixer,
			int recordingSmapleRate, int recordingChannels) {
		return new RecordDevice(mixer, recordingSmapleRate, recordingChannels);
	}
}
