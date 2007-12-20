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
package org.eclipse.actf.ai.voice.internal;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;


public class TTSRegistry {

    private static final String TTS_EXTENSION = "org.eclipse.actf.ai.voice.TTSEngine"; //$NON-NLS-1$

    private static final String DEFAULT_TTS = "org.eclipse.actf.ai.tts.sapi.engine.SapiVoice"; //$NON-NLS-1$

    private static IConfigurationElement[] ttsElements;

    private static boolean[] availables;

    static {
        initialize();
    }

    public static void initialize() {
        ttsElements = Platform.getExtensionRegistry().getConfigurationElementsFor(TTS_EXTENSION);
        Arrays.sort(ttsElements, new Comparator() {
            public int compare(Object o1, Object o2) {
                String s1 = ((IConfigurationElement)o1).getAttribute("priority");
                String s2 = ((IConfigurationElement)o2).getAttribute("priority");
                int i1 = 0;
                int i2 = 0;
                try {
                    i1 = Integer.parseInt(s1);
                } catch (NumberFormatException e) {
                }
                try {
                    i2 = Integer.parseInt(s2);
                } catch (NumberFormatException e) {
                }
                return i2 - i1;
            }
        });
        availables = new boolean[ttsElements.length];
        for (int i = 0; i < ttsElements.length; i++) {
            try {
                if (((ITTSEngine) ttsElements[i].createExecutableExtension("class")).isAvailable()) {
                    availables[i] = true;
                }
            } catch (Exception e) {
                availables[i] = false;
            }
        }
    }
    
    public static boolean isAvailable(String id){
        try {
            for (int i = 0; i < ttsElements.length; i++) {
                if (id.equals(ttsElements[i].getAttribute("id"))) { //$NON-NLS-1$
                    return availables[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getDefaultEngine() {
        for (int i = 0; i < ttsElements.length; i++) {
            if (availables[i]) {
                return ttsElements[i].getAttribute("id");
            }
        }
        return DEFAULT_TTS;
    }

    public static String[][] getLabelAndIds() {
        String[][] labelAndIds = new String[ttsElements.length][2];
        for (int i = 0; i < ttsElements.length; i++) {
            labelAndIds[i][0] = ttsElements[i].getAttribute("name"); //$NON-NLS-1$
            if(availables[i])
                labelAndIds[i][1] = ttsElements[i].getAttribute("id"); //$NON-NLS-1$
            else
                labelAndIds[i][1] = "";
        }
        return labelAndIds;
    }

    public static ITTSEngine createTTSEngine(String id) {
        try {
            for (int i = 0; i < ttsElements.length; i++) {
                if (id.equals(ttsElements[i].getAttribute("id"))) { //$NON-NLS-1$
                    return (ITTSEngine) ttsElements[i].createExecutableExtension("class"); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
