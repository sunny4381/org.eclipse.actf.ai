/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.tts.sapi.preferences;

import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.tts.sapi.engine.SapiVoice;
import org.eclipse.actf.ai.voice.preferences.util.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

public class SapiAudioOutputFieldEditor extends ComboFieldEditor {

	private static IPreferenceStore preferenceStore = SAPIPlugin.getDefault()
			.getPreferenceStore();

	public SapiAudioOutputFieldEditor(String labelText, Composite parent) {
		super(SapiVoice.AUDIO_OUTPUT, labelText, null, parent);
	}

	protected void initLabelsAndValues(String[][] labelsAndValues) {
		super.initLabelsAndValues(SapiTestManager.getInstance()
				.getAudioOutputNames());
	}
	
	@Override
	protected void fireValueChanged(String property, Object oldValue,
			Object newValue) {

		preferenceStore.setValue(SapiVoice.AUDIO_OUTPUT, newValue.toString());

		super.fireValueChanged(property, oldValue, newValue);
	}
}
