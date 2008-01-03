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

package org.eclipse.actf.ai.xmlstore.nvdl.fm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLAction;
import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLRules;


/**
 * The <code>ActionList</code> encapsulates a sequence of NVDLActions
 * by which we handle the history of NVDL processings.
 */
public class ActionList {
    public static class InvalidIdException extends IllegalArgumentException {
        private static final long serialVersionUID = 5426408147221128508L;
        public final String id;
        InvalidIdException(String id) {
            this.id = id;
        }
    }

    private final ArrayList<NVDLAction> actions = new ArrayList<NVDLAction>();
    public List<NVDLAction> getActions() {
        return actions;
    }

    private int count;
    public int getCount() {
        return count;
    }

    private void invalidId(String id) throws InvalidIdException {
        actions.clear();
        count = 0;
        throw new InvalidIdException(id);
    }

    public int getPrecedence() {
        // TODO!
        return 0;
    }

    public boolean match (ActionList al) {
        int size = actions.size();
        if (size != al.actions.size()) return false;
        for (int i = 0; i < size; i++) {
            NVDLAction a1 = actions.get(i);
            NVDLAction a2 = al.actions.get(i);
            if (!a1.equals(a2)) return false;
        }
        return true;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ActionList)) return false;
        ActionList al = (ActionList) o;
        if (count != al.count) return false;
        return match(al);
    }

    public int hashCode() {
        int v = 0;
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            NVDLAction a = actions.get(i);
            v ^= a.hashCode();
        }
        return v;
    }
    
    public ActionList() {
    }

    public ActionList(NVDLRules rules, String id) throws InvalidIdException {
        actions.clear();
        String[] aids = id.split("[ \t\n\r]");
        for (int i = 0; i < aids.length; i++) {
            NVDLAction a = rules.getAction(aids[i]);
            if (a != null) {
                actions.add(a);
                continue;
            }
            if (i != (aids.length - 1)) {
                invalidId(id);
            }
            try {
                count = Integer.parseInt(aids[i]);
            } catch (NumberFormatException e) {
                invalidId(id);
            }
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            NVDLAction a = actions.get(i);
            buf.append(a.getName());
            buf.append(' ');
        }
        buf.append(getCount());
        return buf.toString();
    }
}
