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
package org.eclipse.actf.ai.tts.protalker.preferences;

import org.eclipse.actf.ai.tts.protalker.ProTalkerPlugin;
import org.eclipse.actf.ai.tts.protalker.engine.ProTalker;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


public class ProTalkerPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = ProTalkerPlugin.getDefault().getPreferenceStore();
		store.setDefault(ProTalker.ID, "male"); //$NON-NLS-1$
	}

}
