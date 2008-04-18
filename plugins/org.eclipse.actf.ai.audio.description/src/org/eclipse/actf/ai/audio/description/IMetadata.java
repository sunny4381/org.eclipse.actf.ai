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
 * This represents an item of audio description such as following description.
 * <pre>
 *   &lt;item importance="high" type="pause-before|speak"&gt;
 *     &lt;start type="relTime"&gt;00:00:100&lt;/start&gt;
 *     &lt;duration&gt;00:04:500&lt;/duration&gt;
 *     &lt;description xml:lang="en"&gt;The following preview has been approved for all audiences.&lt;/description&gt;
 *   &lt;/item&gt;
 * </pre>
 */
public interface IMetadata {
    int IMPORTANCE_LOW = 0;
    int IMPORTANCE_MIDDLE = 1;
    int IMPORTANCE_HIGH = 2;
    
    int MASK_NONE = 0;
    int MASK_PAUSE_BEFORE = 1;
    int MASK_SPEAK = 2;
    int MASK_PAUSE_AFTER = 4;
    int MASK_PAUSE_DURING_SPEAK = 8;
    
    /**
     * @return The inner text of the <i>description tag</i>.
     */
    String getDescription();
    
    /**
     * @return The time to start in 1/1000 second.
     */
    int getStartTime();

    /**
     * @return The time of duration in 1/1000 second.
     */
    int getDuration();
    
    /**
     * @return The xml:lang string specified in the <i>description tag</i>
     */
    String getLang();
    
    /**
     * @return The type value, combination of the following masks, corresponding to the <i>type attribute</i> in the <i>item tag</i>
     * @see #MASK_NONE
     * @see #MASK_PAUSE_BEFORE
     * @see #MASK_SPEAK
     * @see #MASK_PAUSE_AFTER
     * @see #MASK_PAUSE_DURING_SPEAK
     */
    int getType();
    
    /**
     * @return The importance value specified in the <i>item tag</i>
     * @see #IMPORTANCE_LOW
     * @see #IMPORTANCE_MIDDLE
     * @see #IMPORTANCE_HIGH
     */
    int getImportance();
}
