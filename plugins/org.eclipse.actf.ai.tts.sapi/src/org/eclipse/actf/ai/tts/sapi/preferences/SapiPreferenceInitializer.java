/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.sapi.preferences;

import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.tts.sapi.engine.SapiVoice;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


public class SapiPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = SAPIPlugin.getDefault().getPreferenceStore();
		store.setDefault(SapiVoice.ID, "Microsoft Sam"); //$NON-NLS-1$
	}

}
