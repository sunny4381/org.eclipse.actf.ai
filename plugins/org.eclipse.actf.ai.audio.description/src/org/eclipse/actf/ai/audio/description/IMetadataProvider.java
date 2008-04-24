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
	 * @return the number of items.
	 */
	public int getSize();

	/**
	 * {@link #prepareMetadata()} should be called before calling this method.
	 * 
	 * @param index
	 *            the index of the item.
	 * @return the item of audio description specified by the index.
	 */
	public IMetadata getItem(int index);

	/**
	 * {@link #prepareMetadata()} should be called before calling this method.
	 * 
	 * @return the list of the items of audio description.
	 */
	public ArrayList<IMetadata> getAllItems();

	/**
	 * {@link #prepareMetadata()} should be called before calling this method.
	 * 
	 * @param position
	 *            the position of the video in 1/1000 second from the start of
	 *            the video.
	 * @return the index of the item corresponding to the position.
	 */
	public int getIndex(int position);

	/**
	 * Set the locale information to the provider. The provider selects
	 * descriptions for the locale.
	 * 
	 * @param locale
	 *            the local to be set.
	 */
	public void setLocale(Locale locale);

	/**
	 * Prepare the metadata information in the default locale or the locale
	 * specified by {@link #setLocale(Locale)}.
	 * 
	 * @see #setLocale(Locale)
	 * @see #prepareMetadata(Locale)
	 */
	public void prepareMetadata();

	/**
	 * Prepare the metadata information in the specified locale.
	 * 
	 * @param locale
	 *            the local to be used.
	 */
	public void prepareMetadata(Locale locale);

	/**
	 * Reload the audio description file.
	 */
	public void reload();

	/**
	 * @return whether the instance has any metadata information or not.
	 */
	public boolean hasMetadata();
}
