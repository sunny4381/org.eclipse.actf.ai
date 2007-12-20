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

public class Keymap {
	public int key;

	public int modifier;

	public boolean jaws;

	public boolean windows;

	public String scriptName;

	public Keymap(String line) {
		try {
			int index = line.indexOf("=");

			String keyStr = line.substring(0, index).trim();
            if(keyStr.equals("JAWSKey+H")){
                int i = 1;
            }
			scriptName = line.substring(index + 1).trim();

			String[] keyStrs = keyStr.split("[\\+]");
			for (int i = 0; i < keyStrs.length; i++) {
				String k = keyStrs[i].toLowerCase().replaceAll(" ","");
				if (k.equals("control"))
					modifier |= KeyEvent.CTRL_MASK;
				else if (k.equals("shift"))
					modifier |= KeyEvent.SHIFT_MASK;
				else if (k.equals("alt"))
					modifier |= KeyEvent.ALT_MASK;
				else if (k.equals("jawskey") || k.equals("insert"))
                    jaws = true;
				else if (k.equals("windows"))
					windows = true;
				else
					key = KeyConverter.convert(k);
                
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			System.out.println(line);
		}
	}

	public String toString() {
		return key+", "+modifier+", "+jaws+", "+windows+", "+scriptName;
	}
	
	public boolean equals(Object o){
		if(o instanceof Key){
			Key k = (Key)o;
			if(k.key == key && k.modifier == modifier &&
					!jaws && !windows)
				return true;
		}
		return false;
	}
}
