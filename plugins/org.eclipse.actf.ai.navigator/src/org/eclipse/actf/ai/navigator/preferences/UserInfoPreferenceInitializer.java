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
package org.eclipse.actf.ai.navigator.preferences;

import org.eclipse.actf.ai.internal.navigator.NavigatorPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * Class used to initialize default preference values.
 */
public class UserInfoPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
    public void initializeDefaultPreferences() {
		IPreferenceStore store = NavigatorPlugin.getDefault().getPreferenceStore();
		store.setDefault(UserInfoPreferenceConstants.AUTO_REFRESH, true);
        store.setDefault(UserInfoPreferenceConstants.AUTO_SAVE, true);
        store.setDefault(UserInfoPreferenceConstants.NONVERBAL_OVERVIEW, true);
	}

}
