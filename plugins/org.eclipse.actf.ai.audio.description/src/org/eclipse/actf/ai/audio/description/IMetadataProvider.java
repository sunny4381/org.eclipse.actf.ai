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

import java.util.ArrayList;
import java.util.Locale;

/**
 * This represents an audio description file.
 */
public interface IMetadataProvider {
    /**
     * @return The number of items.
     */
    public int getSize();

    /**
     * @param index The index of the item.
     * @return The item of audio description specified by the index.
     */
    public IMetadata getItem(int index);

    /**
     * @return The list of the items of audio description. 
     */
    public ArrayList<IMetadata> getAllItems();

    /**
     * @param position The position of the video in 1/1000 second from the start of the video.
     * @return The index of the item corresponding to the position.
     */
    public int getIndex(int position);

    /**
     * @param locale Set the locale information to the provider. The provider selects descriptions specified in the locale. 
     */
    public void setLocale(Locale locale);
    
    /**
     * Set the metadata information in default locale.
     * @see #setLocale(Locale)
     * @see #setAlternative(Locale)
     */
    public void setAlternative();

    /**
     * Set the metadata information in specified locale.
     * @param locale Set the metadata information in specified locale.
     */
    public void setAlternative(Locale locale);

    /**
     * Reload the audio description file.
     */
    public void reload();
    
    /**
     * @return Return true if this has any metadata information.
     */
    public boolean hasMetadata();
}
