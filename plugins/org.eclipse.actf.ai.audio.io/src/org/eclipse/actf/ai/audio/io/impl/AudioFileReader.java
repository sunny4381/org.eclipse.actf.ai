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

package org.eclipse.actf.ai.audio.io.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.actf.ai.audio.io.AudioIOException;
import org.eclipse.actf.ai.audio.io.IAudioReader;

public class AudioFileReader implements IAudioReader {

	private File file;

	private URL url;

	private AudioFormat format;

	private AudioInputStream stream;

	private boolean opened = false;

	private boolean closed = false;

	public AudioFileReader(String fileName) {
		setFile(fileName);
	}

	public AudioFileReader(URL url) {
		setURL(url);
	}

	public String getName() {
		if (file != null)
			return "File \"" + file.getName() + "\"";
		if (url != null)
			return "URL \"" + url.getPath() + "\"";
		return "";
	}

	private void setFile(String fileName) {
		this.file = new File(fileName);
	}

	private void setURL(URL url) {
		this.url = url;
	}

	public boolean canRead() {
		try {
			if (file != null)
				AudioSystem.getAudioInputStream(file);
			if (url != null)
				AudioSystem.getAudioFileFormat(url);
			return true;
		} catch (UnsupportedAudioFileException e) {
		} catch (IOException e) {
		}
		return false;
	}

	synchronized public void open() throws AudioIOException {
		try {
			if (file != null)
				stream = AudioSystem.getAudioInputStream(file);
			if (url != null)
				stream = AudioSystem.getAudioInputStream(url);
			
			// AudioFormat baseFormat = stream.getFormat();
			// 
			// AudioFormat decodedFormat = new AudioFormat(baseFormat
			// .getEncoding(), baseFormat.getSampleRate(), 16, baseFormat
			// .getChannels(), baseFormat.getChannels() * 2, baseFormat
			// .getSampleRate(), false);
			//
			// stream = AudioSystem.getAudioInputStream(decodedFormat, stream);

			format = stream.getFormat();
			opened = true;
			closed = false;
		} catch (UnsupportedAudioFileException e) {
			throw new AudioIOException("Unsupported audio file type.", e);
		} catch (IOException e) {
			throw new AudioIOException(e.toString(), e);
		}
	}

	public AudioFormat getAudioFormat() {
		return format;
	}

	synchronized public int read(byte[] data, int offset, int length)
			throws AudioIOException {
		if (!opened) {
			throw new AudioIOException("This is not opened.", null);
		}
		try {
			int nBytesRead = stream.read(data, offset, length);
			if (nBytesRead == -1) {
				stream.close();
				opened = false;
				closed = true;
				nBytesRead = 0;
			}
			return nBytesRead;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	synchronized public void close() {
		try {
			if (stream != null)
				stream.close();
		} catch (IOException e) {
		}
		opened = false;
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}
}
