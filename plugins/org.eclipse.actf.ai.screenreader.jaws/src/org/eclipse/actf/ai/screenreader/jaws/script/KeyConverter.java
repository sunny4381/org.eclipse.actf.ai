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
import java.util.HashMap;
import java.util.Iterator;

public class KeyConverter {

    private static HashMap<Integer, String> k2s = new HashMap<Integer, String>();

    private static HashMap<String, Integer> s2k = new HashMap<String, Integer>();

    static {
        k2s.put(KeyEvent.VK_0, "0");
        k2s.put(KeyEvent.VK_1, "1");
        k2s.put(KeyEvent.VK_2, "2");
        k2s.put(KeyEvent.VK_3, "3");
        k2s.put(KeyEvent.VK_4, "4");
        k2s.put(KeyEvent.VK_5, "5");
        k2s.put(KeyEvent.VK_6, "6");
        k2s.put(KeyEvent.VK_7, "7");
        k2s.put(KeyEvent.VK_8, "8");
        k2s.put(KeyEvent.VK_9, "9");
        k2s.put(KeyEvent.VK_NUMPAD0, "NumPad0");
        k2s.put(KeyEvent.VK_NUMPAD1, "NumPad1");
        k2s.put(KeyEvent.VK_NUMPAD2, "NumPad2");
        k2s.put(KeyEvent.VK_NUMPAD3, "NumPad3");
        k2s.put(KeyEvent.VK_NUMPAD4, "NumPad4");
        k2s.put(KeyEvent.VK_NUMPAD5, "NumPad5");
        k2s.put(KeyEvent.VK_NUMPAD6, "NumPad6");
        k2s.put(KeyEvent.VK_NUMPAD7, "NumPad7");
        k2s.put(KeyEvent.VK_NUMPAD8, "NumPad8");
        k2s.put(KeyEvent.VK_NUMPAD9, "NumPad9");
        k2s.put(KeyEvent.VK_A, "A");
        k2s.put(KeyEvent.VK_B, "B");
        k2s.put(KeyEvent.VK_C, "C");
        k2s.put(KeyEvent.VK_D, "D");
        k2s.put(KeyEvent.VK_E, "E");
        k2s.put(KeyEvent.VK_F, "F");
        k2s.put(KeyEvent.VK_G, "G");
        k2s.put(KeyEvent.VK_H, "H");
        k2s.put(KeyEvent.VK_I, "I");
        k2s.put(KeyEvent.VK_J, "J");
        k2s.put(KeyEvent.VK_K, "K");
        k2s.put(KeyEvent.VK_L, "L");
        k2s.put(KeyEvent.VK_M, "M");
        k2s.put(KeyEvent.VK_N, "N");
        k2s.put(KeyEvent.VK_O, "O");
        k2s.put(KeyEvent.VK_P, "P");
        k2s.put(KeyEvent.VK_Q, "Q");
        k2s.put(KeyEvent.VK_R, "R");
        k2s.put(KeyEvent.VK_S, "S");
        k2s.put(KeyEvent.VK_T, "T");
        k2s.put(KeyEvent.VK_U, "U");
        k2s.put(KeyEvent.VK_V, "V");
        k2s.put(KeyEvent.VK_W, "W");
        k2s.put(KeyEvent.VK_X, "X");
        k2s.put(KeyEvent.VK_Y, "Y");
        k2s.put(KeyEvent.VK_Z, "Z");
        k2s.put(KeyEvent.VK_F1, "F1");
        k2s.put(KeyEvent.VK_F2, "F2");
        k2s.put(KeyEvent.VK_F3, "F3");
        k2s.put(KeyEvent.VK_F4, "F4");
        k2s.put(KeyEvent.VK_F5, "F5");
        k2s.put(KeyEvent.VK_F6, "F6");
        k2s.put(KeyEvent.VK_F7, "F7");
        k2s.put(KeyEvent.VK_F8, "F8");
        k2s.put(KeyEvent.VK_F9, "F9");
        k2s.put(KeyEvent.VK_F10, "F10");
        k2s.put(KeyEvent.VK_F11, "F11");
        k2s.put(KeyEvent.VK_F12, "F12");
        k2s.put(KeyEvent.VK_F13, "F13");
        k2s.put(KeyEvent.VK_F14, "F14");
        k2s.put(KeyEvent.VK_F15, "F15");
        k2s.put(KeyEvent.VK_F16, "F16");
        k2s.put(KeyEvent.VK_F17, "F17");
        k2s.put(KeyEvent.VK_F18, "F18");
        k2s.put(KeyEvent.VK_F19, "F19");
        k2s.put(KeyEvent.VK_F20, "F20");
        k2s.put(KeyEvent.VK_F21, "F21");
        k2s.put(KeyEvent.VK_F22, "F22");
        k2s.put(KeyEvent.VK_F23, "F23");
        k2s.put(KeyEvent.VK_F24, "F24");
        k2s.put(KeyEvent.VK_BACK_SPACE, "Backspace");
        k2s.put(KeyEvent.VK_BACK_QUOTE, "`");
        k2s.put(KeyEvent.VK_CLEAR, "Clear");
        k2s.put(KeyEvent.VK_COLON, "Colon");
        k2s.put(KeyEvent.VK_CLOSE_BRACKET, "]");
        k2s.put(KeyEvent.VK_COMMA, ",");
        k2s.put(KeyEvent.VK_DELETE, "Delete");
        k2s.put(KeyEvent.VK_DOLLAR, "$");
        k2s.put(KeyEvent.VK_DOWN, "DownArrow");
        k2s.put(KeyEvent.VK_EQUALS, "Equals");
        k2s.put(KeyEvent.VK_ESCAPE, "Escape");
        k2s.put(KeyEvent.VK_ALT, "Alt");
        k2s.put(13, "Enter");
        k2s.put(KeyEvent.VK_END, "End");
        k2s.put(KeyEvent.VK_HOME, "Home");
        k2s.put(KeyEvent.VK_INSERT, "Insert");
        k2s.put(KeyEvent.VK_LEFT, "LeftArrow");
        k2s.put(KeyEvent.VK_MINUS, "-");
        k2s.put(KeyEvent.VK_OPEN_BRACKET, "[");
        k2s.put(KeyEvent.VK_PAUSE, "Pause");
        k2s.put(KeyEvent.VK_PAGE_DOWN, "PageDown");
        k2s.put(KeyEvent.VK_PAGE_UP, "PageUp");
        k2s.put(KeyEvent.VK_PERIOD, ".");
        k2s.put(KeyEvent.VK_PLUS, "+");
        k2s.put(KeyEvent.VK_RIGHT, "RightArrow");
        k2s.put(KeyEvent.VK_SEMICOLON, ";");
        k2s.put(KeyEvent.VK_SEPARATER, "|");
        k2s.put(KeyEvent.VK_SLASH, "/");
        k2s.put(KeyEvent.VK_SPACE, "Space");
        k2s.put(KeyEvent.VK_TAB, "Tab");
        k2s.put(KeyEvent.VK_UNDERSCORE, "_");
        k2s.put(KeyEvent.VK_UP, "UpArrow");
        k2s.put(KeyEvent.VK_CONTROL, "Control");
        k2s.put(KeyEvent.VK_SHIFT, "Shift");

        for (Iterator<Integer> i = k2s.keySet().iterator(); i.hasNext();) {
            int key = i.next();
            s2k.put(k2s.get(key).toLowerCase(), key);
        }
        s2k.put("numpadplus", KeyEvent.VK_PLUS);
        s2k.put("numpadminus", KeyEvent.VK_MINUS);
        s2k.put("numpadslash", KeyEvent.VK_SLASH);
        s2k.put("numpadstar", KeyEvent.VK_ASTERISK);
        s2k.put("homerow", KeyEvent.VK_HOME);
        s2k.put("extendedleftarrow", KeyEvent.VK_LEFT);
        s2k.put("extendedrightarrow", KeyEvent.VK_RIGHT);
        s2k.put("extendeduparrow", KeyEvent.VK_UP);
        s2k.put("extendeddownarrow", KeyEvent.VK_DOWN);
        s2k.put("comma", KeyEvent.VK_COMMA);
        s2k.put("period", KeyEvent.VK_PERIOD);
        s2k.put("apostrophe", KeyEvent.VK_QUOTE);
        s2k.put("leftbracket", KeyEvent.VK_OPEN_BRACKET);
        s2k.put("rightbracket", KeyEvent.VK_CLOSE_BRACKET);
        s2k.put("semicolon", KeyEvent.VK_SEMICOLON);
    }

    public static boolean isIt(int value, int mask) {
        return (value & mask) == mask;
    }

    public static int convert(String key) {
        Integer i = s2k.get(key.toLowerCase());
        if (i == null) {
            return 0;
        }
        return i.intValue();
    }

    public static String convert(int key, int modifier) {
        StringBuffer sb = new StringBuffer();

        if (isIt(modifier, KeyEvent.ALT_MASK)) {
            sb.append("Alt+");
        }
        if (isIt(modifier, KeyEvent.CTRL_MASK)) {
            sb.append("Control+");
        }
        if (isIt(modifier, KeyEvent.SHIFT_MASK)) {
            sb.append("Shift+");
        }

        String s = k2s.get(new Integer(key));
        if (s == null) {
            System.err.println("ERROR convert "+key);
            System.exit(0);
        }
        sb.append(s);

        return sb.toString();
    }

}
