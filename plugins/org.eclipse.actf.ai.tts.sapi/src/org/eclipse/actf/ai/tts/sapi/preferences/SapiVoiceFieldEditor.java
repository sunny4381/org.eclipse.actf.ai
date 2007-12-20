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

import org.eclipse.actf.ai.tts.sapi.engine.SapiVoice;
import org.eclipse.actf.ai.voice.preferences.ComboFieldEditor;
import org.eclipse.swt.widgets.Composite;



public class SapiVoiceFieldEditor extends ComboFieldEditor {

	public SapiVoiceFieldEditor(String labelText, Composite parent) {
		super(SapiVoice.ID, labelText, null, parent);
	}

	protected void initLabelsAndValues(String[][] labelsAndValues) {
		super.initLabelsAndValues(SapiTestManager.getInstance().getVoiceNames());
	}
}
