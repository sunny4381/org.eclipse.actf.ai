/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.fennec.treemanager;

/**
 * IVideoControl interface defines the method to be implemented by the
 * collection of video objects in a document. The implementation has a focused
 * video which is located after the current reading position.
 */
public interface IVideoControl {
	/**
	 * @param listener
	 *            the listener to be executed.
	 * @return true if the adding was succeeded.
	 * @see IMediaSyncEventListener
	 */
	boolean addEventListener(IMediaSyncEventListener listener);

	/**
	 * The states of videos.
	 */
	public enum VideoState {
		/**
		 * The state is not in the other state.
		 */
		STATE_OTHER,
		/**
		 * The video is playing.
		 */
		STATE_PLAY,
		/**
		 * The video is stopped.
		 */
		STATE_STOP,
		/**
		 * The video is paused.
		 */
		STATE_PAUSE,
		/**
		 * The video is in fast forwarding.
		 */
		STATE_FASTFORWARD,
		/**
		 * The video is in fast reversing.
		 */
		STATE_FASTREVERSE,
		/**
		 * The video is waiting to complete buffering.
		 */
		STATE_WAITING
	}

	/**
	 * The video state of the videos in the document. If at least one of video
	 * is playing then the state is STATE_PLAYING.
	 * 
	 * @return the video state.
	 */
	VideoState getVideoState();

	/**
	 * Change the track of the current video to the previous one.
	 * 
	 * @return whether the changing was succeeded or not.
	 */
	boolean previousTrack();

	/**
	 * Change the track of the current video to the next one.
	 * 
	 * @return whether the changing was succeeded or not.
	 */
	boolean nextTrack();

	/**
	 * Stop all videos in the document including the current video.
	 * 
	 * @return whether the stopping was succeeded or not.
	 */
	boolean stopMedia();

	/**
	 * Play the current video.
	 * 
	 * @return whether the playing was succeeded or not.
	 */
	boolean playMedia();

	/**
	 * Pause all videos in the document including the current video, or resume
	 * the current video.
	 * 
	 * @return whether the pausing was succeeded or not.
	 */
	boolean pauseMedia();

	/**
	 * Fast-reverse the current video.
	 * 
	 * @return whether the reversing was succeeded or not.
	 */
	boolean fastReverse();

	/**
	 * Fast-forward the current video.
	 * 
	 * @return whether the forwarding was succeeded or not.
	 */
	boolean fastForward();

	/**
	 * @return the current position of the current video in second.
	 */
	double getCurrentPosition();

	/**
	 * @return the total length of the current video in second.
	 */
	double getTotalLength();

	/**
	 * @return the number of the videos in the document.
	 */
	int getCount();

	/**
	 * @return the index of the current videos in the document. If there is no
	 *         video then it returns -1.
	 */
	int getIndex();
}
