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
package org.eclipse.actf.ai.screenreader.jaws.script;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Key {
    public int key;

    public int modifier;

    public Key jawsKey;

    public boolean jawsHandle = false;

    public boolean jawsSayAllStop = true;

    public boolean jawsSayAllStopIgnore = false;
    
    public boolean currentScript = false;
    
    public boolean jawsScript = true;

    public Key(Node keyNode, Element commandElement) {
        this(keyNode.getTextContent().trim());
        
        if (keyNode instanceof Element) {
            Element keyElem = (Element) keyNode;
            String key = keyElem.getAttribute("jawskey");
            if ((key != null) && (key.length() > 0)) {
                jawsKey = new Key(key);
            }
            String jawsHandleVal = keyElem.getAttribute("jawshandle");
            if (jawsHandleVal != null) {
                if (jawsHandleVal.trim().equals("true")) {
                    jawsHandle = true;
                }
            }
            String jawsSayAllStopVal = keyElem.getAttribute("jawsSayAllStop");
            if (jawsSayAllStopVal != null) {
                if (jawsSayAllStopVal.trim().equals("false")) {
                    jawsSayAllStop = false;
                }
            }
            String jawsScriptVal = keyElem.getAttribute("jawsscript");
            if (jawsScriptVal != null) {
                if (jawsScriptVal.trim().equals("false")) {
                    jawsScript = false;
                }
            }
            if ("speakAll".equals(commandElement.getTagName())) {
                jawsSayAllStopIgnore = true;
            }
        }
    }

    public Key(String keyStr) {
        init(keyStr);
    }

    private void init(String keyStr) {
        String keyStrs[] = keyStr.split("[ \t\r\n]");

        if ("currentScript".equals(keyStr)){
            currentScript = true;
            return;
        }
        
        modifier = 0;
        for (int i = 0; i < keyStrs.length; i++) {
            if (keyStrs[i].startsWith("VK_")) {
                key = convertToInt(keyStrs[i]);
            } else if (keyStrs[i].endsWith("_MASK")) {
                modifier |= convertToInt(keyStrs[i]);
            }
        }
        if(key == 0){
            System.err.println("ERROR init "+keyStr);
        }
    }

    @Override
    public String toString() {
        return KeyConverter.convert(key, modifier);
    }

    public String toTypeString() {
        if (key == KeyEvent.VK_PAUSE)
            return "Control+Alt+Shift+P";
        return toString();
    }

    private int convertToInt(String code) {
        code = code.trim();
        try {
            int i = Integer.parseInt(code);
            return i;
        } catch (java.lang.NumberFormatException e) {
            //e.printStackTrace();
        }

        if (code.equals("VK_RETURN")) {
            return 13;
        }

        try {
            Class c = Class.forName("java.awt.event.KeyEvent");
            Field f = c.getField(code);
            return ((Integer) f.get(null)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.err.println("ERROR "+code);
        return 0;
    }

    public String toFuncString() {
        String temp = toString();
        temp = temp.replaceAll("\\+", "_");
        return "func_" + temp;
    }
}
