/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.tts.msp.preferences;

import org.eclipse.actf.ai.tts.msp.MspPlugin;
import org.eclipse.actf.ai.tts.msp.engine.MspVoice;
import org.eclipse.actf.ai.voice.preferences.util.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

public class MspAudioOutputFieldEditor extends ComboFieldEditor {

	private static IPreferenceStore preferenceStore = MspPlugin.getDefault()
			.getPreferenceStore();

	public MspAudioOutputFieldEditor(String labelText, Composite parent) {
		super(MspVoice.AUDIO_OUTPUT, labelText, null, parent);
	}

	protected void initLabelsAndValues(String[][] labelsAndValues) {
		super.initLabelsAndValues(MspTestManager.getInstance()
				.getAudioOutputNames());
	}
	
	@Override
	protected void fireValueChanged(String property, Object oldValue,
			Object newValue) {

		preferenceStore.setValue(MspVoice.AUDIO_OUTPUT, newValue.toString());

		super.fireValueChanged(property, oldValue, newValue);
	}
}
