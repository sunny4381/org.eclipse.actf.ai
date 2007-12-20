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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>NVDLTriggerManager</code> manages when to fire 'triggers'.
 */
public class NVDLTriggerManager {
    static private class Trigger {
        final String ns;
        final String[] localNames;

        public String toString() {
            return "{" + ns + "}" + localNames;
        }

        public boolean match(String ns, String localName) {
            if (this.ns.equals(ns)) {
                for (int i = 0; i < localNames.length; i++) {
                    if (localNames[i].equals(localName)) return true;
                }
            }
            return false;
        }

        Trigger(String ns, String[] localNames) {
            this.ns = ns;
            this.localNames = localNames;
        }
    }
    List<Trigger> triggers = new ArrayList<Trigger>(0);

    public NVDLTriggerManager() {
    }

    public void addTrigger(String ns, String[] localNames) {
        Trigger t = new Trigger(ns, localNames);
        triggers.add(t);
    }

    public String toString() {
        StringBuffer r = new StringBuffer("Triggers:");
        for (Iterator<Trigger> it = triggers.iterator();
             it.hasNext();) {
            Trigger trigger = it.next();
            r.append(trigger);
            r.append(";");
        }
        return r.toString();
    }

    public boolean match(String ns, String localName, LinkedList elemStack) {
        if (elemStack.isEmpty()) return false;
        NVDLElement parent = (NVDLElement) elemStack.getLast();

        for (int i = 0; i < triggers.size(); i++) {
            Trigger t = triggers.get(i);
            if (t.match(ns, localName)
                && !t.match(parent.ns, parent.localName)) {
                return true;
            }
        }

        return false;
    }

    public boolean match(NVDLElement e, NVDLElement parent) {
        for (int i = 0; i < triggers.size(); i++) {
            Trigger t = (Trigger) triggers.get(i);
            if (t.match(e.ns, e.localName)
                && !t.match(parent.ns, parent.localName)) {
                return true;
            }
        }

        return false;
    }
}
