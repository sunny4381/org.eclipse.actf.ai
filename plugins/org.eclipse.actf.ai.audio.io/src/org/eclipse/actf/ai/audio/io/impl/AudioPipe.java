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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Date;

import javax.sound.sampled.AudioFormat;

import org.eclipse.actf.ai.audio.io.AudioIOException;
import org.eclipse.actf.ai.audio.io.AudioPipeListener;
import org.eclipse.actf.ai.audio.io.AudioUtil;
import org.eclipse.actf.ai.audio.io.IAudioPipe;
import org.eclipse.actf.ai.audio.io.IAudioReader;
import org.eclipse.actf.ai.audio.io.IAudioWriter;

public class AudioPipe implements IAudioPipe {

	private IAudioReader reader;

	private IAudioWriter writer;

	private AudioFormat format;

	private Thread thread;

	private Runnable runner;

	private byte[] buffer;

	private int bufferSize;

	private int interval = 100;

	private int priority = Thread.NORM_PRIORITY;

	private int bufferSizeInMs = 1000;

	private boolean active;

	private boolean waiting;

	private boolean stopFlag = false;

	private boolean stopFlag2 = false;

	private ArrayList<AudioPipeListener> listeners = new ArrayList<AudioPipeListener>();

	public AudioPipe(IAudioReader reader, IAudioWriter writer) {
		this.reader = reader;
		this.writer = writer;
		runner = new ReaderWriterRunner();
	}

	public void addAudioPipeListener(AudioPipeListener listener) {
		listeners.add(listener);
	}

	class ReaderWriterRunner implements Runnable {
		private void fadeout(byte[] buffer, int nBytesRead) {
			int bps = AudioUtil.getBytesPerSample(format);

			ByteBuffer bb = ByteBuffer.wrap(buffer);
			ShortBuffer sb = null;
			if (format.isBigEndian())
				bb.order(ByteOrder.BIG_ENDIAN);
			else
				bb.order(ByteOrder.LITTLE_ENDIAN);

			if (bps == 2)
				sb = bb.asShortBuffer();

			int len = nBytesRead / bps;

			for (int i = 0; i < len; i++) {
				double k = (double) (len - i) / (double) len;
				if (bps == 1)
					bb.put(i, (byte) (bb.get(i) * k));
				else
					sb.put(i, (short) (sb.get(i) * k));
			}
		}

		public void run() {
			try {
				while (true) {
					if (reader.isClosed())
						break;

					int nBytesRead = reader.read(buffer, 0, buffer.length);

					int nBytesWritten = 0;
					do {
						if (writer.isClosed()) {
							active = false;
							fireStopped();
							return;
						}
						if (stopFlag) {
							fadeout(buffer, nBytesRead);
							stopFlag = false;
							stopFlag2 = true;
						}
						int temp = writer.write(buffer, nBytesWritten,
								nBytesRead);
						nBytesWritten += temp;
						nBytesRead -= temp;
						sleep();
					} while (nBytesRead > 0);

					if (stopFlag2) {
						stopFlag2 = false;
						writer.close();
					}
				}
				writer.close();
			} catch (AudioIOException e) {
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
				writer.close();
			}
			active = false;
			fireFinished();
		}
	}

	private void fireFinished() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).finished(this);
		}
	}

	private void fireStopped() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).stopped(this);
		}
	}

	synchronized public void setInterval(int interval) {
		this.interval = interval;
	}

	synchronized public void setPriority(int priority) {
		this.priority = priority;
		if (thread != null)
			thread.setPriority(priority);
	}

	public void prepare() {
		try {
			reader.open();
			format = reader.getAudioFormat();
			writer.open(format);
		} catch (AudioIOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void start() {
		start(0);
	}

	public void start(final int delay) {
		if (active || waiting)
			return;
		waiting = true;
		thread = new Thread(new Runnable() {
			public void run() {
				long s = (new Date()).getTime();

				while (true) {
					long n = (new Date()).getTime();
					if (stopFlag) {
						waiting = false;
						stopFlag = false;
						active = false;
						fireStopped();
						return;
					}
					if (s + delay <= n) {
						break;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}

				while (active) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}

				prepare();

				int Bps = AudioUtil.getBytesPerSampleLR(format);
				bufferSize = AudioUtil.getBytesPerSecondLR(format)
						* bufferSizeInMs / 1000 / Bps * Bps;
				buffer = new byte[bufferSize];
				active = true;
				waiting = false;
				runner.run();
			}
		});
		thread.setPriority(priority);
		thread.setName(reader.getName() + "<->" + writer.getName());
		thread.start();
	}

	synchronized private void sleep() {
		try {
			Thread.sleep(interval);
			Thread.yield();
		} catch (InterruptedException e) {
		}
	}

	public boolean isActive() {
		return active;
	}

	public void stop() {
		if (active || waiting)
			stopFlag = true;
	}

	public void join() {
		if (thread == null)
			return;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setBufferSize(int miliSeconds) {
		bufferSizeInMs = miliSeconds;
	}

}
