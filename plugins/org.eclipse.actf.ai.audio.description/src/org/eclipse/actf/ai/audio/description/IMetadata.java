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

package org.eclipse.actf.ai.audio.description;

/**
 * This represents an item of audio description such as following declaration.
 * 
 * <pre>
 *   &lt;item importance=&quot;high&quot; type=&quot;pause-before|speak&quot;&gt;
 *     &lt;start type=&quot;relTime&quot;&gt;00:00:100&lt;/start&gt;
 *     &lt;duration&gt;00:04:500&lt;/duration&gt;
 *     &lt;description xml:lang=&quot;en&quot;&gt;The following preview has been approved for all audiences.&lt;/description&gt;
 *   &lt;/item&gt;
 * </pre>
 */
public interface IMetadata {
	/**
	 * Importance is low.
	 */
	int IMPORTANCE_LOW = 0;
	/**
	 * Importance is middle.
	 */
	int IMPORTANCE_MIDDLE = 1;
	/**
	 * Importance is high.
	 */
	int IMPORTANCE_HIGH = 2;

	/**
	 * Null mask.
	 */
	int MASK_NONE = 0;
	/**
	 * The description will be spoken.
	 */
	int MASK_SPEAK = 1;
	/**
	 * The video should be paused before speaking the description.
	 */
	int MASK_PAUSE_BEFORE = 1 << 1;
	/**
	 * The video should be paused after speaking the description.
	 */
	int MASK_PAUSE_AFTER = 1 << 2;
	/**
	 * The video should be paused during speaking the description.
	 */
	int MASK_PAUSE_DURING_SPEAK = 1 << 3;

	/**
	 * @return the inner text of the <i>description tag</i>.
	 */
	String getDescription();

	/**
	 * @return the time to start in 1/1000 second.
	 */
	int getStartTime();

	/**
	 * @return the time of duration in 1/1000 second.
	 */
	int getDuration();

	/**
	 * @return the xml:lang string specified in the <i>description tag</i>
	 */
	String getLang();

	/**
	 * @return the type value, combination of the following masks, corresponding
	 *         to the <i>type attribute</i> in the <i>item tag</i>
	 * @see #MASK_PAUSE_BEFORE
	 * @see #MASK_SPEAK
	 * @see #MASK_PAUSE_AFTER
	 * @see #MASK_PAUSE_DURING_SPEAK
	 */
	int getType();

	/**
	 * @return the importance value specified in the <i>item tag</i>
	 * @see #IMPORTANCE_LOW
	 * @see #IMPORTANCE_MIDDLE
	 * @see #IMPORTANCE_HIGH
	 */
	int getImportance();

	int getSpeed();

	String getGender();

	/*
	 * public String getAddition();
	 * 
	 * public void setAddition(String addition);
	 * 
	 * public boolean hasAddition();
	 * 
	 * public void setAdditionSpeed(String speed);
	 * 
	 * public int getAdditionSpeed();
	 * 
	 * public void setAdditionGender(String gender);
	 * 
	 * public String getAdditionGender();
	 */

	public boolean hasValidWav();

	public String getWavUri();

	public String getWavLocal();

	public int getWavSpeed();

	public boolean getWavEnabled();

	public void setWavUri(String wavUri);

	public void setWavLocal(String wavLocal);

	public void setWavSpeed(String wavSpeed);

	public void setWavEnabled(String wavEnabled);

}
