/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore.nvdl.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The <code>NVDLMessage</code> holds messages in NVDL scripts.
 */
public class NVDLMessage {
    private String defMessage;
    // lang -> message;
    private HashMap<String, String> langMessage;

    public void addMessage(String lang, String message) {
        if (lang == null) {
            defMessage = message;
        } else {
            langMessage.put(lang, message);
        }
    }

    public String getMessage(String lang) {
        if (lang == null) return defMessage;
        String message = langMessage.get(lang);
        if (message == null) return defMessage;
        return message;
    }

    public String toString() {
        StringBuffer r = new StringBuffer();
        if (defMessage != null) {
        	r.append(defMessage);
        	r.append(";");
        }
        Iterator it = langMessage.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            r.append("Lang:");
            r.append(entry.getKey());
            r.append("->");
            r.append(entry.getValue());
            r.append(";");
        }
        return r.toString();
    }

    public NVDLMessage() {
        langMessage = new HashMap<String, String>(0);
    }
}
