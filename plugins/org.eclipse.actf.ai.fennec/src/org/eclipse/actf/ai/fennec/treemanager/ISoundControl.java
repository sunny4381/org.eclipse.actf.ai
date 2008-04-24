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
 * ISoundControl interface defines the method to be implemented by the
 * collection of sound objects in a document. It can manage the volume of the
 * sound objects.
 */
public interface ISoundControl {
	/**
	 * The states of sound volume.
	 */
	public enum VolumeState {
		/**
		 * volume is minimum.
		 */
		MIN,
		/**
		 * volume is maximum.
		 */
		MAX,
		/**
		 * volume is not MIN, MAX, and MUTE.
		 */
		OTHER,
		/**
		 * sound is muted.
		 */
		MUTE
	}

	/**
	 * It mutes all sounds in the document.
	 * 
	 * @return whether the muting was succeeded or not.
	 */
	boolean muteMedia();

	/**
	 * All sound objects have a same state then the state will be returned,
	 * otherwise {@link VolumeState#OTHER} is returned.
	 * 
	 * @return the state of the volume.
	 * @see VolumeState
	 */
	VolumeState getVolumeState();

	/**
	 * Volume down all sound objects in default step width.
	 * @return whether the volume down was succeeded or not.
	 */
	boolean volumeDownMedia();

	/**
	 * Volume up all sound objects in default step width.
	 * @return whether the volume up was succeeded or not.
	 */
	boolean volumeUpMedia();

	/**
	 * Volume down all sound objects in minimal step width.
	 * @return whether the volume down was succeeded or not.
	 */
	boolean minimalVolumeDownMedia();

	/**
	 * Volume up all sound objects in minimal step width.
	 * @return whether the volume up was succeeded or not.
	 */
	boolean minimalVolumeUpMedia();

	/**
	 * @return the number of the sound objects.
	 */
	int getCount();

	/**
	 * @return the values of all sound volume.
	 */
	int[] getVolumes();

	/**
	 * @param volumes the values to be set.
	 * @return whether the setting was succeeded or not.
	 */
	boolean setVolumes(int[] volumes);
}
